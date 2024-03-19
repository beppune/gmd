package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.services.StorageService

class OrderForm(
    private val bo:BackOffice,
    private val defaultFirmName:String) : FormLayout() {

    private val binder:Binder<OrderPresentation>

    private var bean:OrderPresentation

    private var typeField:Select<Order.Type>

    private var subjectField:Select<Order.Subject>

    private var dcSelect:Select<Datacenter>

    private var refField:TextField

    private var supplierField:ComboBox<Supplier>

    private var cancelItemsButton: Button

    private var itemsButton:Button

    private var linesContainer:VerticalLayout

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
                        Order.Subject.SUPPLIER -> "DA FORNITORE"
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
                setItemLabelGenerator {
                    "${it.short} - ${it.fullName}"
                }
                setItems(bo.dcs.findAll())
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
                placeholder = " DA FORNITORE"
                setItems(bo.sups.findAll())
                setItemLabelGenerator {it.name}
            }

        binder.forField(supplierField)
            .asRequired("Obbligatorio")
            .bind({it.supplier}, { order, sup -> order.supplier = sup})

        binder.readBean(bean)

        subjectField.addValueChangeListener {
            if( it.source.value == Order.Subject.INTERNAL || it.source.value == Order.Subject.SUPPLIER_DC ) {
                supplierField.value = bo.sups.findByName(defaultFirmName)
                supplierField.isEnabled = false
            } else {
                supplierField.value = null
                supplierField.isEnabled = true

            }
        }

        itemsButton = Button("MERCI")
            .apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
            }

        cancelItemsButton = Button("ANNULLA")
            .apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR)
                isEnabled = false
            }

        itemsButton.addClickListener {
            val res = binder.validate()
            if( res.isOk ) {
                displayLines()

                //update view
                it.source.isEnabled = false
                cancelItemsButton.isEnabled = true
            }
        }

        cancelItemsButton.addClickListener {
            undisplayLines(true)

            //update view
            it.source.isEnabled = false
            itemsButton.isEnabled = true
        }

        linesContainer = VerticalLayout().apply {
            isVisible = false
        }

        add(
            typeField, subjectField,
            dcSelect, supplierField,
            refField, Span(),
            HorizontalLayout(itemsButton, cancelItemsButton)
        )
        add(linesContainer, 2)

        val addLineButton = Button("AGGIUNGI") {
            add(makeLineForm())
        }

        add(addLineButton, 2)

    }

    // Make OrderLinesPresentation forms list based on OrderPresentation values
    fun displayLines(/*lines:List<OrderLinePresentation>*/) {

        val line = makeLineForm()

        linesContainer.isVisible = true
        linesContainer.add(line)

    }

    fun undisplayLines(remove:Boolean=false) {
        if( remove ) {
            linesContainer.children.filter { it is HorizontalLayout }.forEach { linesContainer.remove(it) }
        }

        linesContainer.isVisible = false
    }

    private fun makeLineForm(): HorizontalLayout {
        val hr = HorizontalLayout()
        val line = OrderLineForm(bo.os.findItems(), dcSelect.value.locations)
        line.snIsRegistered = {bo.ss.snIsRegistered(line.snField.value)}
        line.ptIsRegistered = {bo.ss.ptIsRegistered(line.ptField.value)}
        val button = Button(Icon(VaadinIcon.MINUS))

        hr.add(line, button)

        button.addClickListener {
            linesContainer.remove(hr)
        }

        return hr
    }
}