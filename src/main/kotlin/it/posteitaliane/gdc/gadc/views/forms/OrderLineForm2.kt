package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.Validator
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
    private val snField = ComboBox<String>()
    private val ptField = ComboBox<String>()

    private val SNRegistration = Validator<String> { value, _ ->
        val found = ss.findBySn(value) != null

        if( found && order.type!! == Order.Type.INBOUND ) {
            return@Validator ValidationResult.error("SN già registrato")
        }

        if( found.not() && order.type!! == Order.Type.OUTBOUND) {
            return@Validator ValidationResult.error("SN non registrato")
        }

        return@Validator ValidationResult.ok()

    }

    private val PTRegistration = Validator<String> { value, _ ->
        val found = ss.findByPt(value) != null

        if( found && order.type!! == Order.Type.INBOUND ) {
            return@Validator ValidationResult.error("PT già registrato")
        }

        if( found.not() && order.type!! == Order.Type.OUTBOUND) {
            return@Validator ValidationResult.error("PT non registrato")
        }

        return@Validator ValidationResult.ok()

    }

    init {

        binder.bean = bean

        gui()

        bind()

    }

    private fun bind() {
        binder.forField(itemField)
            .asRequired("Campo obbligatorio")
            .bind({it.item}, {olp, value -> olp.item = value})

        binder.forField(posField)
            .asRequired("Campo obbligatorio")
            .bind({it.position},{olp, value -> olp.position = value})

        binder.forField(amountField)
            .asRequired("Campo obbligatorio")
            .bind({it.amount},{olp, value -> olp.amount = value})

        binder.forField(snField)
            .withValidator(SNRegistration)
            .bind({it.sn},{olp, value -> olp.sn = value})

        binder.forField(ptField)
            .withValidator(PTRegistration)
            .bind({it.pt},{olp, value -> olp.pt = value})

    }

    private fun gui() {

        itemField.apply {
            placeholder = "MERCE"

            setItemsByType(order.type!!)

            addValueChangeListener {
                setAmountByStorage(it.value, posField.value)
            }
        }

        posField.apply {
            setLocationsByDc(order.datacenter!!)

            addValueChangeListener {
                if(snField.value.isNullOrEmpty() && ptField.value.isNullOrEmpty() ) {
                    setLocationsByDc(order.datacenter!!)
                    setItemsByType(order.type!!)
                    setItemsByPos(it.value)
                    setAmountByStorage(itemField.value, it.value)
                }else {
                    setLocationsByDc(order.datacenter!!)
                }
            }
        }

        amountField.apply {
            min = 1
            placeholder = "#"
        }

        snField.apply {
            placeholder = "S/N"

            setSnListByType()

            addValueChangeListener {
                setFormBySn(it.value)
            }
        }

        ptField.apply {
            placeholder = "PT"

            setPtListByType()

            addValueChangeListener {
                setFormByPt(it.value)
            }
        }


        add(itemField, posField, amountField, snField, ptField)

    }

    private fun setItemsByPos(pos: String) {
        if(order.type!! == Order.Type.OUTBOUND) {
            ss.findAll()
                .filter { it.dc == order.datacenter && it.pos == pos }
                .map(Storage::item)
                .also {
                    itemField.clear()
                    itemField.setItems(it)
                }
        }
    }

    fun setItemsByType(type: Order.Type) {
        when(type) {
            Order.Type.OUTBOUND -> {
                itemField.isAllowCustomValue = false
                ss.findAll()
                    .filter { it.dc == order.datacenter }
                    .map(Storage::item)
                    .distinct().also {
                        itemField.setItems(it)
                    }
            }
            else -> {
                val saved = itemField.value
                itemField.isAllowCustomValue = true
                ss.findAll()
                    .map(Storage::item)
                    .distinct().also {
                        itemField.setItems(it)
                        itemField.value = saved
                    }
            }
        }
    }

    fun setLocationsByDc(dc:Datacenter) {
        when(order.type!!){
            Order.Type.OUTBOUND -> {
                ss.findAll()
                    .filter { it.dc ==  dc  }
                    .map(Storage::pos)
                    .also {
                        posField.setItems(it)
                    }
            }
            else -> {
                dcs.findAll(true)
                    .filter { it == dc }
                    .first()
                    .locations
                    .also {
                        posField.setItems(it)
                    }
            }
        }
    }

    fun setAmountByStorage(item:String?, pos:String?) {
        if(item.isNullOrEmpty().not() && pos.isNullOrEmpty().not()) {
            ss.findAll()
                .filter { it.item == item && it.pos == pos && it.dc == order.datacenter }
                .firstOrNull().also {
                    if (it != null) {
                        amountField.max = it.amount
                        amountField.placeholder = "Max: ${it.amount}"
                    } else {
                        amountField.max = Int.MAX_VALUE
                        amountField.placeholder = "#"
                    }
                }
        } else {
            amountField.max = Int.MAX_VALUE
            amountField.placeholder = "#"
        }
    }

    fun setSnListByType() {
        if(order.type!! == Order.Type.OUTBOUND) {
            snField.isAllowCustomValue = false
            ss.findAll().filter { it.dc == order.datacenter && it.sn.isNullOrEmpty().not() }
                .map(Storage::sn)
                .also {
                    snField.setItems(it)
                }
        } else {
            snField.isAllowCustomValue = true
            snField.setItems(listOf())
        }
    }

    fun setFormBySn(sn:String) {
        if(order.type!! == Order.Type.OUTBOUND) {
            ss.findBySn(sn).also {
                amountField.value = 1
                setLocationsByDc(order.datacenter!!)
                posField.value = it!!.pos

                itemField.value = it.item

                ptField.value = it.pt
            }
        }
    }

    fun setPtListByType() {
        if(order.type!! == Order.Type.OUTBOUND) {
            ptField.isAllowCustomValue = false
            ss.findAll().filter { it.dc == order.datacenter && it.pt.isNullOrEmpty().not() }
                .map(Storage::pt)
                .also {

                    ptField.setItems(it)
                }
        } else {
            ptField.isAllowCustomValue = true
            ptField.setItems(listOf())
        }
    }

    fun setFormByPt(pt:String) {
        if(order.type!! == Order.Type.OUTBOUND) {
            ss.findByPt(pt).also {
                amountField.value = 1
                setLocationsByDc(order.datacenter!!)
                posField.value = it!!.pos

                itemField.value = it.item

                snField.value = it.sn
            }
        }
    }

    fun validate(): Boolean {
        val states = binder.validate()

        states.validationErrors.forEach(::println)

        return states.isOk
    }

}