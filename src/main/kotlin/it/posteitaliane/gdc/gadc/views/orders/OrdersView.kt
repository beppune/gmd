package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.services.BackOffice

@Route("orders")
class OrdersView(val BO:BackOffice) : VerticalLayout() {

    init {

        var operator = BO.ops.findAll().first()
        var datacenter = BO.dcs.findAll().first()

        var o = BO.from(operator)
            .place {

                receiveFromDc(datacenter)


            }

        val button = Button("Test")  {
            BO.register(o)
        }

        add(button)
    }

}