package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Storage
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

@Service
class OrderService(val db:JdbcTemplate, val tr:TransactionTemplate, val ops:OperatorService, val dcs:DatacenterService, val sups:SupplierService, val ss:StorageService) {

    val orderMapper = RowMapper { rs, _ ->
        Order(
            number = rs.getInt("id"),
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
            order = findByOrderId(rs.getObject("ownedby", Int::class.java)),
            item = rs.getString("item"),
            amount = rs.getInt("amount"),
            position = rs.getString("pos")
        )
    }


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

    private val QUERY_LINES = "SELECT ownedby,item,pos,amount FROM ORDERS_LINES WHERE ownedby = ?"

    private val QUERY_ITEMS = "SELECT name FROM ITEMS"

    fun findAll(): List<Order> {
        return db.query(QUERY_ALL, orderMapper)
    }

    private fun findByOrderId(id: Int) : Order {
        return db.queryForObject(QUERY_BY_ID, orderMapper, id)!!
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


    private val QUERY_SUBMIT_ORDER = "INSERT INTO ORDERS(operator,datacenter,supplier,issued,type,subject,status,ref) " +
            "VALUES(?,?,?,?,?,?,?,?)"
    private val QUERY_SUBMIT_LINE = "INSERT INTO ORDERS_LINES(ownedby,datacenter,item,pos,amount) " +
            "VALUES(?,?,?,?,?)"

    private val QUERY_SUBMIT_STORAGE = "INSERT INTO STORAGE(item,dc,pos,amount,sn,pt) VALUES(?,?,?,?,NULL,NULL)"
    private val QUERY_UPDATE_STORAGE = "UPDATE STORAGE SET amount = ? WHERE item = ? AND dc = ? AND pos = ?"
    private val QUERY_DELETE_STORAGE = "DELETE STORAGE WHERE item = ? AND dc = ? AND pos = ?"

    fun submit(o: Order): Result<Order>  = tr.execute {
        try {
                db.update(
                    QUERY_SUBMIT_ORDER,
                    o.op.username,
                    o.dc.short,
                    o.supplier.name,
                    LocalDateTime.now(),
                    o.type.name,
                    o.subject.name,
                    o.status.name,
                    o.ref
                )

                o.number = db.queryForObject("SELECT LAST_INSERT_ID()", Int::class.java)!!

                for (i in o.lines.indices) {
                    db.update(
                        QUERY_SUBMIT_LINE,
                        o.number,
                        o.dc.short,
                        o.lines[i].item,
                        o.lines[i].position,
                        o.lines[i].amount
                    )
                }

                /*for (i in o.lines.indices) {

                    val line = o.lines[i]

                    when(o.type) {
                        Order.Type.INBOUND -> {
                            val s = ss.findForCount(line.item, o.dc.short, line.position)

                            if( s == null ) {
                                db.update(
                                    QUERY_SUBMIT_STORAGE,
                                    line.item, o.dc.short, line.position, line.amount
                                )
                            } else {
                                db.update(
                                    QUERY_UPDATE_STORAGE,
                                    s.amount + line.amount, line.item, o.dc.short, line.position
                                )
                            }

                        }
                        Order.Type.OUTBOUND -> {
                            var s = ss.findForCount(line.item, o.dc.short, line.position)

                            if( s == null ) {
                                s = Storage(line.item, o.dc, line.position, 0)
                            }

                            if( s.amount > line.amount) {
                                db.update(
                                    QUERY_UPDATE_STORAGE,
                                    s.amount - line.amount, line.item, o.dc.short, line.position
                                )
                            } else if ( s.amount == line.amount ) {
                                db.update(
                                    QUERY_DELETE_STORAGE,
                                    line.item, o.dc.short, line.position
                                )
                            } else {
                                it.setRollbackOnly()
                                return@execute Result(null,"Errore: quantità non disponibile: $line")
                            }
                        }
                    }

                }*/

                return@execute Result(o, null)
            } catch (ex:TransactionException) {
                it.setRollbackOnly()
                println("OrderService::submit: ${ex.message}")
                return@execute Result(null, ex.message)
            }
        }!!

}
