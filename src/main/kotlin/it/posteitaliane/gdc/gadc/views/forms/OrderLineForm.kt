package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
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
    private val items:List<String>,
    private val positions:MutableList<String>
) : HorizontalLayout() {

    var snIsRegistered:(String)->Boolean={false}
    var ptIsRegistered:(String)->Boolean={false}

    private var positionsField: ComboBox<String>

    private val binder:Binder<OrderLinePresentation>

    private var bean = OrderLinePresentation()

    private val itemsField:ComboBox<String>

    private val amountField:IntegerField

    val snField:TextField

    private val ptField:TextField

    private val uniqueButton:Button

    private val saveButton:Button

    private val resetButton:Button

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
                addBlurListener {writeBean()}
            }

        positionsField = ComboBox<String>()
            .apply {
                placeholder = "POSIZIONE"
                setItems(positions)
                isAllowCustomValue = false
                value = ""
                minWidth = "75px"
                maxWidth = "250px"
                addBlurListener {writeBean()}
            }

        amountField = IntegerField()
            .apply {
                placeholder = "#"
                value = 0
                maxWidth = "50px"
                addBlurListener {writeBean()}
            }

        snField = TextField()
            .apply {
                placeholder = "S/N"
                isVisible = false
                value = ""
                addBlurListener {writeBean()}
            }

        ptField = TextField()
            .apply {
                placeholder = "PT"
                isVisible = false
                value = ""
                addBlurListener { writeBean() }
            }

        uniqueButton = Button(Icon(VaadinIcon.ARROW_RIGHT)) {toggleUnique()}

        binder.forField(itemsField)
            .asRequired("Obbligatorio")
            .bind({it.item},{ line, value -> line.item = value })

        binder.forField(positionsField)
            .asRequired("Obbligatorio")
            .withValidator { s, valueContext ->
                if(!positions.contains(s))
                    ValidationResult.error("La posizione non esiste")
                else ValidationResult.ok()
            }
            .bind({it.position},{line, value -> line.position = value})

        binder.forField(amountField)
            .withValidator { a, valueContext ->
                if( a == null || a <= 0 ) {
                    ValidationResult.error("Deve essere positivo")
                } else {
                    ValidationResult.ok()
                }
            }
            .bind({it.amount},{ line, value -> line.amount = value})

        binder.readBean(bean)

        binder.addValueChangeListener {writeBean()}

        saveButton = Button(Icon(VaadinIcon.CHECK)) {writeBean()}

        resetButton = Button(Icon(VaadinIcon.CLOSE)) {
            bean = OrderLinePresentation()
            binder.readBean(bean)
            toggleUnique()
        }

        binder.forField(snField)
            .withValidator { sn, _ ->
                if( snIsRegistered.invoke(sn) ) {
                    ValidationResult.error("S/N già presente in giacenza")
                } else ValidationResult.ok()
            }
            .bind({ol -> ol.sn}, {ol, sn -> ol.sn = sn})

        add(itemsField, positionsField, amountField, uniqueButton, snField, ptField, saveButton, resetButton)
    }

    private fun toggleUnique() {
        if( snField.isVisible ) {
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
            Notification.show(ex.toString()).position = Notification.Position.MIDDLE
        }
    }


    fun reset(dc:Datacenter?) {
        itemsField.value = ""
        if( dc != null ) {
            positions.clear()
            positions.addAll(dc.locations)
        }
    }

    fun validate() {
        binder.validate()
    }

}