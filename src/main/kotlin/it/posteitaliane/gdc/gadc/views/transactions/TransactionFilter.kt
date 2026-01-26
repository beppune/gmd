package it.posteitaliane.gdc.gadc.views.transactions

import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Transaction
import java.time.LocalDateTime
import java.util.Optional

data class TransactionFilter(
    var from:LocalDateTime?=null,
    var to:LocalDateTime?=null,
    var dc:Datacenter?=null,
    var operator: String?=null,
    var type: String?=null,
    var position: String?=null,
    var item: String?=null,
)