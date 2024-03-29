package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Transaction
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionsService(val db:JdbcTemplate) {

    companion object {
        val mapper = RowMapper { rs, _ ->
            Transaction(
                rs.getInt("id"),
                rs.getString("operator"),
                rs.getString("type"),
                rs.getObject("timestamp", LocalDateTime::class.java),
                rs.getString("item"),
                rs.getString("dc"),
                rs.getString("pos"),
                rs.getInt("amount"),
                rs.getString("sn").orEmpty(),
                rs.getString("pt").orEmpty()
            )
        }
    }

    private val QUERY_ALL = "SELECT id,operator,type,timestamp,item,dc,pos,amount,sn,pt FROM TRANSACTIONS"
    private val LOG_TRANSACTION_SQL = "INSERT INTO TRANSACTIONS (operator,type,timestamp,item,dc,pos,amount,sn,pt)" +
            " VALUES(?,?,?,?,?,?,?,?,?)"

    fun findAll() : List<Transaction> {
        val list = db.query(QUERY_ALL, mapper)
        return list
    }

    fun findFromTo(from:LocalDateTime, to:LocalDateTime) : List<Transaction> {
        val query = "$QUERY_ALL WHERE timestamp BETWEEN ? AND ?"

        val list = db.query(query, mapper, from, to)
        return list
    }

    fun logTransaction(line:OrderLine) {

        val o = line.order

        var type = ""
        when(o.type) {
            Order.Type.INBOUND -> type += "CARICO"
            Order.Type.OUTBOUND -> type += "SCARICO"
        }
        when(o.subject) {
            Order.Subject.INTERNAL -> type += " INTERNO"
            Order.Subject.SUPPLIER -> type += " FORNITORE"
            Order.Subject.SUPPLIER_DC -> type += " MOVING"
        }

        db.update(
            LOG_TRANSACTION_SQL,
            o.op.username, type, o.issued,
            line.item, "${o.dc.short} - ${o.dc.fullName}", line.position,
            line.amount, line.sn, line.pt
        )

    }

}