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
    private val UNIQUE_MUST_BE_IN_STORAGE:OrderPredicate = {UNIQUE_MUST_NOT_BE_IN_STORAGE().not()}

    private val INTERNAL_AMEND:Order.()->Unit = {
        supplier = sups.findByName(config.firmName)
        lines.forEach { line ->
            if(line.isUnique) line.amount = 1
        }
    }

    val INBOUND_INTERNAL = OrderSpec()
        .amend(INTERNAL_AMEND)
        .define("UNIQUE_MUST_NOT_BE_IN_STORAGE", UNIQUE_MUST_NOT_BE_IN_STORAGE)

    val OUTBOUND_INTERNAL = OrderSpec()
        .amend(INTERNAL_AMEND)
        .define("UNIQUE_MUST_BE_IN_STORAGE", UNIQUE_MUST_BE_IN_STORAGE)



    fun run(order: Order): Pair<List<SpecBit>,List<SpecBit>> {

        if(order.type == Order.Type.INBOUND && order.subject == Order.Subject.INTERNAL){
            return INBOUND_INTERNAL.runSpec(order)
        }

        if(order.type == Order.Type.OUTBOUND && order.subject == Order.Subject.INTERNAL){
            return OUTBOUND_INTERNAL.runSpec(order)
        }

        throw RuntimeException("Unkown type/subject: $order")
    }
}