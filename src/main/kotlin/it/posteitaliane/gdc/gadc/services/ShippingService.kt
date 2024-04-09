package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Shipping
import it.posteitaliane.gdc.gadc.services.specs.SpecService
import org.springframework.context.annotation.Lazy
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service

@Service
class ShippingService(
    private val db:JdbcTemplate,
    private val specs:SpecService,
    @Lazy private val os:OrderService
) {

    val mapper = RowMapper { rs, _ ->
        Shipping(
            rs.getString("number"),
            os.findByOrderId( rs.getInt("ownedby") ),
            rs.getTimestamp("issued").toLocalDateTime(),
            rs.getString("motive"),
            rs.getString("hauler"),
            rs.getString("address"),
            rs.getString("filepath"),
            rs.getInt("numpack")
        )
    }

    private val QUERY_ORDER_SEARCH_KEY = """
        SELECT id,operator,datacenter,supplier,issued,type,subject,status,ref FROM ORDERS 
        JOIN OPERATORS ON uid=operator 
        JOIN DCS ON shortname=datacenter 
    """.trimIndent()

    private val QUERY_ORDER_BY_ID = """
        $QUERY_ORDER_SEARCH_KEY 
        WHERE id = ?
    """.trimIndent()

    private val QUERY_SHIPPING = "SELECT number,ownedby,issued,motive,hauler,address,numpack,filepath FROM SHIPPINGS"
    private val QUERY_SHIPPING_BY_ID = "$QUERY_SHIPPING WHERE ownedby = ? ORDER BY ownedby LIMIT 1"

    fun findByOrder(id:Int) : Shipping? {
        return db.query(QUERY_SHIPPING_BY_ID, mapper, id).firstOrNull()
    }

    fun findAll(): List<Shipping> {
        return db.query(QUERY_SHIPPING, mapper).toList()
    }

    private val UPDATE_SHIPPING_QUERY =
        "UPDATE SHIPPINGS SET issued = ?, motive = ?, hauler = ?, address = ?, filepath = ?, numpack = ?" +
        " WHERE ownedby = ? ORDER BY ownedby LIMIT 1"

    private val PREPARE_SHIPPING_QUERY = "INSERT INTO SHIPPINGS(ownedby,issued,motive,hauler,address,filepath,numpack) " +
            " VALUES(?,?,?,?,?,?,?)"

    fun prepareShipping(o:Order, sh:Shipping) : Result<Shipping> {

        val (_, no) = specs.run(o, specs.ORDER_TO_SHIPPING_SPEC)

        if(no.isNotEmpty()) {
            no.forEach(::println)
            return Result(null, "ShippingService::prepare: Spec failed")
        }

        try{

            val s = findByOrder(o.number)

            if( s == null ) {
                db.update(
                    PREPARE_SHIPPING_QUERY,
                    o.number, o.issued, sh.motive, sh.hauler, sh.address, sh.filepath, sh.number
                )
            } else {
                db.update(
                    UPDATE_SHIPPING_QUERY,
                    o.issued, sh.motive, sh.hauler, sh.address, sh.filepath, sh.number, o.number
                )
            }

            return Result(sh)
        } catch (ex:DataAccessException) {
            println("ShippingService::prepare: ${ex.message}")
            return Result(null, ex.message)
        }


    }

    private val QUERY_FINALIZE_SHIPPING =
        "UPDATE SHIPPINGS SET number = get_next_shipping_number(), filepath = ?, numpack = ? WHERE ownedby = ?"

    fun finalize(s: Shipping) : Result<Shipping>  {

        try {
            db.update(
                QUERY_FINALIZE_SHIPPING,
                "bill.pfd", s.numpack, s.order.number
            )

            val rs = findByOrder(s.order.number)

            return Result(rs)
        } catch (ex:DataAccessException) {
            return Result(null, "ShippingService::finalize: ${ex.message}")
        }

    }

}