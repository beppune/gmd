package it.posteitaliane.gdc.gadc.model

import java.time.LocalDateTime

data class Datacenter(
    val short:String,
    val fullName:String,
    val legal: String
    ) {


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
    var role:Role,
    var isActive:Boolean,
    var localPassword:String?=null
) {
    enum class Role { OPERATOR, ADMIN }
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

    val isAdmin = role == Role.ADMIN
    val isOperator = role == Role.ADMIN || role == Role.OPERATOR
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

        return name == other.name
    }

    override fun hashCode(): Int {
        return piva.hashCode()
    }

}

data class Order(
    var number:Int=-1,
    val op:Operator,
    val dc: Datacenter,
    var supplier:Supplier,
    val issued:LocalDateTime,
    val type: Type,
    val subject: Subject,
    var status: Status=Status.PENDING
) {
    val lines:MutableList<OrderLine> = mutableListOf()

    var ref:String = "${op.firstName} ${op.lastName}"

    var remarks:String?=null

    var filepath:String?=null

    enum class Type { INBOUND, OUTBOUND }
    enum class Subject { SUPPLIER, SUPPLIER_DC, INTERNAL  }
    enum class Status { PENDING, COMPLETED, CANCELED }
}

data class OrderLine(
    var order:Order,
    var item:String,
    var amount:Int,
    var position:String,
    var sn:String?=null,
    var pt:String?=null
) {
    val isUnique:Boolean get() = sn.isNullOrEmpty().not() || pt.isNullOrEmpty().not()
}

data class Storage(
    val item:String,
    val dc:Datacenter,
    val pos:String,

    var amount:Int,

    var sn:String?=null,
    var pt:String?=null
)

data class Transaction(
    val id:Int,
    val operator:String,
    val type:String,
    val timestamp:LocalDateTime,
    val item:String,
    val dc:String,
    val pos:String,
    val amount:Int,
    val sn:String,
    val pt:String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}

data class Shipping(
    val number:String?=null,
    val order:Order,
    val issued:LocalDateTime,
    val motive:String,
    val hauler:String,
    val address:String,
    val filepath: String?=null,
    val numpack:Int=1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Shipping

        return order == other.order
    }

    override fun hashCode(): Int {
        return order.hashCode()
    }
}


