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
                legal = rs.getString("legal")
            )
        }
    }

    fun findAll(locations:Boolean=false) : List<Datacenter> {
        return db.query("SELECT shortname,fullname,legal FROM DCS", mapper)
            .apply {
                if(locations) {
                    forEach { dc ->
                        dc.locations.addAll( db.queryForList("SELECT name FROM LOCATIONS WHERE dc = ?", String::class.java, dc.short) )
                    }
                }
            }
    }

    fun findByShortName(short:String) : Datacenter? {
        return db.queryForObject("SELECT shortname,fullname,legal FROM DCS WHERE shortname LIKE ?", mapper, short)
    }

    private val CREATE_DATACENTER_SQL = "INSERT INTO DCS(shortname,fullname,legal) VALUES(?,?,?)"
    private val CREATE_LOCATIONS_SQL = "INSERT INTO LOCATIONS(dc,name) VALUES(?,?)"
    fun create(dc: Datacenter) : Result<Datacenter> = tr.execute {

        try {

            db.update(CREATE_DATACENTER_SQL, dc.short.uppercase(), dc.fullName, dc.legal)
            dc.locations.forEach {
                db.update(CREATE_LOCATIONS_SQL, dc.short.uppercase(), it)
            }

            return@execute Result(dc)
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            return@execute Result<Datacenter>(null,"DatacenterService::create: ${ex.message}")
        }
    }!!

}