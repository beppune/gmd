package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.views.forms.OrderLineForm2
import it.posteitaliane.gdc.gadc.views.forms.OrderPresentation
import jakarta.annotation.security.RolesAllowed


@Route("test2")
@RolesAllowed("ADMIN")
class TestView(
    private val ss: StorageService,
    private val os:OrderService,
    private val dcs:DatacenterService
) : Div() {


    init {

        val order = os.findAll(true).first()
            .run {
                OrderPresentation(
                    number = number,
                    operator = op,
                    type = type,
                    subject = subject,
                    supplier = supplier,
                    datacenter = dc
                )
            }

        val form = OrderLineForm2(order, ss, dcs)
        val typeField = Select<Order.Type>().apply {
                setItems(Order.Type.values().toList())
                setItemLabelGenerator { it.name.uppercase() }
                value = Order.Type.INBOUND

                addValueChangeListener {
                    order.type = it.value
                    form.setItemsByType(it.value)
                    form.setSnListByType()
                    form.setPtListByType()
                }
        }

        val resetButton = Button(Icon(VaadinIcon.ERASER))
            .apply {
                addClickListener {
                    form.validate()
                }
            }

        val hl = HorizontalLayout(
            typeField
        )

        add(hl)
        add(HorizontalLayout(form, resetButton))

    }

}
