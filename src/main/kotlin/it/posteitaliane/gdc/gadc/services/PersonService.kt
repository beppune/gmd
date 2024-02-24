package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Person
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service

@Service
class PersonService(val db:JdbcTemplate) {

    private val mapper = RowMapper { rs, _ ->
        Person(
            rs.getInt("id"),
            rs.getString("lastname"),
            rs.getString("firstname")
        )
    }

    fun findAll() : List<Person> {
        return db.query("SELECT id,lastname,firstname FROM PERSONS", mapper)
    }

    fun find(offset:Int=0, limit:Int=Int.MAX_VALUE, ascending:Boolean=true, sortkey:String?, filter:String?): List<Person> {
        var query = "SELECT id,lastname,firstname FROM PERSONS "

        if(!filter.isNullOrEmpty()) {
            query += " WHERE lastname LIKE '%$filter%' OR firstname LIKE '%$filter%' "
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


    fun update(p: Person): Int {
        val affected = db.update("UPDATE PERSONS SET lastname=?, firstname=? WHERE id=?",
            p.lastName, p.firstName, p.uuid)
        return affected
    }

}