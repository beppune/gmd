package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BackOffice(
    val dcs:DatacenterService,
    val op:OperatorService
) {

    class OrderBuilder(val op:Operator) {

        lateinit var dc:Datacenter
        lateinit var subject:Order.Subject
        lateinit var type:Order.Type

        fun build() : Order {
            val o = Order(
                op = op.username,
                dc = dc,
                type = type,
                subject = subject,
                issued = LocalDate.now()
            )
            return o
        }

        fun place(builder:OrderBuilder.()->Unit) : OrderBuilder {

            builder(this)
            //validate() //may throw permission exception

            return this
        }

        fun receiveFromDc(datacenter:Datacenter) {

            subject = Order.Subject.INTERNAL
            type = Order.Type.INBOUND
            dc = datacenter
        }

        fun sendToDc(datacenter:Datacenter) {

            subject = Order.Subject.INTERNAL
            type = Order.Type.OUTBOUND
            dc = datacenter
        }

    }

    fun from(op:Operator) = OrderBuilder(op)

}

/*
    BackOffice().from(operator).place {

        inbound(dc)

        supplier("IBM")


    }
 */