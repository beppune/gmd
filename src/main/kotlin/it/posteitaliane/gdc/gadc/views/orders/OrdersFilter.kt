package it.posteitaliane.gdc.gadc.views.orders

import it.posteitaliane.gdc.gadc.model.Datacenter

data class OrdersFilter(var searchKery:String?=null) {
    val dcs:MutableList<Datacenter> = mutableListOf()
}
