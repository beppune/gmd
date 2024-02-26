package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service

@Service
class OperatorService(val db:JdbcTemplate) {

    companion object {
        val mapper = RowMapper { rs, _ ->
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
    }

    private fun fetchPermissions(ops:MutableList<Operator>) {
        ops.forEach { op ->
            val perms = db.query(
                "SELECT shortname, fullname FROM DCS JOIN PERMISSIONS ON shortname=dc WHERE operator = ?",
                DatacenterService.mapper,
                op.username
            )
            op.permissions.addAll( perms )
        }
    }

    fun findAll() : List<Operator> {
        return db.query("SELECT uid,lastname,firstname,email,role,active,localpassword FROM OPERATORS", mapper)
            .apply { fetchPermissions(this) }

    }

    fun get(username:String) : Operator {
        val ops = db.query("SELECT uid,lastname,firstnaem,email,role,active FROM OPERATORS WHERE uid = ? LIMIT 1", mapper, username)
        fetchPermissions( ops )
        return ops.first()
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

        return db.query(query, mapper)
            .apply { fetchPermissions(this) }
    }


    fun update(p: Operator): Int {
        val affected = db.update("UPDATE OPERATORS SET lastname=?, firstname=?, email=?, active=? WHERE uid=?",
            p.lastName, p.firstName, p.email, p.isActive, p.username)
        return affected
    }

    fun updatePermissions(op: Operator, permissions: List<Datacenter>): Int {
        db.update("DELETE FROM PERMISSIONS WHERE operator = ?", op.username)
        var affected = 0
        permissions.forEach { dc ->
            affected += db.update("INSERT INTO PERMISSIONS(operator,dc) VALUES(?,?)", op.username, dc.short)
        }
        return affected
    }

}