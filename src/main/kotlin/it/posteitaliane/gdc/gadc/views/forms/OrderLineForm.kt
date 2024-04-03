package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.Validator
import com.vaadin.flow.data.binder.ValueContext
import it.posteitaliane.gdc.gadc.model.Datacenter
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
    items:List<String>,
    positions:MutableList<String>,
    ss:StorageService
) : HorizontalLayout() {

    private var positionsField: ComboBox<String>

    val binder:Binder<OrderLinePresentation>

    var bean = OrderLinePresentation()

    private val itemsField:ComboBox<String>

    private val amountField:IntegerField

    val snField:TextField

    val ptField:TextField

    val uniqueButton:Button

    var SNExternalValidator:(String, ValueContext)->ValidationResult = {_,_->ValidationResult.ok()}
        /*{ value, _ ->
        if( isUnique().not() ) ValidationResult.ok()

        if( parent==null) ValidationResult.ok()

        val l = parent!!.linesForms().filter { it != this }.map { it.snField.value }.toList()

        if( l.contains(value) ) ValidationResult.error("Duplicated SN")
        else ValidationResult.ok()

    }*/

    var PTExternalValidator:(String, ValueContext)->ValidationResult = {_,_->ValidationResult.ok()}
    /*{ value, _ ->
        if( isUnique().not() ) ValidationResult.ok()

        if( parent==null ) ValidationResult.ok()

        val l = parent!!.linesForms().filter { it != this }.map { it.ptField.value }.toList()

        if( l.contains(value) ) ValidationResult.error("Duplicated PT")
        else ValidationResult.ok()

    }*/

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

        snField = TextField()
            .apply {
                prefixComponent = Span("S/N")
                isVisible = false
                value = ""
            }

        ptField = TextField()
            .apply {
                prefixComponent = Span("PT")
                isVisible = false
                maxLength = 8
                minLength = 8
                allowedCharPattern = "\\d"
                value = ""
            }

        val PTNotRegistered = Validator<String> { value, _ ->
            if( ss.findByPt(value) != null ) ValidationResult.error("PT must not be registered")
            else ValidationResult.ok()
        }

        val SNNotRegistered = Validator<String> { value, _ ->
            if( ss.findBySn(value) != null ) ValidationResult.error("SN must not be registered")
            else ValidationResult.ok()
        }

        Validator<String> { value, _ ->
            if( ss.findByPt(value) == null ) ValidationResult.error("PT must be registered")
            else ValidationResult.ok()
        }

        Validator<String> { value, _ ->
            if( ss.findBySn(value) == null ) ValidationResult.error("SN must be registered")
            else ValidationResult.ok()
        }

        uniqueButton = Button(Icon(VaadinIcon.ARROW_RIGHT)) {toggleUnique()}

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
            .withValidator { value, _ ->
                if(isUnique() && value.isNullOrEmpty() && ptField.value.isNullOrEmpty()) ValidationResult.error("Obbligatorio almeno uno fra S/N e PT")
                else ValidationResult.ok()
            }
            .withValidator(SNNotRegistered)
            .withValidator(SNExternalValidator)
            .bind({ol -> ol.sn}, {ol, sn -> ol.sn = sn})

        binder.forField(ptField)
            .withValidator { value, _ ->
                if(isUnique() && value.isNullOrEmpty() && snField.value.isNullOrEmpty()) ValidationResult.error("Obbligatorio almeno uno fra S/N e PT")
                else ValidationResult.ok()
            }
            .withValidator(PTNotRegistered)
            .withValidator(PTExternalValidator)
            .bind({ol -> ol.pt}, {ol, pt -> ol.pt = pt})

        reset()
        /* UI */
        add(itemsField, positionsField, amountField, uniqueButton, snField, ptField)
    }

    private fun toggleUnique() {
        if( isUnique() ) {
            snField.value = ""
            snField.isVisible = false

            ptField.value = ""
            ptField.isVisible = false

            amountField.value = null
            amountField.isEnabled = true

            uniqueButton.icon = Icon(VaadinIcon.ARROW_RIGHT)
        } else {
            snField.isVisible = true
            ptField.isVisible = true

            amountField.value = 1
            amountField.isEnabled = false

            uniqueButton.icon = Icon(VaadinIcon.ARROW_LEFT)
        }
    }

    fun reset(dc:Datacenter?=null, skipItem:Boolean=false, skipAmount:Boolean=false) {
        bean = OrderLinePresentation()
        if( skipAmount ) {
            bean.amount = amountField.value
        }
        if( isUnique() ) {
            bean.amount = 1
        }

        if( skipItem ) {
            bean.item = itemsField.value
        }

        if( dc != null ) {
            positionsField.clear()
            positionsField.setItems(dc.locations)

            bean.sn = snField.value
            bean.pt = ptField.value
        }
        binder.readBean(bean)
    }

    fun validate() {
        if ( binder.validate().isOk ) {
            binder.writeBean(bean)
        }
    }

    private fun isUnique() : Boolean = snField.isVisible

}