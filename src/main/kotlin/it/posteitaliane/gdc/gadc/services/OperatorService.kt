package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Operator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service

@Service
class OperatorService(val db:JdbcTemplate) {

    private val mapper = RowMapper { rs, _ ->
        Operator(
            rs.getString("uid"),
            rs.getString("lastname"),
            rs.getString("firstname"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getBoolean("active"),
            rs.getString("localpassword")

        )
    }

    fun findAll() : List<Operator> {
        return db.query("SELECT uid,lastname,firstname,email,role,active,localpassword FROM OPERATORS", mapper)
    }

    fun find(offset:Int=0, limit:Int=Int.MAX_VALUE, ascending:Boolean=true, sortkey:String?, filter:String?): List<Operator> {
        var query = "SELECT uid,lastname,firstname,email,role,active,localpassword FROM OPERATORS "

        if(!filter.isNullOrEmpty()) {
            query += " WHERE" +
                    " lastname LIKE '%$filter%' OR " +
                    " firstname LIKE '%$filter%' OR " +
                    " email LIKE '%$filter%'"
        }

        if(!sortkey.isNullOrEmpty()) {
            query += " ORDER BY $sortkey "
        }

        if( ascending.not() ) {
            query += " DESC "
        }

        query += " LIMIT $limit "

        query += " OFFSET $offset "

        println(query)

        return db.query(query, mapper)
    }


    fun update(p: Operator): Int {
        val affected = db.update("UPDATE OPERATORS SET lastname=?, firstname=?, email=?, active=? WHERE uid=?",
            p.lastName, p.firstName, p.email, p.isActive, p.username)
        return affected
    }

}