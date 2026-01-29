package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.OrderLine
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.StorageService
import java.util.*

data class OrderLinePresentation(
    val viewid: UUID = UUID.randomUUID(),
    var item:String?=null,
    var position:String?=null,
    var amount:Int?=null,
    var sn:String?=null,
    var pt:String?=null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderLinePresentation

        return viewid == other.viewid
    }

    override fun hashCode(): Int {
        return viewid.hashCode()
    }
}

class OrderLineForm(
    private var order: OrderPresentation,
    private val ss: StorageService,
    private val dcs: DatacenterService
) : HorizontalLayout() {

    val binder:Binder<OrderLinePresentation> = Binder(OrderLinePresentation::class.java, false)

    var bean = OrderLinePresentation()

    private val itemsField:ComboBox<String> = ComboBox<String>()
    private val itemByUnique:TextField = TextField()
    private val posField: Select<String> = Select()
    private val amountField: IntegerField = IntegerField()
    private val snField: ComboBox<String> = ComboBox()
    private val ptField: ComboBox<String> = ComboBox()

    init {

        bind()

        gui()

    }

    private fun gui() {
        itemsField.apply {
            placeholder = "MERCE"
            isAllowCustomValue = true
            setItemFieldList()

            addValueChangeListener {
                setMaxAmountOnChange()
            }
        }

        itemByUnique.apply {
            placeholder = "MERCE"
            isVisible = false
            isEnabled = false
        }

        posField.apply {
            placeholder = "POSIZIONE"
            setPositionsByDc()

            addValueChangeListener { ev ->
                if(order.type == Order.Type.OUTBOUND ) {
                    ss.findAll()
                        .filter { it.pos == ev.value }
                        .map(Storage::item)
                        .also {
                            itemsField.setItems(it)
                        }
                }
                setMaxAmountOnChange()
            }
        }

        amountField.apply {
            placeholder = "#"
            min = 1
        }

        snField.apply {
            placeholder = "S/N"
            setSnItems()

            addValueChangeListener {
                setUnique(sn=it.value, pt=null)
            }
        }

        ptField.apply {
            placeholder = "PT"
            setPtItems()

            addValueChangeListener {
                setUnique(sn=null, pt=it.value)
            }
        }

        add(itemByUnique, itemsField, posField, amountField, snField, ptField)
    }

    private fun bind() {
        binder.forField(itemsField)
            .withValidator { value, _ ->
                if( itemsField.isVisible && value.isNullOrEmpty() ) ValidationResult.error("Obbligatorio")
                else ValidationResult.ok()
            }
            .bind({it.item},{line, value -> line.item = value})

        binder.forField(itemByUnique)
            .withValidator { value, _ ->
                if( itemByUnique.isVisible && value.isNullOrEmpty() ) ValidationResult.error("Obbligatorio")
                else ValidationResult.ok()
            }
            .bind({it.item},{line, value -> line.item = value})

        binder.forField(posField)
            .asRequired()
            .bind({it.position},{line, value -> line.position = value})

        binder.forField(amountField)
            .asRequired()
            .bind({it.amount},{line, value -> line.amount = value})

        binder.forField(snField)
            .bind({it.sn},{line, value -> line.sn = value})

        binder.forField(ptField)
            .bind({it.pt},{line, value -> line.pt = value})
    }

    fun setPositionsByDc() {
        dcs.findAll(locations = true)
            .filter { it == order.datacenter }
            .first()
            .locations
            .also {
                posField.setItems(it)
            }
    }

    fun setItemFieldList() {
        when(order.type!!) {
            Order.Type.INBOUND -> {
                itemsField.setItems(ss.findAllItems())
            }
            Order.Type.OUTBOUND -> {
                ss.findAll()
                    .filter { order.datacenter == it.dc }
                    .map(Storage::item)
                    .also {
                        itemsField.setItems(it)
                    }
            }
        }
    }

    private fun setMaxAmountOnChange() {
        if(order.type == Order.Type.OUTBOUND && itemsField.value.isNullOrEmpty().not() && posField.value.isNullOrEmpty().not() ) {
            ss.findForCount(itemsField.value, order.datacenter!!.short, posField.value)
                .also {
                    amountField.max = it!!.amount
                    amountField.placeholder = "Max ${it.amount}"
                }
        } else {
            amountField.max = Integer.MAX_VALUE
            amountField.placeholder = "#"
        }
    }

    fun setSnItems() {
        when(order.type!!) {
            Order.Type.INBOUND -> {
                snField.isAllowCustomValue = true
                snField.setItems(listOf())
            }
            Order.Type.OUTBOUND -> {
                snField.isAllowCustomValue = false
                ss.findAll()
                    .filter {
                        (order.datacenter == it.dc)
                            .and( it.sn.isNullOrEmpty().not() )
                    }
                    .map(Storage::sn)
                    .also {
                        snField.setItems(it)
                    }
            }
        }
    }

    fun setPtItems() {
        when(order.type!!) {
            Order.Type.INBOUND -> {
                ptField.isAllowCustomValue = true
                ptField.setItems(listOf())
            }
            Order.Type.OUTBOUND -> {
                ptField.isAllowCustomValue = false
                ss.findAll()
                    .filter {
                        (order.datacenter == it.dc)
                            .and( it.pt.isNullOrEmpty().not() )
                    }
                    .map(Storage::pt)
                    .also {
                        ptField.setItems(it)
                    }
            }
        }
    }

    fun setUnique(sn:String?, pt:String?) {

        if(sn.isNullOrEmpty() && pt.isNullOrEmpty()) {

            itemByUnique.isVisible = false
            itemsField.isVisible = true
            itemsField.isEnabled = true

            posField.isEnabled = true

            amountField.isEnabled = true

            snField.value = null

            ptField.value = null

            return
        }

        if( sn.isNullOrEmpty().not() ) {
            val storage = ss.findBySn(sn)!!

            itemsField.isEnabled = false
            itemsField.isVisible = false
            itemByUnique.value = storage.item
            itemByUnique.isVisible = true

            posField.value = storage.pos
            posField.isEnabled = false

            amountField.value = 1
            amountField.isEnabled = false

            ptField.value = storage.pt
            ptField.isEnabled = false

            return
        }

        if( pt.isNullOrEmpty().not() ) {
            val storage = ss.findByPt(pt)!!

            itemsField.isEnabled = false
            itemsField.isVisible = false
            itemByUnique.value = storage.item
            itemByUnique.isVisible = true

            posField.value = storage.pos
            posField.isEnabled = false

            amountField.value = 1
            amountField.isEnabled = false

            snField.value = storage.sn
            snField.isEnabled = false

            return
        }
    }

    fun validate(): Boolean {
        val status = binder.validate()
        if ( status.isOk ) {
            binder.writeBean(bean)
            return true
        }

        status.validationErrors.forEach(::println)
        return false
    }

    fun reset(type: Order.Type) {
        bean = OrderLinePresentation()
        order.type = type
        setUnique(null,null)
        setItemFieldList()
        binder.bean = bean
    }

    fun compileLine(o:Order): OrderLine {
        binder.writeBean(bean)
        bean.item = if( itemsField.isVisible ) itemsField.value
                    else itemByUnique.value
        val line = OrderLine(
            order = o,
            item = bean.item!!,
            position = bean.position!!,
            amount = bean.amount!!,
            sn = bean.sn,
            pt = bean.pt
        )

        return line
    }

}