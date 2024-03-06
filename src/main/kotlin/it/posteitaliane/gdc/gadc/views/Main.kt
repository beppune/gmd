package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderForm

@Route
class Main(bo:BackOffice, config:GMDConfig) : VerticalLayout() {

    init {

        val order = OrderForm(
                bo.os,
                bo.dcs.findAll(true),
                bo.sups.findAll(),
                config
            )
        add(order)
    }
}