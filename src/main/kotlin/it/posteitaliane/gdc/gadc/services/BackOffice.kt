package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BackOffice(
    val ss:StorageService,
    val os:OrderService,
    val dcs:DatacenterService,
    val ops:OperatorService,
    val sups:SupplierService,
    val trs:TransactionsService,
    val config:GMDConfig
) {

    class OrderBuilder(val op:Operator, val bo:BackOffice) {

        lateinit var dc:Datacenter
        lateinit var subject:Order.Subject
        lateinit var type:Order.Type
        lateinit var supplier:Supplier
        var ref:String?=null

        fun build() : Order {
            val o = Order(
                op = op,
                dc = dc,
                supplier = supplier,
                type = type,
                subject = subject,
                issued = LocalDateTime.now()
            )

            if(ref.isNullOrEmpty().not()) o.ref = ref as String

            return o
        }


        fun receiveFromDc(datacenter:Datacenter) {

            subject = Order.Subject.INTERNAL
            type = Order.Type.INBOUND
            dc = datacenter
            supplier = bo.sups.findByName(bo.config.firmName)!!
        }

        fun sendToDc(datacenter:Datacenter) {

            subject = Order.Subject.INTERNAL
            type = Order.Type.OUTBOUND
            dc = datacenter
        }

        fun onBehalfOf(r:String) {
            ref = r
        }

        fun place(builder:OrderBuilder.()->Unit) : Order {
            builder(this)

            return build()
        }

    }

    fun from(op:Operator)  = OrderBuilder(op, this)

}

