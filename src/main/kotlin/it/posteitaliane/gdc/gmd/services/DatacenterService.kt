package it.posteitaliane.gdc.gmd.services

import it.posteitaliane.gdc.gmd.model.Datacenter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class DatacenterService(
    private val db:JdbcTemplate,
    private val tr:TransactionTemplate
) {


    companion object {

        val mapper = RowMapper { rs, _ ->
            Datacenter(
                short = rs.getString("shortname"),
                fullName = rs.getString("fullname"),
                legal = rs.getString("legal"),
                operating = rs.getBoolean("active"),
            )
        }
    }

    fun findAll(locations:Boolean=false, onlyOperating: Boolean=true) : List<Datacenter> {
        var query = "SELECT shortname,fullname,legal,active FROM DCS "

        if (onlyOperating) {
            query += " WHERE active IS TRUE"
        }

        return db.query(query, mapper)
            .apply {
                if(locations) {
                    forEach { dc ->
                        dc.locations.addAll( db.queryForList("SELECT name FROM LOCATIONS WHERE dc = ?", String::class.java, dc.short) )
                    }
                }
            }
    }

    fun findByShortName(short:String) : Datacenter? {
        return db.queryForObject("SELECT shortname,fullname,legal,active FROM DCS WHERE shortname LIKE ?", mapper, short)
    }

    private val CREATE_DATACENTER_SQL = "INSERT INTO DCS(shortname,fullname,legal,active) VALUES(?,?,?,?)"
    private val CREATE_LOCATIONS_SQL = "INSERT INTO LOCATIONS(dc,name) VALUES(?,?)"
    fun create(dc: Datacenter) : Result<Datacenter> = tr.execute {

        try {

            db.update(CREATE_DATACENTER_SQL, dc.short.uppercase(), dc.fullName, dc.legal, "TRUE")
            dc.locations.forEach {
                db.update(CREATE_LOCATIONS_SQL, dc.short.uppercase(), it)
            }

            return@execute Result(dc)
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            return@execute Result<Datacenter>(null,"DatacenterService::create: ${ex.message}")
        }
    }!!

    fun findOthers(locations: Boolean=false): List<Datacenter> {
        var query = "SELECT shortname,fullname,legal,active FROM DCS WHERE active IS FALSE"

        return db.query(query, mapper)
            .apply {
                if(locations) {
                    forEach { dc ->
                        dc.locations.addAll( db.queryForList("SELECT name FROM LOCATIONS WHERE dc = ?", String::class.java, dc.short) )
                    }
                }
            }
    }

}