package it.posteitaliane.gdc.gmd.views.storage

import it.posteitaliane.gdc.gmd.model.Datacenter

data class StorageFilter(
    var key:String?=null,
    var position:String?=null,
    var sn:String?=null,
    var pt:String?=null
) {
    val dcs:MutableList<Datacenter> = mutableListOf()
}
