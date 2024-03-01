package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderForm
import it.posteitaliane.gdc.gadc.views.forms.OrderLineForm

@Route
class Main(bo:BackOffice) : VerticalLayout() {

    init {

        val order = OrderForm()
        add(order)
    }
}