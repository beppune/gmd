package it.posteitaliane.gdc.gmd.views.storage

import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.Supplier
import java.time.LocalDate

data class StorageFilter(
    val dcs: MutableSet<String> = mutableSetOf(),
    val others: MutableSet<String> = mutableSetOf(),
    var items: MutableSet<String> = mutableSetOf(),
    var positions:MutableSet<String> = mutableSetOf(),
    var sn:String?=null,
    var pt:String?=null,
    var type: Order.Type?=null,
    val operators: MutableSet<String> = mutableSetOf(),
    var from: LocalDate?=null,
    var to: LocalDate?=null,
    var status: Order.Status?=null,
    var ref:String?=null,
    var supplier: Supplier?=null,

    var key:String?=null,

    var showOthers: Boolean=false,
)
