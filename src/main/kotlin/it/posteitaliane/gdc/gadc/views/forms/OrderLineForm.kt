package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.StorageService
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
    private val posField: Select<String> = Select()
    private val amountField: IntegerField = IntegerField()

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

        add(itemsField, posField, amountField)
    }

    private fun bind() {
        binder.forField(itemsField)
            .asRequired()
            .bind("item")

        binder.forField(posField)
            .asRequired()
            .bind("position")

        binder.forField(amountField)
            .asRequired()
            .bind("amount")
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

    fun setItemFieldList(saveItem:Boolean=true) {
        val value = itemsField.value
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

        if(saveItem) itemsField.value = value
    }

    private fun setMaxAmountOnChange() {
        if(order.type == Order.Type.OUTBOUND && itemsField.value.isNullOrEmpty().not() && posField.value.isNullOrEmpty().not() ) {
            ss.findForCount(itemsField.value, order.datacenter!!.short, posField.value)
                .also {
                    amountField.max = it!!.amount
                    amountField.placeholder = "Max ${it!!.amount}"
                }
        } else {
            amountField.max = Integer.MAX_VALUE
            amountField.placeholder = "#"
        }
    }

    fun validate() {
        if ( binder.validate().isOk ) {
            binder.writeBean(bean)
        }
    }

}