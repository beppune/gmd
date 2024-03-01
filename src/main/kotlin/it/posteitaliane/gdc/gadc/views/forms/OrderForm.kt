package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Order

class OrderForm : FormLayout() {

    private val binder:Binder<OrderPresentation>

    private var bean:OrderPresentation

    private var typeField:Select<Order.Type>

    init {
        bean = OrderPresentation()

        binder = Binder(OrderPresentation::class.java, false)

        typeField = Select<Order.Type>()
            .apply {
                setItems(Order.Type.values().asList())
                setItemLabelGenerator {
                    when(it) {
                        Order.Type.INBOUND -> "CARICO"
                        Order.Type.OUTBOUND -> "SCARICO"
                    }
                }

            }

        binder.forField(typeField)
            .asRequired("Obbligatorio")
            .bind({it.type}, {order, type -> order.type = type})

        binder.readBean(bean)

        add(typeField)

    }

}