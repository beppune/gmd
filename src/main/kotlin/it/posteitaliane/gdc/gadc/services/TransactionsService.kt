package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Transaction
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionsService(
    private val db:JdbcTemplate
) {

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

    fun logTransaction(line:OrderLine) {

        val o = line.order

        var type = when(o.type) {
            Order.Type.INBOUND -> "CARICO"
            Order.Type.OUTBOUND -> "SCARICO"
        }
        type += when(o.subject) {
            Order.Subject.INTERNAL -> " INTERNO"
            Order.Subject.SUPPLIER -> " FORNITORE"
            Order.Subject.SUPPLIER_DC -> " MOVING"
        }

        db.update(
            LOG_TRANSACTION_SQL,
            o.op.username, type, o.issued,
            line.item, "${o.dc.short} - ${o.dc.fullName}", line.position,
            line.amount, line.sn, line.pt
        )

    }

}