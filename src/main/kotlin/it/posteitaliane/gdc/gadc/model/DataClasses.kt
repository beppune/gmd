package it.posteitaliane.gdc.gadc.model

import java.time.LocalDate

data class Datacenter(
    val short:String,
    val fullName:String) {
        var locations: MutableList<String> = mutableListOf()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Datacenter

        return short == other.short
    }

    override fun hashCode(): Int {
        return short.hashCode()
    }


}

data class Operator(
    val username:String,
    var lastName:String,
    var firstName:String,
    var email:String,
    var role:String,
    var isActive:Boolean,
    var localPassword:String?=null
) {
    val permissions:MutableList<Datacenter> = mutableListOf()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Operator

        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}

data class Order(
    val op:String,
    val dc: Datacenter,
    val issued:LocalDate,
    val type: Type,
    val subject: Subject
) {
    //val lines:MutableList<OrderLine> = mutableListOf()

    var project:String?=null
    var ref:String = op

    enum class Type { INBOUND, OUTBOUND }
    enum class Subject { SUPPLIER, SUPPLIER_DC, INTERNAL  }
}
/*data class OrderLine(
    var item:String,
    var amount:Int,
    var position:String,
    var sn:String?=null,
    var pt:String?=null
)*/

/*data class Storage(
    val item:String,
    val dc:Datacenter,
    val pos:String,

    var amount:Int,

    var sn:String?=null,
    var pt:String?=null
)*/