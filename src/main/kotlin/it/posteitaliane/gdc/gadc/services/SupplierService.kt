package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Supplier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service

@Service
class SupplierService(val db:JdbcTemplate) {

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

}