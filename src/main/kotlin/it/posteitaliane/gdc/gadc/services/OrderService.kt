package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(val db:JdbcTemplate) {

    private val REGISTER_ORDER_SQL =
        "INSERT INTO ORDERS(id,operator,datacenter,supplier,issued,type,subject,status,ref)" +
                "VALUES(?,?,?,?,?,?,?,?,?)"

    @Transactional
    fun register(o: Order) {
        println(o)
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

}
