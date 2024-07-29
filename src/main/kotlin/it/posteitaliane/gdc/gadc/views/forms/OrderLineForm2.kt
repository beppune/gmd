package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.StorageService

class OrderLineForm2(
    private var order: OrderPresentation,
    private val ss: StorageService,
    private val dcs: DatacenterService
) : HorizontalLayout() {

    private val binder = Binder(OrderLinePresentation::class.java, false)

    var bean = OrderLinePresentation()

    private val itemField = ComboBox<String>()
    private val posField = Select<String>()
    private val amountField = IntegerField()

    init {

        binder.readBean(bean)

        gui()

    }

    private fun gui() {

        itemField.apply {
            placeholder = "MERCE"

            setItemsByType(order.type!!)

            addValueChangeListener {
                setAmountByItemAndPos(value!!, posField.value)
            }
        }

        posField.apply {
            setLocationsByDc(order.datacenter!!)

            addValueChangeListener {
                setItemsByType(order.type!!)
                setAmountByItemAndPos(itemField.value, posField.value)
            }
        }

        amountField.apply {
            min = 1
            placeholder = "#"
        }

        add(itemField, posField, amountField)

    }

    fun setItemsByType(type: Order.Type) {
        when(type) {
            Order.Type.OUTBOUND -> {
                itemField.isAllowCustomValue = false
                ss.findAll()
                    .filter {
                        var p = it.dc == order.datacenter
                        if( posField.value.isNullOrEmpty().not() ) {
                            p = p && it.pos == posField.value
                        }
                        p
                    }
                    .map(Storage::item)
                    .distinct().also {
                        itemField.setItems(it)
                    }
            }
            else -> {
                itemField.isAllowCustomValue = true
                ss.findAll()
                    .map(Storage::item)
                    .distinct().also {
                        itemField.setItems(it)
                    }
            }
        }
    }

    fun setLocationsByDc(dc:Datacenter) {
        dcs.findAll(true)
            .filter { it == dc }
            .first()
            .locations
            .also {
                posField.setItems(it)
            }
    }

    fun setAmountByItemAndPos(item:String?, pos:String?) {
        if(posField.value.isNullOrEmpty().not()) {
            amountField.let {
                ss.findAll().filter { it.item == item && it.pos == pos && order.type == Order.Type.OUTBOUND }
                    .first().also { amountField.placeholder = "MAX: ${it.amount}" }
            }
        } else {
            amountField.max = Int.MAX_VALUE
            amountField.placeholder == "#"
        }
    }

}