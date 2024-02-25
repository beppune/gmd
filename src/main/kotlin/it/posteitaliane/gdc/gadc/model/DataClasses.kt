package it.posteitaliane.gdc.gadc.model

import java.time.LocalDate
import java.util.*

data class Datacenter(
    val short:String,
    val fullName:String,
    val locations: List<String>)

data class Person(
    val uuid: Int,
    var lastName:String,
    var firstName:String


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        return uuid == other.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}

data class IDPaper(
    var type: IdType,
    var number:String,
    var expiration:LocalDate?=null,
    var owner:UUID
) {
    enum class IdType { CI, PASS, PA }
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