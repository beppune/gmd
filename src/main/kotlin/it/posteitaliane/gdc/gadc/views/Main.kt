package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderLineForm

@Route
class Main(bo:BackOffice, config:GMDConfig) : VerticalLayout() {

    init {

        val items = bo.os.findItems()
        var list = bo.dcs.findAll(true)
        var dc = list.random();
        val form = OrderLineForm(items, dc.locations)
        val reset = Button("RESET") {form.reset()}
        val validate = Button("OK") {form.validate()}
        val changeDc = Select<Datacenter>()
            .apply {
                setItemLabelGenerator {it.fullName}
                setItems(list)
                addValueChangeListener {
                    form.reset(it.value, skipItem = true)
                }
            }

        changeDc.value = dc

        add(form, reset, validate, changeDc)
    }
}