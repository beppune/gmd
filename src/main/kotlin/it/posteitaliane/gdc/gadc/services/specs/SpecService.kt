package it.posteitaliane.gdc.gadc.services.specs

import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.services.SupplierService
import org.springframework.stereotype.Service

@Service
class SpecService(
    private val config:GMDConfig,

    private val sups:SupplierService,
    private val ss:StorageService
) {

    private val UNIQUE_MUST_NOT_BE_IN_STORAGE: OrderPredicate = {
        lines.all {
            ( ss.findBySn(it.sn) ?: ss.findByPt(it.pt) ) == null
        }
    }
    private val UNIQUE_MUST_BE_IN_STORAGE:OrderPredicate = {
        lines.all {
            if( it.isUnique ) {
                (ss.findBySn(it.sn) ?: ss.findByPt(it.pt)) != null
            }
            true
        }
    }

    private val AMEND_UNIQUE_AMOUNT:Order.()->Unit = {
        lines.forEach { line ->
            if(line.isUnique) line.amount = 1
        }
    }

    private val AMEND_INTERNAL_SUPPLIER:Order.()->Unit = {
        supplier = sups.findByName(config.firmName)
    }

    val INBOUND_INTERNAL_SPEC = OrderSpec()
        .amend(AMEND_INTERNAL_SUPPLIER)
        .amend(AMEND_UNIQUE_AMOUNT)
        .define("UNIQUE_MUST_NOT_BE_IN_STORAGE", UNIQUE_MUST_NOT_BE_IN_STORAGE)

    val OUTBOUND_INTERNAL_SPEC = OrderSpec()
        .amend(AMEND_INTERNAL_SUPPLIER)
        .amend(AMEND_UNIQUE_AMOUNT)
        .define("UNIQUE_MUST_BE_IN_STORAGE", UNIQUE_MUST_BE_IN_STORAGE)

    val INBOUND_SUPPLIER_SPEC = OrderSpec()
        .amend(AMEND_UNIQUE_AMOUNT)
        .define("UNIQUE_MUST_NOT_BE_IN_STORAGE", UNIQUE_MUST_NOT_BE_IN_STORAGE)

    val OUTBOUND_SUPPLIER_SPEC = OrderSpec()
        .amend(AMEND_UNIQUE_AMOUNT)
        .define("UNIQUE_MUST_BE_IN_STORAGE", UNIQUE_MUST_BE_IN_STORAGE)

    fun run(order: Order): Pair<List<SpecBit>,List<SpecBit>> {

        if(order.type == Order.Type.INBOUND && order.subject == Order.Subject.INTERNAL){
            return INBOUND_INTERNAL_SPEC.runSpec(order)
        }

        if(order.type == Order.Type.OUTBOUND && order.subject == Order.Subject.INTERNAL){
            return OUTBOUND_INTERNAL_SPEC.runSpec(order)
        }

        if(order.type == Order.Type.INBOUND && order.subject == Order.Subject.SUPPLIER){
            return INBOUND_SUPPLIER_SPEC.runSpec(order)
        }

        if(order.type == Order.Type.OUTBOUND && order.subject == Order.Subject.SUPPLIER){
            return OUTBOUND_SUPPLIER_SPEC.runSpec(order)
        }

        throw RuntimeException("Unknown type/subject: $order")
    }
}