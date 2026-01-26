package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.views.transactions.TransactionFilter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForList
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.stream.Stream

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

    private fun queryBuilder(q:String, offset: Int, limit: Int, sortKey: String?, ascending: Boolean, filter: TransactionFilter?): String {
        var query = q;

        if (filter != null) {
            query = " WHERE true"
            if (filter.dc != null) {
                query += " OR dc LIKE '%${filter.from}%"
            }
            if (filter.type != null) {
                query += " OR type = ${filter.type}"
            }
            if (filter.item != null) {
                query += " OR item LIKE '%${filter.item}%"
            }
            if (filter.operator != null) {
                query += " OR operator ${filter.operator}"
            }
        }

        if (sortKey != null) {
            query += " ORDER BY $sortKey"

            if (ascending.not()) {
                query += " DESC"
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"

        return query
    }

    fun findAll() : List<Transaction> {
        val list = db.query(QUERY_ALL, mapper)
        return list
    }

    fun find(
        offset: Int,
        limit: Int,
        sortKey: String?,
        ascending: Boolean,
        searchFilter: TransactionFilter?
    ): List<Transaction> {
        val query = queryBuilder(QUERY_ALL, offset, limit, sortKey, ascending, searchFilter)
        return db.query(query, mapper)
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

    fun count(
        offset: Int,
        limit: Int,
        searchFilter: TransactionFilter?
    ): Int {
        val query = queryBuilder("SELECT COUNT(*) FROM TRANSACTIONS", offset, limit, null, true, searchFilter)
        return db.queryForObject(query, Int::class.java)!!
    }

}