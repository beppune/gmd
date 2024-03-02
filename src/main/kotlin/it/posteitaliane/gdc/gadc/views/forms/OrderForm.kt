package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier

class OrderForm(private val dcs:List<Datacenter>, private val sups:List<Supplier>, private val config:GMDConfig) : FormLayout() {

    private val binder:Binder<OrderPresentation>

    private var bean:OrderPresentation

    private var typeField:Select<Order.Type>

    private var subjectField:Select<Order.Subject>

    private var dcSelect:Select<Datacenter>

    private var refField:TextField

    private var supplierField:ComboBox<Supplier>

    private var cancelItemsButton: Button

    private var itemsButton:Button

    private var itemsContainer:VerticalLayout

    init {
        bean = OrderPresentation()

        binder = Binder(OrderPresentation::class.java, false)

        typeField = Select<Order.Type>()
            .apply {
                placeholder = "CARICO / SCARICO"
                setItems(Order.Type.entries)
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
                placeholder = "TIPO DI ORDINE"
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

        dcSelect = Select<Datacenter>()
            .apply {
                placeholder = "DATACENTER"
                setItems(dcs)
                setItemLabelGenerator {
                    "${it.short} - ${it.fullName}"
                }
            }

        binder.forField(dcSelect)
            .asRequired("Obbligatorio")
            .bind({it.datacenter},{order, dc -> order.datacenter = dc})

        refField = TextField()
            .apply {
                placeholder = "REFERENTE"
            }

        binder.forField(refField)
            .bind({it.ref}, { order, ref -> order.ref = ref })

        supplierField = ComboBox<Supplier>()
            .apply {
                placeholder = "FORNITORE"
                setItems(sups)
                setItemLabelGenerator {it.name}
            }

        binder.forField(supplierField)
            .bind({it.supplier}, { order, sup -> order.supplier = sup})

        binder.readBean(bean)

        subjectField.addValueChangeListener {
            if( it.source.value == Order.Subject.INTERNAL || it.source.value == Order.Subject.SUPPLIER_DC ) {
                supplierField.value = sups.find { it.name == config.firmName }
                supplierField.isEnabled = false
            } else {
                supplierField.value = null
                supplierField.isEnabled = true

            }
        }

        itemsButton = Button("MERCI")
            .apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
                addClickListener { toggleItems() }
            }

        cancelItemsButton = Button("ANNULLA")
            .apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR)
                isEnabled = false
                addClickListener {
                    toggleItems()
                    cancelItems()
                }
            }

        itemsContainer = VerticalLayout().apply { isVisible = false }

        add(
            typeField, subjectField,
            dcSelect, supplierField,
            refField, Span(),
            HorizontalLayout(itemsButton, cancelItemsButton)

        )
        add(itemsContainer, 2)

    }

    fun toggleItems() {
        if( !itemsContainer.isVisible ) {
            itemsContainer.isVisible = true
            itemsButton.isEnabled = false
            cancelItemsButton.isEnabled = true
        } else {
            cancelItemsButton.isEnabled = false
            itemsButton.isEnabled = true
            itemsContainer.isVisible = false
        }
    }

    fun cancelItems() {
        itemsContainer.removeAll()
    }

    fun writeBean() {
        try {
            binder.writeBean(bean)
        } catch (ex:ValidationException) {
            println(ex)
        }
    }

}