package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.formlayout.FormLayout
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier

data class OrderPresentation(
    var operator:Operator?=null,
    var type:Order.Type?=null,
    var subject:Order.Subject?=null,
    var ref:String?=null,
    var supplier:Supplier?=null,
    var datacenter:Datacenter?=null
)