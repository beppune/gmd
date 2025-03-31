package it.posteitaliane.gdc.gadc.views.storage

import it.posteitaliane.gdc.gadc.model.Datacenter

data class StorageFilter(
    var key:String?=null,
    var position:String?=null,
    var sn:String?=null,
    var pt:String?=null
) {
    val dcs:MutableList<Datacenter> = mutableListOf()
}
