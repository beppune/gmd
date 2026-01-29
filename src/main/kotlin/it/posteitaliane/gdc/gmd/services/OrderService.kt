package it.posteitaliane.gdc.gmd.services

import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.OrderLine
import it.posteitaliane.gdc.gmd.services.specs.SpecService
import org.slf4j.Logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import kotlin.text.isNullOrEmpty

@Service
class OrderService(
    private val db:JdbcTemplate,
    private val tr:TransactionTemplate,

    private val ops:OperatorService,
    private val dcs:DatacenterService,
    private val sups:SupplierService,
    private val ss:StorageService,
    private val trs:TransactionsService,
    private val specs: SpecService,
    private val logger:Logger
) {

    val orderMapper = RowMapper { rs, _ ->
        Order(
            number = rs.getInt("id"),
            op = ops.findAll().find { it.username == rs.getString("operator") }!!,
            dc = dcs.findAll(locations = false).find { it.short == rs.getString("datacenter") }!!,
            supplier = sups.findByName(rs.getString("supplier"))!!,
            issued = rs.getTimestamp("issued").toLocalDateTime(),
            type = Order.Type.valueOf(rs.getString("type")),
            subject = Order.Subject.valueOf(rs.getString("subject")),
            status = Order.Status.valueOf(rs.getString("status")),
        ).apply {
            ref = rs.getString("ref")
            remarks = rs.getString("remarks")

            fillPath(this)
        }

    }

    var lineMapper = RowMapper { rs, _ ->
        OrderLine(
            order = findByOrderId(rs.getObject("ownedby", Int::class.java)),
            item = rs.getString("item"),
            amount = rs.getInt("amount"),
            position = rs.getString("pos"),
            sn = rs.getString("sn"),
            pt = rs.getString("pt")
        )
    }


    private val QUERY_ALL =
        "SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref,remarks FROM ORDERS"

    private val QUERY_SEARCH_KEY = """
        SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref,remarks FROM ORDERS 
        JOIN OPERATORS ON uid=operator 
        JOIN DCS ON shortname=datacenter 
    """.trimIndent()

    private val QUERY_BY_ID = """
        $QUERY_SEARCH_KEY 
        WHERE id = ?
    """.trimIndent()

    private val QUERY_LINES = "SELECT ownedby,item,pos,amount,sn,pt FROM ORDERS_LINES WHERE ownedby = ?"

    private val QUERY_ITEMS = "SELECT name FROM ITEMS"

    fun findAll(fetchLines:Boolean=false): List<Order> {
        return db.query(QUERY_ALL, orderMapper).also {
            if( fetchLines ) {
                it.forEach(this::fillOrderLines)
            }
        }
    }

    fun findByOrderId(id: Int) : Order {
        return db.queryForObject(QUERY_BY_ID, orderMapper, id)!!
    }

    private fun queryBuilder(q:String, offset: Int, limit: Int, searchKey: String?, sortKey:String?, dcs:String?, ascending:Boolean): String {
        var  query = q
        if ( searchKey != null ) {

            query += " WHERE "
            query +=  " fullname LIKE '$searchKey%' OR "
            query +=  " lastname LIKE '$searchKey%' OR "
            query +=  " operator LIKE '$searchKey%' OR "
            query +=  " supplier LIKE '$searchKey%' OR "
            query +=  " ref LIKE '$searchKey%' "

        }

        if( dcs.isNullOrEmpty().not() ) {
            query += " AND datacenter IN ${dcs} "
        }

        if( sortKey != null ) {
            query += " ORDER BY $sortKey "

            if( ascending.not() ) {
                query += " DESC "
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"

        return query
    }

    fun find(offset: Int, limit: Int, searchKey: String?, sortKey:String?, dcs:String?, ascending:Boolean): List<Order> {
        val query = queryBuilder(QUERY_SEARCH_KEY, offset, limit, searchKey, sortKey, dcs, ascending)


        return db.query(query, orderMapper)

    }

    fun fillOrderLines(o:Order) {
        o.lines.clear()
        o.lines.addAll(db.query(QUERY_LINES, lineMapper, o.number))
    }

    fun findItems(): List<String> {
        return db.queryForList(QUERY_ITEMS, String::class.java)
    }


    private val QUERY_SUBMIT_ORDER = "INSERT INTO ORDERS(operator,datacenter,supplier,issued,type,subject,status,ref,remarks) " +
            "VALUES(?,?,?,?,?,?,?,?,?)"
    private val QUERY_SUBMIT_LINE = "INSERT INTO ORDERS_LINES(ownedby,datacenter,item,pos,amount,sn,pt) " +
            "VALUES(?,?,?,?,?,?,?)"

    private val QUERY_UPDATE_ORDER = "UPDATE ORDERS " +
            " SET operator = ?, datacenter = ?, supplier = ?, issued = ?, type = ?, subject = ?, status = ?, ref = ?, remarks = ?" +
            " WHERE id = ? ORDER BY id LIMIT 1"
    private val QUERY_DELETE_LINES = "DELETE FROM ORDERS_LINES WHERE ownedby = ?"

    fun count(offset: Int, limit: Int, searchKey: String?, sortKey: String?, dcs: String?, ascending: Boolean): Int {
        val query = queryBuilder("SELECT COUNT(*) FROM ORDERS", offset, limit, searchKey, sortKey, dcs, ascending)

        return db.queryForObject(query, Int::class.java)!!
    }

    fun register(o: Order): Result<Order>  = tr.execute { it ->

        try {
                val REMARKS = if(o.remarks.isNullOrEmpty()) null else o.remarks
                if( o.number == -1 ) { //if New
                    db.update(
                        QUERY_SUBMIT_ORDER,
                        o.op.username,
                        o.dc.short,
                        o.supplier.name,
                        LocalDateTime.now(),
                        o.type.name,
                        o.subject.name,
                        o.status.name,
                        o.ref,
                        REMARKS
                    )

                    o.number = db.queryForObject("SELECT LAST_INSERT_ID()", Int::class.java)!!
                } else { //if registered

                    db.update(QUERY_DELETE_LINES, o.number)

                    db.update(
                        QUERY_UPDATE_ORDER,
                        o.op.username, o.dc.short, o.supplier.name, LocalDateTime.now(),
                        o.type.name, o.subject.name, o.status.name, o.ref, REMARKS, o.number
                    )
                }

                for (i in o.lines.indices) {
                    if(o.lines[i].isUnique) {
                        o.lines[i].amount = 1
                    }
                    db.update(
                        QUERY_SUBMIT_LINE,
                        o.number,
                        o.dc.short,
                        o.lines[i].item,
                        o.lines[i].position,
                        o.lines[i].amount,
                        o.lines[i].sn?.uppercase(),
                        o.lines[i].pt?.uppercase()
                    )
                }

                return@execute Result(o, null)
            } catch (ex:TransactionException) {
                it.setRollbackOnly()
                println("OrderService::submit: ${ex.message}")
                return@execute Result(null, ex.message)
            } catch (ex:RuntimeException) {
                it.setRollbackOnly()
                println("OrderService::submit: ${ex.message}")
                return@execute Result(null, ex.message)
            }
        }!!

    fun submit(o:Order) : Result<Order> = tr.execute {
        /*
                NEW & PENDING           -> register as pending

                REGISTERED & PENDING    -> update order as pending

                NEW & COMPLETED         -> registered as completed
                                           update storage and transactions

                REGISTERED & COMPLETED  -> update order as completed
                                           update storage and transactions
         */

        val (_, no) = specs.run(o)

        if( no.isNotEmpty() ) {
            println("Specs failed for $o")
            no.forEach { println(it.name) }
            it.setRollbackOnly()
            return@execute Result(null, "Specs failed")
        }

        try {


            val res = register(o)
            if(res.isError()) {
                it.setRollbackOnly()
                return@execute res
            }

            if( o.status == Order.Status.COMPLETED ) {
                o.lines.forEach { line ->
                    ss.updateStorage(line).also { res ->
                        if (res.isError()) {
                            it.setRollbackOnly()
                            return@execute Result(null, "OrderService::submit: ${res.error}")
                        }
                    }

                    trs.logTransaction(line)
                }
            }

            logger.info("SUMBMITTIN ORDER: ${o.number}")
            return@execute Result(o)
        }catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("OrderService::submit: ${ex.message}")
            return@execute Result(null, ex.message)
        }

    }!!

    fun fillPath(o:Order) {
        try {
            val path: String? = db.queryForObject(
                "SELECT filepath FROM SHIPPINGS WHERE ownedby = ?",
                String::class.java,
                o.number.toString()
            )


            o.filepath = path
        }catch (ex:EmptyResultDataAccessException) {
            //do nothing
        }catch (ex:DataAccessException) {
            ex.printStackTrace()
        }
    }
}
