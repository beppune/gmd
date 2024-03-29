package it.posteitaliane.gdc.gadc.views.transactions

import it.posteitaliane.gdc.gadc.model.Datacenter
import java.time.LocalDateTime

data class TransactionFilter(
    var from:LocalDateTime?=null,
    var to:LocalDateTime?=null,
    var dc:Datacenter?=null
)