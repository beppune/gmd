package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderService(val db:JdbcTemplate, val ops:OperatorService, val dcs:DatacenterService, val sups:SupplierService) {

    val mapper = RowMapper { rs, _ ->
        Order(
            number = rs.getObject("id", UUID::class.java),
            op = ops.findAll().find { it.username == rs.getString("operator") }!!,
            dc = dcs.findAll(locations = false).find { it.short == rs.getString("datacenter") }!!,
            supplier = sups.findByName(rs.getString("supplier")),
            issued = rs.getDate("issued").toLocalDate(),
            type = Order.Type.valueOf(rs.getString("type")),
            subject = Order.Subject.valueOf(rs.getString("subject")),
            status = Order.Status.valueOf(rs.getString("status")),
        ).apply {
            ref = rs.getString("ref")
        }
    }

    private val REGISTER_ORDER_SQL =
        "INSERT INTO ORDERS(id,operator,datacenter,supplier,issued,type,subject,status,ref)" +
                "VALUES(?,?,?,?,?,?,?,?,?)"

    private val QUERY_ALL =
        "SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref FROM ORDERS"

    @Transactional
    fun register(o: Order) {
        db.update(
            REGISTER_ORDER_SQL,
            o.number,
            o.op.username,
            o.dc.short,
            o.supplier.name,
            o.issued,
            o.type.name,
            o.subject.name,
            o.status.name,
            o.ref
        )
    }

    fun findAll(): List<Order> {
        return db.query(QUERY_ALL, mapper)
    }

}
