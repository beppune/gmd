package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gadc.model.Datacenter

class OrderLineForm(
    items:List<String>,
    positions:MutableList<String>
) : HorizontalLayout() {

    var snIsRegistered:(String)->Boolean={false}
    var ptIsRegistered:(String)->Boolean={false}

    private var positionsField: ComboBox<String>

    val binder:Binder<OrderLinePresentation>

    private var bean = OrderLinePresentation()

    private val itemsField:ComboBox<String>

    private val amountField:IntegerField

    val snField:TextField

    val ptField:TextField

    private val uniqueButton:Button

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

                addBlurListener {

                }
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
            .bind({ol -> ol.sn}, {ol, sn -> ol.sn = sn})

        binder.forField(ptField)
            .withValidator { value, _ ->
                if(isUnique() && value.isNullOrEmpty() && snField.value.isNullOrEmpty()) ValidationResult.error("Obbligatorio almeno uno fra S/N e PT")
                else ValidationResult.ok()
            }
            .bind({ol -> ol.pt}, {ol, pt -> ol.pt = pt})

        binder.readBean(bean)

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
    private fun writeBean() {
        try {
            binder.writeBean(bean)

        } catch (ex: ValidationException) {
            Notification.show(ex.toString()).position = Notification.Position.TOP_CENTER
        }
    }


    fun reset(dc:Datacenter?=null, skipItem:Boolean=false) {
        bean = OrderLinePresentation()
        if( isUnique() ) {
            bean.amount = 1
        }

        if( skipItem ) {
            bean.item = itemsField.value
        }

        bean.amount = amountField.value

        if( dc != null ) {
            positionsField.clear()
            positionsField.setItems(dc.locations)

            bean.sn = snField.value
            bean.pt = ptField.value
        }
        binder.readBean(bean)
    }

    fun validate() {
        binder.validate()
    }

    private fun isUnique() : Boolean = snField.isVisible

}