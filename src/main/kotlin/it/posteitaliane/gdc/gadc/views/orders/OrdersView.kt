package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.services.BackOffice

@Route("orders")
class OrdersView(val bo:BackOffice) : VerticalLayout() {

    init {


        val orders = mutableListOf(
            bo.from(bo.op.findAll().first())
                .place {
                    receiveFromDc(bo.dcs.findAll().first())
                }.build(),

            bo.from(bo.op.findAll().first())
                .place {
                    sendToDc(bo.dcs.findAll().first())
                }.build(),

            bo.from(bo.op.findAll().first())
                .place {
                    receiveFromSupplier(bo.sups.findAll().first())


                }
        )

        orders.forEach {
            add(Div(it.toString()))
        }
    }

}