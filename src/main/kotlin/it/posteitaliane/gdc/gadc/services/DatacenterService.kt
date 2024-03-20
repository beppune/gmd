package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Datacenter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service

@Service
class DatacenterService(val db:JdbcTemplate) {

    companion object {

        val mapper = RowMapper { rs, _ ->
            Datacenter(
                short = rs.getString("shortname"),
                fullName = rs.getString("fullname")
            )
        }
    }

    fun findAll(locations:Boolean=false) : List<Datacenter> {
        return db.query("SELECT shortname,fullname FROM DCS", mapper)
            .apply {
                if(locations) {
                    forEach { dc ->
                        dc.locations.addAll( db.queryForList("SELECT name FROM LOCATIONS WHERE dc = ?", String::class.java, dc.short) )
                    }
                }
            }
    }

    fun findByShortName(short:String) : Datacenter? {
        return db.queryForObject("SELECT shortname,fullname FROM DCS WHERE shortname LIKE ?", mapper, short)
    }

}