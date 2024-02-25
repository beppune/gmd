package it.posteitaliane.gdc.gadc.model

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