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
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gadc.model.*
import it.posteitaliane.gdc.gadc.services.BackOffice
import java.time.LocalDate

data class OrderPresentation(
    var operator: Operator?=null,
    var type:Order.Type?=null,
    var subject:Order.Subject?=null,
    var ref:String?=null,
    var supplier:Supplier?=null,
    var datacenter:Datacenter?=null
)

class OrderForm(
    private val bo: BackOffice,
    private val defaultFirmName: String,
    type: Order.Type? = null
) : FormLayout() {

    var isValid: Boolean = false

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

    private val addLineButton:Button

    private val formSn = mutableListOf<String>()
    private val formPt = mutableListOf<String>()

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
                setItems(bo.dcs.findAll(true))
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

        dcSelect.addValueChangeListener {
            if( linesContainer.isVisible ) {
                linesContainer.children.forEach { hr ->
                    if( hr is HorizontalLayout ) {
                        val form:OrderLineForm = hr.children.findFirst().get() as OrderLineForm
                        form.reset(it.value, skipItem = true)
                    }
                }
            }
        }

        binder.bean = bean

        add(
            typeField, subjectField,
            dcSelect, supplierField,
            refField, Span(),
            HorizontalLayout(itemsButton, cancelItemsButton)
        )
        add(VerticalLayout(linesContainer), 2)

        addLineButton = Button("AGGIUNGI")
            .apply {
                addClickListener { linesContainer.add(makeLineForm()) }
                isVisible = false
            }


        add(addLineButton, 1)

        if( type != null ) {
            typeField.value = type
        }

        reset(type)

    }

    fun reset(type: Order.Type?=null) {
        undisplayLines(true)
        bean = OrderPresentation()
        bean.type = type
        binder.readBean(bean)
    }

    // Make OrderLinesPresentation forms list based on OrderPresentation values
    fun displayLines(/*lines:List<OrderLinePresentation>*/) {

        val line = makeLineForm()

        linesContainer.isVisible = true
        linesContainer.add(line)
        addLineButton.isVisible = true

    }

    fun undisplayLines(remove:Boolean=false) {
        if( remove ) {
            linesContainer.children.filter { it is HorizontalLayout }.forEach { linesContainer.remove(it) }
        }

        linesContainer.isVisible = false
        addLineButton.isVisible = false
    }

    private fun makeLineForm(): HorizontalLayout {
        val hr = HorizontalLayout()
        val line = OrderLineForm(this, bo.os.findItems(), dcSelect.value.locations)
        val button = Button(Icon(VaadinIcon.MINUS))

        hr.add(line, button)

        button.addClickListener {
            linesContainer.remove(hr)
        }

        return hr
    }

    fun linesForms() = linesContainer.children.filter { it is HorizontalLayout }
        .map { (it as HorizontalLayout).children.findFirst().get() as OrderLineForm }

    fun validate() : Boolean {

        var lines = linesForms()
            .allMatch { it.validate(); it.binder.isValid }
        isValid = lines && binder.validate().isOk
        return  isValid
    }

    fun compileOrder() : Order {

        binder.writeBean(bean)

        var order = Order(
            type = bean.type!!,
            op = bo.ops.findAll().first(),
            issued = LocalDate.now(),
            dc = bean.datacenter!!,
            subject = bean.subject!!,
            supplier = bean.supplier!!,
            status = Order.Status.PENDING
        )

        linesForms().forEach {
            it.validate()
            var line = OrderLine(
                order = order,
                item = it.bean.item!!,
                position = it.bean.position!!,
                amount = it.bean.amount!!,
                sn = it.bean.sn,
                pt = it.bean.pt
            )

            order.lines.add(line)
        }

        return order
    }
}