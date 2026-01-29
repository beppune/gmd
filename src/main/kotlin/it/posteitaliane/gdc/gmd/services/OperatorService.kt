package it.posteitaliane.gdc.gmd.services

import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Operator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class OperatorService(
    private val db:JdbcTemplate,
    private val tr:TransactionTemplate
) {

    companion object {
        val mapper = RowMapper { rs, _ ->
            Operator(
                rs.getString("uid"),
                rs.getString("lastname"),
                rs.getString("firstname"),
                rs.getString("email"),
                Operator.Role.valueOf(rs.getString("role")),
                rs.getBoolean("active"),
                rs.getString("localpassword")

            )
        }
    }

    private fun fetchPermissions(ops:MutableList<Operator>) {
        ops.forEach { op ->
            val perms = db.query(
                "SELECT shortname,fullname,legal FROM DCS JOIN PERMISSIONS ON shortname=dc WHERE operator = ?",
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
        val ops = db.query("SELECT uid,lastname,firstname,email,role,active,localpassword FROM OPERATORS WHERE uid = ? LIMIT 1", mapper, username)
        fetchPermissions( ops )
        return ops.first()
    }

    fun find(offset:Int=0, limit:Int=Int.MAX_VALUE, ascending:Boolean=true, sortkey:String?=null, filter:String?): List<Operator> {
        var query = "SELECT uid,lastname,firstname,email,role,active,localpassword FROM OPERATORS "

        if(!filter.isNullOrEmpty()) {
            query += " WHERE" +
                    " uid LIKE '%$filter%' OR " +
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

    private val CREATE_OPERATOR_SQL = "INSERT INTO OPERATORS(uid,lastname,firstname,email,role,active,localpassword)" +
            " VALUES(?,?,?,?,?,?,?)"
    private val ADD_OPERATOR_PERMISSION = "INSERT INTO PERMISSIONS(operator,dc) VALUES(?,?)"
    fun create(op:Operator) : Result<Operator> = tr.execute {

        try {
            db.update(
                CREATE_OPERATOR_SQL,
                op.username.uppercase(), op.lastName, op.firstName, op.email, op.role.name, op.isActive, op.localPassword
                )

            op.permissions.forEach { dc ->
                db.update(ADD_OPERATOR_PERMISSION, op.username.uppercase(), dc.short)
            }

            return@execute Result(op)
        } catch (ex:TransactionException) {
            it.setRollbackOnly()
            println("OperatorService::create: ${ex.message}")
            return@execute Result(null, "OperatorService::create: ${ex.message}")
        }

    }!!

    fun updateLocalPassword(op: Operator, passwd: String?) {
        val sql = "UPDATE operators SET localpassword = ? WHERE uid = ? ORDER BY uid LIMIT 1"
        db.update(sql, passwd, op.username)
    }

}