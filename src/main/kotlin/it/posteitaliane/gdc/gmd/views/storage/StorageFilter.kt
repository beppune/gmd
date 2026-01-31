package it.posteitaliane.gdc.gmd.views.storage

data class StorageFilter(
    var items: MutableSet<String> = mutableSetOf(),
    var key:String?=null,
    var positions:MutableSet<String> = mutableSetOf(),
    var sn:String?=null,
    var pt:String?=null,
    val dcs: MutableSet<String> = mutableSetOf(),
    val others: MutableSet<String> = mutableSetOf(),
    var showOthers: Boolean=false,
)
