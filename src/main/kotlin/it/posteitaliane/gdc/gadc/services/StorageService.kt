package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Storage
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service

@Service
class StorageService(val db:JdbcTemplate, val dcs:DatacenterService) {

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

    private val QUERY_FOR_SN = "$QUERY_ALL WHERE sn = ?"

    fun findAll() : List<Storage> {
        return db.query(QUERY_ALL, storageMapper)
    }

    fun find(offset: Int, limit: Int, searchKey: String?) : List<Storage> {
        var query = QUERY_ALL

        if ( searchKey != null ) {
            query += " WHERE "
            query += " item ILIKE '$searchKey%'"
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

}