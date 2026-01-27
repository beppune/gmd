package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.views.transactions.TransactionFilter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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


        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
    }

    private val QUERY_ALL = "SELECT id,operator,type,timestamp,item,dc,pos,amount,sn,pt FROM TRANSACTIONS"
    private val LOG_TRANSACTION_SQL = "INSERT INTO TRANSACTIONS (operator,type,timestamp,item,dc,pos,amount,sn,pt)" +
            " VALUES(?,?,?,?,?,?,?,?,?)"

    private fun queryBuilder(q:String, offset: Int, limit: Int, sortKey: String?, ascending: Boolean, filter: TransactionFilter?): String {
        var query = q;

        if (filter != null) {
            var dc = ""
            var type = ""
            var item = ""
            var operator = ""
            var timestamp = ""

            if (filter.dc != null) {
                dc = " dc = '${filter.dc!!.short}' "
            }
            if (filter.type != null) {
                type = " type = '${filter.type}' "
            }
            if (filter.item != null) {
                item = " item LIKE '%${filter.item}%' "
            }
            if (filter.operators.isNullOrEmpty().not()) {
                operator = " operator IN " + filter.operators.joinToString(
                    prefix = "(",
                    postfix = ") ",
                    separator = ",",
                ) { " '${it.username}'" }
            }
            if( (filter.from ?: filter.to) != null) {
                val both = if ( filter.from != null && filter.to != null) {
                    " AND "
                } else {
                    ""
                }

                var q_from = ""
                var q_to = ""
                if (filter.from != null) {
                    q_from = "timestamp > '${filter.from!!.format(dateTimeFormatter)}'"
                }
                if (filter.to != null) {
                    q_to = "timestamp < '${filter.to!!.format(dateTimeFormatter)}'"
                }

                timestamp = " ($q_from $both $q_to) "
            }

            val f = arrayOf(dc, type, item, operator, timestamp)
                .filter { it.isNullOrBlank().not() }
                .joinToString(separator = " AND ", prefix = " WHERE ")

            query += f
            println(query)
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