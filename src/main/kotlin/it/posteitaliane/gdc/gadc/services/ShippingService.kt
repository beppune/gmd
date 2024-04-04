package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Shipping
import it.posteitaliane.gdc.gadc.services.specs.SpecBit
import it.posteitaliane.gdc.gadc.services.specs.SpecService
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShippingService(
    private val db:JdbcTemplate,
    private val specs:SpecService,
    private val os:OrderService
) {

    val mapper = RowMapper { rs, _ ->
        Shipping(
            rs.getString("number"),
            os.findByOrderId( rs.getInt("ownedby") ),
            rs.getTimestamp("issued").toLocalDateTime()
        )
    }

    private val QUERY_SHIPPING = "SELECT number, ownedby, issued FROM SHIPPINGS"

    fun findAll(): List<Shipping> {
        return db.query(QUERY_SHIPPING, mapper).toList()
    }

    //private val UPDATE_SHIPPING_QUERY = "UPDATE SHIPPING SET issued = ? WHERE ownedby = ? ORDER BY ownedby LIMIT 1"
    private val PREPARE_SHIPPING_QUERY = "INSERT INTO SHIPPINGS(ownedby,issued) VALUES(?,?)"

    fun prepareShipping(o:Order) : Result<Shipping> {

        val (_, no) = specs.run(o, specs.ORDER_TO_SHIPPING_SPEC)

        if(no.isNotEmpty()) {
            no.forEach(::println)
            return Result(null, "ShippingService::prepare: Spec failed")
        }

        try{

            val sh = Shipping(null, o, LocalDateTime.now())

            db.update(PREPARE_SHIPPING_QUERY, o.number, LocalDateTime.now())

            return Result(sh)
        }catch (ex:DataAccessException) {
            println(ex.message)

            return Result(null, ex.message)
        }


    }

}