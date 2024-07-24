package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.Validator
import com.vaadin.flow.data.binder.ValueContext
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Storage
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
    private var order:OrderPresentation,
    private val items:List<String>,
    positions:MutableList<String>,
    private val ss:StorageService
) : HorizontalLayout() {

    private var positionsField: ComboBox<String>

    val binder:Binder<OrderLinePresentation>

    var bean = OrderLinePresentation()

    private val itemsField:ComboBox<String>

    val amountField:IntegerField

    val snField:ComboBox<String>

    val ptField:ComboBox<String>

    init {

        binder = Binder(OrderLinePresentation::class.java, false)

        itemsField = ComboBox<String>()
            .apply {
                placeholder = "MERCE"
                setItems(items)
                isAllowCustomValue = true
                value = ""
                minWidth = "75px"
                maxWidth = "250px"
            }

        positionsField = ComboBox<String>()
            .apply {
                placeholder = "POSIZIONE"
                setItems(positions)
                isAllowCustomValue = false
                value = ""
                minWidth = "75px"
                maxWidth = "250px"
            }

        amountField = IntegerField()
            .apply {
                placeholder = "#"
                value = 0
                maxWidth = "50px"
            }

        snField = ComboBox<String>()
            .apply {
                prefixComponent = Span("S/N")
                value = ""
            }

        ptField = ComboBox<String>()
            .apply {
                prefixComponent = Span("PT")
                allowedCharPattern = "\\d"
                value = ""
                element.executeJs("this.childNodes.item(0).maxLength = 8")
                element.executeJs("this.childNodes.item(0).minLength = 8")
            }

        val PTMustNotRegistered = Validator<String> { value, _ ->
            if( ss.findByPt(value) != null ) ValidationResult.error("PT must not be registered")
            else ValidationResult.ok()
        }

        val SNMustNotRegistered = Validator<String> { value, _ ->
            if( ss.findBySn(value) != null ) ValidationResult.error("SN must not be registered")
            else ValidationResult.ok()
        }

        val PTMustBeRegistered = Validator<String> { value, _ ->
            if( ss.findByPt(value) == null ) ValidationResult.error("PT must be registered")
            else ValidationResult.ok()
        }

        val SNMustBeRegistered = Validator<String> { value, _ ->
            if( ss.findBySn(value) == null ) ValidationResult.error("SN must be registered")
            else ValidationResult.ok()
        }

        /* BINDINGS */
        binder.forField(itemsField)
            .asRequired("Campo Obbligatorio")
            .bind({it.item},{ line, value -> line.item = value })

        binder.forField(positionsField)
            .asRequired("Campo Obbligatorio")
            .bind({it.position},{line, value -> line.position = value})

        binder.forField(amountField)
            .asRequired("Campo Obbligatorio")
            .bind({it.amount},{ line, value -> line.amount = value})

        binder.forField(snField)
            .bind({ol -> ol.sn}, {ol, sn -> ol.sn = sn})

        binder.forField(ptField)
            .bind({ol -> ol.pt}, {ol, pt -> ol.pt = pt})

        /* UI */
        add(itemsField, positionsField, amountField, snField, ptField)
    }

    private fun setAmount() {
        if( snField.value.isNotEmpty() || ptField.value.isNotEmpty() ) {
            amountField.value = 1
            amountField.isEnabled = false
            return
        }

        if( snField.value.isNullOrEmpty() && ptField.value.isNullOrEmpty() ) {
            amountField.clear()
            amountField.isEnabled = true
            return
        }
    }

    fun validate() {
        if ( binder.validate().isOk ) {
            binder.writeBean(bean)
        }
    }

}