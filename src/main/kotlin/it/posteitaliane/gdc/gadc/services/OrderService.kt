package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

@Service
class OrderService(val db:JdbcTemplate, val tr:TransactionTemplate, val ops:OperatorService, val dcs:DatacenterService, val sups:SupplierService) {

    data class Result(val order:Order?, val error:String?) {
        val isOk get() = error.isNullOrEmpty().not()
        val isError get() = order != null
    }

    val orderMapper = RowMapper { rs, _ ->
        Order(
            number = rs.getObject("id", UUID::class.java),
            op = ops.findAll().find { it.username == rs.getString("operator") }!!,
            dc = dcs.findAll(locations = false).find { it.short == rs.getString("datacenter") }!!,
            supplier = sups.findByName(rs.getString("supplier")),
            issued = rs.getDate("issued").toLocalDate(),
            type = Order.Type.valueOf(rs.getString("type")),
            subject = Order.Subject.valueOf(rs.getString("subject")),
            status = Order.Status.valueOf(rs.getString("status")),
        ).apply {
            ref = rs.getString("ref")
        }
    }

    var lineMapper = RowMapper { rs, _ ->
        OrderLine(
            order = findByOrderId(rs.getObject("ownedby", UUID::class.java)),
            item = rs.getString("item"),
            amount = rs.getInt("amount"),
            position = rs.getString("pos")
        )
    }

    private val REGISTER_ORDER_SQL =
        "INSERT INTO ORDERS(id,operator,datacenter,supplier,issued,type,subject,status,ref)" +
                "VALUES(?,?,?,?,?,?,?,?,?)"

    private val QUERY_ALL =
        "SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref FROM ORDERS"

    private val QUERY_SEARCH_KEY = """
        SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref FROM ORDERS 
        JOIN OPERATORS ON uid=operator 
        JOIN DCS ON shortname=datacenter 
    """.trimIndent()

    private val QUERY_BY_ID = """
        $QUERY_SEARCH_KEY 
        WHERE id = ?
    """.trimIndent()

    private val QUERY_LINES = "SELECT ownedby,item,pos,amount,sn FROM ORDERS_LINES WHERE ownedby = ?"

    private val QUERY_ITEMS = "SELECT name FROM ITEMS"

    @Transactional
    fun register(o: Order) {
        db.update(
            REGISTER_ORDER_SQL,
            o.number,
            o.op.username,
            o.dc.short,
            o.supplier.name,
            o.issued,
            o.type.name,
            o.subject.name,
            o.status.name,
            o.ref
        )
    }

    fun findAll(): List<Order> {
        return db.query(QUERY_ALL, orderMapper)
    }

    private fun findByOrderId(uuid: UUID) : Order {
        return db.queryForObject(QUERY_BY_ID, orderMapper, uuid)!!
    }

    fun find(offset: Int, limit: Int, searchKey: String?, sortKey:String?, ascending:Boolean): List<Order> {
        var query = QUERY_SEARCH_KEY

        if ( searchKey != null ) {

            query += " WHERE "
            query +=  " fullname ILIKE '$searchKey%' OR "
            query +=  " lastname ILIKE '$searchKey%' OR "
            query +=  " operator ILIKE '$searchKey%' OR "
            query +=  " supplier ILIKE '$searchKey%' OR "
            query +=  " ref ILIKE '$searchKey%' "

        }

        if( sortKey != null ) {
            query += " ORDER BY $sortKey "

            if( ascending.not() ) {
                query += " DESC "
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"
        //println(query)
        return db.query(query, orderMapper)

    }

    fun fillOrderLines(o:Order) {
        o.lines.clear()
        o.lines.addAll(db.query(QUERY_LINES, lineMapper, o.number))
    }

    fun findItems(): List<String> {
        return db.queryForList(QUERY_ITEMS, String::class.java)
    }


    private val QUERY_SUBMIT_ORDER = "INSERT INTO ORDERS(id,operator,datacenter,supplier,issued,type,subject,status,ref) " +
            "VALUES(?,?,?,?,?,?,?,?,?)"
    private val QUERY_SUBMIT_LINE = "INSERT INTO ORDERS_LINES(ownedby,datacenter,item,pos,amount,sn) " +
            "VALUES(?,?,?,?,?,?)"

    @Transactional
    fun submit(o: Order): Result {

        var result:Result? = Result(null,"")
        //validate order
        //save order
        result = tr.execute { status ->
            try {
                db.update(
                    QUERY_SUBMIT_ORDER,
                    o.number,
                    o.op.username,
                    o.dc.short,
                    o.supplier.name,
                    LocalDateTime.now(),
                    o.type.name,
                    o.subject.name,
                    o.status.name,
                    o.ref
                )

                for (i in o.lines.indices) {
                    db.update(
                        QUERY_SUBMIT_LINE,
                        o.number,
                        o.dc.short,
                        o.lines[i].item,
                        o.lines[i].position,
                        o.lines[i].amount,
                        o.lines[i].sn
                    )
                }

                return@execute Result(o, null)
            } catch (ex:RuntimeException) {
                status.setRollbackOnly()
                print(ex.message)
                return@execute Result(null, ex.message)
            }
        }

        if(result!!.isError) return result

        //save lines
        //update storage
        //update transactions log
        return result
    }

}
