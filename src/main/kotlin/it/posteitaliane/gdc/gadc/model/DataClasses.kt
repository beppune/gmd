package it.posteitaliane.gdc.gadc.model

import java.time.LocalDate
import java.util.*

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

data class Supplier(
    val name:String,
    val legal:String,
    val piva:String
) {
    val addresses:MutableList<String> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Supplier

        return piva == other.piva
    }

    override fun hashCode(): Int {
        return piva.hashCode()
    }

}

data class Order(
    val number:UUID = UUID.randomUUID(),
    val op:Operator,
    val dc: Datacenter,
    val supplier:Supplier,
    val issued:LocalDate,
    val type: Type,
    val subject: Subject,
    val status: Status=Status.OPENED
) {
    //val lines:MutableList<OrderLine> = mutableListOf()

    var ref:String = "${op.firstName} ${op.lastName}"

    enum class Type { INBOUND, OUTBOUND }
    enum class Subject { SUPPLIER, SUPPLIER_DC, INTERNAL  }
    enum class Status { OPENED, PENDING, COMPLETED, CANCELED }
}

/*data class OrderLine(
    var order:Order,
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