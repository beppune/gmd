package it.posteitaliane.gdc.gadc.views.transactions

import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import java.time.LocalDateTime

data class TransactionFilter(
    var from:LocalDateTime?=null,
    var to:LocalDateTime?=null,
    var dc:Datacenter?=null,
    var operators: MutableList<Operator> = mutableListOf(),
    var type: String?=null,
    var position: String?=null,
    var item: String?=null,
)