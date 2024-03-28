package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Storage
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class StorageService(val db:JdbcTemplate, val dcs:DatacenterService, val tr:TransactionTemplate) {

    val storageMapper = RowMapper { rs, _ ->
        Storage(
            item = rs.getString("item"),
            dc = dcs.findByShortName(rs.getString("dc"))!!,
            pos = rs.getString("pos"),
            amount = rs.getInt("amount"),
            sn = rs.getString("sn")
        )
    }

    private val QUERY_ALL = "SELECT item,dc,pos,amount,sn FROM STORAGE"

    private val QUERY_FOR_COUNT = "SELECT item,dc,pos,amount,sn,pt FROM STORAGE" +
            " WHERE item = ? AND dc = ? AND pos = ?"

    private val QUERY_FOR_SN = "$QUERY_ALL WHERE sn = ?"

    fun findAll() : List<Storage> {
        return db.query(QUERY_ALL, storageMapper)
    }

    fun findForCount(item:String, dc:String, pos:String): Storage? {
        val one = db.query(
            QUERY_FOR_COUNT,
            storageMapper,
            item, dc, pos
        )

        if ( one.size == 1 ) return one.get(0)
        else return null
    }

    fun find(offset: Int, limit: Int, searchKey: String?, ascending: Boolean, sortKey: String?) : List<Storage> {
        var query = QUERY_ALL

        if ( searchKey != null ) {
            query += " WHERE "
            query += " item ILIKE '$searchKey%'"
        }

        if( sortKey != null ) {
            query += " ORDER BY $sortKey "

            if( !ascending ) {
                query += " DESC "
            }
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset"

        return db.query(query, storageMapper)
    }

    fun snIsRegistered(sn:String): Boolean {
        val s = db.queryForObject(QUERY_FOR_SN, storageMapper, sn)
        return s == null
    }

    fun ptIsRegistered(pt: String): Boolean {
        return pt == "11223344"
    }

    private val CREATE_ITEM_SQL = "INSERT INTO ITEMS(name) VALUES(?)"
    fun addItem(i:String) : Result<String> = tr.execute {

        try {

            db.update(CREATE_ITEM_SQL, i.uppercase())

            return@execute Result(i.uppercase())
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("StorageService::addItem: ${ex.message}")
            return@execute Result(null, "StorageService::addItem: ${ex.message}")
        }

    }!!

}