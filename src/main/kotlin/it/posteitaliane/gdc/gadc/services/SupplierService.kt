package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Supplier
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.TransactionException
import org.springframework.transaction.support.TransactionTemplate

@Service
class SupplierService(val db:JdbcTemplate, val tr:TransactionTemplate) {

    companion object {
        val mapper = RowMapper { rs, _ ->
            Supplier(
                rs.getString("name"),
                rs.getString("legal"),
                rs.getString("piva")
            )
        }
    }

    private val QUERY_ALL = "SELECT name,legal,piva FROM SUPPLIERS"
    private val QUERY_BY_NAME = "SELECT name,legal,piva FROM SUPPLIERS WHERE name = ?"
    private val QUERY_SUPPLIER_ADDRESSES = "SELECT address FROM SUPPLIERS_ADDRESSES WHERE supplier = ?"

    private fun fillAddresses(s:Supplier) {
        val list = db.queryForList(QUERY_SUPPLIER_ADDRESSES, String::class.java, s.name)
        s.addresses.addAll(list)
    }
    fun findAll() : List<Supplier> {
        val list = db.query(QUERY_ALL, mapper)

        list.forEach { fillAddresses(it) }
        return list
    }

    fun findByName(name: String): Supplier {
        return db.queryForObject(QUERY_BY_NAME, mapper, name)!!
    }

    private val CREATE_SUPPLIER_SQL = "INSERT INTO SUPPLIERS(name,legal,piva) VALUES(?,?,?)"
    private val ADD_SUPPLIER_ADDRESS = "INSERT INTO SUPPLIERS_ADDRESSES(supplier,address) VALUES(?,?)"
    fun create(s:Supplier) : Result<Supplier> = tr.execute {
        try {

            db.update(CREATE_SUPPLIER_SQL, s.name.uppercase(), s.legal, s.piva)

            s.addresses.forEach { addr ->
                db.update(ADD_SUPPLIER_ADDRESS, s.name.uppercase(), addr)
            }

            return@execute Result(s)
        } catch (ex:TransactionException ) {
            it.setRollbackOnly()
            println("SupplierService::create: ${ex.message}")
            return@execute Result(null, "SupplierService::create ${ex.message}")
        }
    }!!

}