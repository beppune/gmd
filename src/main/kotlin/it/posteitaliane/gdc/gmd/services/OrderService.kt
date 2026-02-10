package it.posteitaliane.gdc.gmd.services

import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.OrderLine
import it.posteitaliane.gdc.gmd.services.TransactionsService.Companion.dateTimeFormatter
import it.posteitaliane.gdc.gmd.services.specs.SpecService
import org.slf4j.Logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
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
            dc = dcs.findByShortName(rs.getString("datacenter") )!!,
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

    private fun queryBuilder(
        q: String, offset: Int, limit: Int,
        operators: List<String>, dcs: List<String>, from: LocalDate? = null, to: LocalDate? = null,
        type: String? = null, subject: String? = null, status: String? = null, ref: List<String>,
        items: List<String>, pos: List<String>, sn: String? = null, pt: String? = null,
        sortBy: String?=null, asc: Boolean=true
    ): String {
        var  query = q

        var parts = mutableListOf<String>()

        if (operators.isNotEmpty()) {
            operators.joinToString(
                prefix = " operator IN (",
                postfix = ") ",
                separator = ", ",
                transform = { "'$it'" },
            ).also(parts::add)
        }

        if (dcs.isNotEmpty()) {
            dcs.joinToString(
                prefix = " datacenter IN (",
                postfix = ") ",
                separator = ", ",
                transform = { "'$it'" },
            ).also(parts::add)
        }

        if( (from ?: to) != null) {
            val both = if ( from != null && to != null) {
                " AND "
            } else {
                ""
            }

            var q_from = ""
            var q_to = ""
            if (from != null) {
                q_from = "timestamp > '${from!!.format(dateTimeFormatter)}'"
            }
            if (to != null) {
                q_to = "timestamp < '${to!!.format(dateTimeFormatter)}'"
            }

            parts.add(" ($q_from $both $q_to) ")
        }

        if (type.isNullOrEmpty().not()) {
            parts.add(" type = '$type'")
        }

        if (subject != null) {
            parts.add(" subject = '$subject'")
        }

        if (status != null) {
            parts.add(" status = '$status'")
        }

        if (ref != null) {
            parts.add(" ref = '$ref'")
        }

        /* oder lines */
        var lines = mutableListOf<String>()

        if ( items.isNotEmpty() ) {
            items.joinToString(
                prefix = " item IN (",
                postfix = ") ",
                separator = ", ",
                transform = { "'$it'" },
            ).also(lines::add)
        }

        if ( pos.isNotEmpty() ) {
            pos.joinToString(
                prefix = " pos IN (",
                postfix = ") ",
                separator = ", ",
                transform = { "'$it'" },
            ).also(lines::add)
        }

        if (sn != null) {
            lines.add( " sn LIKE '$sn%' " )
        }

        if (pt != null) {
            lines.add( " pt LIKE '$sn%' " )
        }

        if (parts.isNotEmpty() || lines.isNotEmpty()) {
            query += " WHERE "

            if (parts.isNotEmpty()) {
                query += parts.joinToString(separator = " AND ")
            }

            if (lines.isNotEmpty()) {

                val prefix = """
 EXISTS(
	SELECT 1
    FROM orders_lines
    WHERE orders.id=order_lines.ownedby
		AND
                """.trimIndent()

                query += lines.joinToString(
                    prefix = prefix,
                    separator = " AND ",
                    postfix = ") "
                )
            }
        }

        if( sortBy.isNullOrEmpty().not()) {
            val dir = if (asc) "ASC" else "DESC"
            query += " ORDER BY $sortBy $dir "
        }

        query += " LIMIT $limit OFFSET $offset "

        return query
    }

    fun find(
        offset: Int, limit: Int,
        operators: List<String>, dcs: List<String>, from: LocalDate? = null, to: LocalDate? = null,
        type: String? = null, subject: String? = null, status: String? = null, ref: List<String>,
        items: List<String>, pos: List<String>, sn: String? = null, pt: String? = null,
        sortBy: String?=null, asc: Boolean=true): List<Order> {
        val query = queryBuilder(
            q = QUERY_SEARCH_KEY, offset, limit,
            operators, dcs, from, to, type, subject, status, ref,
            items, pos, sn, pt, sortBy, asc
        )


        return db.query(query, orderMapper)

    }

    fun count(
        offset: Int, limit: Int,
        operators: List<String>, dcs: List<String>, from: LocalDate? = null, to: LocalDate? = null,
        type: String? = null, subject: String? = null, status: String? = null, ref: List<String>,
        items: List<String>, pos: List<String>, sn: String? = null, pt: String? = null,
        sortBy: String?=null, asc: Boolean=true): Int {
        val query = queryBuilder(
            q = "SELECT COUNT(*) FROM ORDERS JOIN OPERATORS ON uid=operator JOIN DCS ON shortname=datacenter  ",
            offset, limit, operators, dcs, from, to, type, subject, status, ref,
            items, pos, sn, pt, sortBy, asc
        )
        return db.queryForObject(query, Int::class.java)!!

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
