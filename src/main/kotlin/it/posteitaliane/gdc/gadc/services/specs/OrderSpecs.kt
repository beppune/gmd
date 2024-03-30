package it.posteitaliane.gdc.gadc.services.specs

import it.posteitaliane.gdc.gadc.model.Order

typealias OrderPredicate = Order.()->Boolean
typealias OrderBlock = Order.()->Unit

data class SpecBit(
    val name:String,
    val orderPredicate: OrderPredicate,
    var result:Boolean = false
) {
    fun run(o:Order):Boolean {
        result = orderPredicate.invoke(o)
        return result
    }
}

class OrderSpec {


    private val list = mutableListOf<SpecBit>()
    private var amendBlock: OrderBlock = {}

    fun define(name:String, spec:OrderPredicate): OrderSpec {
        list.add( SpecBit(name, orderPredicate = spec) )
        return this
    }

    fun amend(p: OrderBlock): OrderSpec {
        amendBlock = p
        return this
    }

    fun runSpec(order: Order): Pair<List<SpecBit>,List<SpecBit>> {
        amendBlock.invoke(order)

        val ok = mutableListOf<SpecBit>()
        val notokk = mutableListOf<SpecBit>()

        list.forEach { bit ->
            if( bit.run(order) ) ok.add(bit)
            else notokk.add(bit)
        }

        return Pair(ok, notokk)
    }

}