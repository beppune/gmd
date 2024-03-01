package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Order

class OrderForm : FormLayout() {

    private val binder:Binder<OrderPresentation>

    private var bean:OrderPresentation

    private var typeField:Select<Order.Type>
    private var subjectField:Select<Order.Subject>

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
            .bind({it.type}, { order, type -> order.type = type})

        subjectField = Select<Order.Subject>()
            .apply {
                setItems(Order.Subject.values().asList())
                setItemLabelGenerator {
                    when(it) {
                        Order.Subject.INTERNAL -> "INTERNO"
                        Order.Subject.SUPPLIER -> "FORNITORE"
                        Order.Subject.SUPPLIER_DC -> "MOVING"
                    }
                }
            }

        binder.forField(subjectField)
            .asRequired("Obbligatorio")
            .bind({it.subject}, { order, subject -> order.subject = subject})

        binder.readBean(bean)

        add(typeField, subjectField)

    }

}