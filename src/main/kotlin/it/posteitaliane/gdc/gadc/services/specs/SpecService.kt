package it.posteitaliane.gdc.gadc.services.specs

import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.OrderLine
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

    private val AMEND_EMPTY_UNIQUE:Order.()->Unit = {
        lines.forEach { line ->
            if( line.sn.isNullOrEmpty() ) {
                line.sn = null
            }

            if(line.pt.isNullOrEmpty() ) {
                line.pt = null
            }
        }
    }

    private val NO_REPEATED_UNIQUES:OrderPredicate = {
        lines.map(OrderLine::sn).filter { it.isNullOrEmpty().not() }.groupBy { it }.all { it.value.size == 1 }
                && lines.map(OrderLine::pt).filter { it.isNullOrEmpty().not() }.groupBy { it }.all { it.value.size == 1 }
    }

    private val AMEND_INTERNAL_SUPPLIER:Order.()->Unit = {
        supplier = sups.findByName(config.firmName)
    }

    private val ITEM_MUST_NOT_BE_NULL_OR_EMPTY:OrderPredicate = {
        lines.map(OrderLine::item).all(String::isNotBlank)
    }

    val INBOUND_INTERNAL_SPEC = OrderSpec()
        .amend(AMEND_INTERNAL_SUPPLIER)
        .amend(AMEND_UNIQUE_AMOUNT)
        .amend(AMEND_EMPTY_UNIQUE)
        .define("ITEM_MUST_NOT_BE_NULL_OR_EMPTY", ITEM_MUST_NOT_BE_NULL_OR_EMPTY)
        .define("UNIQUE_MUST_NOT_BE_IN_STORAGE", UNIQUE_MUST_NOT_BE_IN_STORAGE)
        .define("NO_REPEATED_UNIQUES", NO_REPEATED_UNIQUES)

    val OUTBOUND_INTERNAL_SPEC = OrderSpec()
        .amend(AMEND_INTERNAL_SUPPLIER)
        .amend(AMEND_UNIQUE_AMOUNT)
        .amend(AMEND_EMPTY_UNIQUE)
        .define("ITEM_MUST_NOT_BE_NULL_OR_EMPTY", ITEM_MUST_NOT_BE_NULL_OR_EMPTY)
        .define("UNIQUE_MUST_BE_IN_STORAGE", UNIQUE_MUST_BE_IN_STORAGE)

    val INBOUND_SUPPLIER_SPEC = OrderSpec()
        .amend(AMEND_UNIQUE_AMOUNT)
        .amend(AMEND_EMPTY_UNIQUE)
        .define("ITEM_MUST_NOT_BE_NULL_OR_EMPTY", ITEM_MUST_NOT_BE_NULL_OR_EMPTY)
        .define("UNIQUE_MUST_NOT_BE_IN_STORAGE", UNIQUE_MUST_NOT_BE_IN_STORAGE)
        .define("NO_REPEATED_UNIQUES", NO_REPEATED_UNIQUES)

    val OUTBOUND_SUPPLIER_SPEC = OrderSpec()
        .amend(AMEND_UNIQUE_AMOUNT)
        .amend(AMEND_EMPTY_UNIQUE)
        .define("ITEM_MUST_NOT_BE_NULL_OR_EMPTY", ITEM_MUST_NOT_BE_NULL_OR_EMPTY)
        .define("UNIQUE_MUST_BE_IN_STORAGE", UNIQUE_MUST_BE_IN_STORAGE)

    val ORDER_TO_SHIPPING_SPEC = OrderSpec()
        .define("Must be of OUTBOUND SUPPLIER type") {type==Order.Type.OUTBOUND && subject==Order.Subject.SUPPLIER}

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

    fun run(order: Order, spec:OrderSpec) = spec.runSpec(order)
}