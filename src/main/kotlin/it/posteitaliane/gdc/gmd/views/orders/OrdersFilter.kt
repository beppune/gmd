package it.posteitaliane.gdc.gmd.views.orders

import it.posteitaliane.gdc.gmd.model.Datacenter

data class OrdersFilter(var searchKery:String?=null) {
    val dcs:MutableList<Datacenter> = mutableListOf()
}
