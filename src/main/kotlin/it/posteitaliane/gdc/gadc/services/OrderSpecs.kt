package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Order

typealias OrderPredicate = Order.()->Boolean
typealias OrderBlock = Order.()->Unit

private data class SpecBit(val name:String, val predicate:OrderPredicate, var result:Boolean = false)

class OrderSpec {


    private val list = mutableListOf<SpecBit>()
    private var block: OrderBlock = {}

    val specs get() = list.associate { it.name to it.result }

    fun define(name:String, spec:OrderPredicate ): OrderSpec {
        list.add( SpecBit(name, spec) )
        return this
    }

    fun define(p:OrderBlock): OrderSpec {
        block = p
        return this
    }

    fun validate(order: Order): Boolean {
        block.invoke(order)
        list.forEach { bit -> bit.result = bit.predicate(order) }

        return list.all { bit -> bit.result }
    }

    fun setup(order: Order) {
        block(order)
    }

}