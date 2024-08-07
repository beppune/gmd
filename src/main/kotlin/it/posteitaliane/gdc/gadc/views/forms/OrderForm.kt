package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Supplier
import it.posteitaliane.gdc.gadc.services.*
import java.time.LocalDateTime
import java.util.*

data class OrderPresentation(
    var number: Int=-1,
    var operator: Operator?=null,
    var type:Order.Type?=null,
    var subject:Order.Subject?=null,
    var ref:String?=null,
    var supplier:Supplier?=null,
    var datacenter:Datacenter?=null,
    var remarks:String?=null
)

class OrderForm(
    private val defaultFirmName: String,
    private val dcs:DatacenterService,
    private val sups:SupplierService,
    private val os:OrderService,
    private val ss:StorageService,
    private val ops:OperatorService,
    private val files:FilesService,
    private val op:Operator,
    private var type: Order.Type? = null
) : FormLayout() {

    var isValid: Boolean = false

    val binder:Binder<OrderPresentation>

    private var bean:OrderPresentation

    private val numberField:IntegerField

    var typeField:Select<Order.Type>

    private var subjectField:Select<Order.Subject>

    private var dcSelect:Select<Datacenter>

    private var refField:TextField

    private var supplierField:ComboBox<Supplier>

    private var cancelItemsButton: Button

    private val addLineButton:Button

    private val optionPending:Checkbox

    private val fileUpload:Upload

    private val remarksText:TextArea

    private val lineContainer:VerticalLayout

    var savePath:String?=null

    init {

        bean = OrderPresentation()

        binder = Binder(OrderPresentation::class.java, false)

        numberField = IntegerField().apply { isVisible = false }
        binder.forField(numberField)
            .bind({op->op.number},{op,value->op.number=value})

        typeField = Select<Order.Type>()
            .apply {
                placeholder = "CARICO / SCARICO"
                setItems(Order.Type.entries)
                value = type
                setItemLabelGenerator {
                    when(it) {
                        Order.Type.INBOUND -> "CARICO"
                        Order.Type.OUTBOUND -> "SCARICO"
                        else -> null
                    }
                }

            }

        binder.forField(typeField)
            .asRequired("Obbligatorio")
            .bind({it.type}, { order, value -> order.type = value})

        subjectField = Select<Order.Subject>()
            .apply {
                placeholder = "TIPO DI ORDINE"
                setItems(Order.Subject.INTERNAL, Order.Subject.SUPPLIER)
                setItemLabelGenerator {
                    when(it) {
                        Order.Subject.INTERNAL -> "INTERNO"
                        Order.Subject.SUPPLIER -> "DA FORNITORE"
                        Order.Subject.SUPPLIER_DC -> "MOVING"
                        else -> null
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
                setItems(dcs.findAll(true).filter { op.permissions.contains(it) })
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
                setItems(sups.findAll())
                setItemLabelGenerator {it.name}
            }

        binder.forField(supplierField)
            .asRequired("Obbligatorio")
            .bind({it.supplier}, { order, sup -> order.supplier = sup})

        subjectField.addValueChangeListener {
            if( it.source.value == Order.Subject.INTERNAL || it.source.value == Order.Subject.SUPPLIER_DC ) {
                supplierField.value = sups.findByName(defaultFirmName)
                supplierField.isEnabled = false
            } else {
                supplierField.value = null
                supplierField.isEnabled = true

            }
        }

        cancelItemsButton = Button("ANNULLA")
            .apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR)
                isEnabled = false
            }


        cancelItemsButton.addClickListener {

            //update view
            it.source.isEnabled = false
        }


        fileUpload = Upload(MemoryBuffer()).apply {
            addClassNames(
                LumoUtility.Padding.NONE,
                LumoUtility.Margin.NONE
            )

            setAcceptedFileTypes("application/pdf")

            addSucceededListener {
                val stream = (it.source.receiver as MemoryBuffer)
                    .inputStream

                savePath = files.copyTemp("username", stream)

            }

            maxFiles = 1
            maxFileSize = 1024 * 1024 *2
        }

        remarksText = TextArea()
        binder.forField(remarksText)
            .bind("remarks")

        optionPending = Checkbox("In Sospeso", false)

        binder.bean = bean

        add(
            typeField, subjectField,
            dcSelect, supplierField,
            refField, HorizontalLayout(optionPending, fileUpload))

        add(remarksText, 2)

        addLineButton = Button("AGGIUNGI")

        lineContainer = VerticalLayout()
        add(lineContainer, 2)

        add(addLineButton, 1)

        addLineButton.addClickListener {
            if(validate()) {

                lineContainer.add( makeLineform() )
            }
        }

        if( type != null ) {
            typeField.value = type
        }

        typeField.addValueChangeListener {
            linesForms().forEach { form ->
                form.setItemFieldList()
                form.setSnItems()
            }
        }

        dcSelect.addValueChangeListener {
            linesForms().forEach { it.setPositionsByDc() }
        }

    }

    private fun makeLineform(): HorizontalLayout {
        val hr = HorizontalLayout()
        hr.add(OrderLineForm(binder.bean, ss, dcs))
        hr.add(Button(Icon(VaadinIcon.MINUS)) { lineContainer.remove(hr) })
        hr.add(Button(Icon(VaadinIcon.ERASER)) { (hr.children.findFirst().get() as OrderLineForm).reset(type!!) })

        return hr
    }
    private fun linesForms() = lineContainer.children
                                    .map{ (it as HorizontalLayout).children.findFirst().get() as OrderLineForm }

    fun reset(type: Order.Type?=null) {
        lineContainer.removeAll()
        optionPending.value = false
        bean = OrderPresentation()
        bean.type = type
        binder.readBean(bean)
    }

    fun validate() : Boolean {

        isValid = binder.validate().isOk

        if(isValid.not()) return false

        val all = linesForms().allMatch(OrderLineForm::validate)

        return  all && isValid
    }

    fun compileOrder() : Order {

        binder.writeBean(bean)

        val order = Order(
            number = bean.number,
            type = bean.type!!,
            op = ops.findAll().first(),
            issued = LocalDateTime.now(),
            dc = bean.datacenter!!,
            subject = bean.subject!!,
            supplier = bean.supplier!!,
            status = Order.Status.COMPLETED
        ).apply {
            ref = ref
            remarks = bean.remarks
        }

        if(optionPending.value) {
            order.status = Order.Status.PENDING
        }

        linesForms()
            .forEach { order.lines.add( it.compileLine(order) ) }

        return order
    }

    fun editOrder(o: Order) {

        lineContainer.removeAll()

        val op = OrderPresentation(
            number = o.number,
            operator = o.op,
            type = o.type,
            subject = o.subject,
            ref = o.ref,
            supplier = o.supplier,
            datacenter = o.dc
        )

        binder.bean = op

        o.lines.forEach { line ->
            val olp = OrderLinePresentation(
                item = line.item,
                position = line.position,
                amount = line.amount,
                sn = line.sn,
                pt = line.pt,
                viewid = UUID.randomUUID()
            )

            val hl = makeLineform()
            val form = hl.children.findFirst().get() as OrderLineForm
            form.bean = olp
            form.binder.readBean(form.bean)

            if(olp.sn!=null || olp.pt!=null) {
                form.setUnique(olp.sn, olp.pt)
            }

            lineContainer.add(hl)

        }

        optionPending.value = true
    }
}



