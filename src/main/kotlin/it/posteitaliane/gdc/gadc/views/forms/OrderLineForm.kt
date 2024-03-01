package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gadc.model.Datacenter

class OrderLineForm(
    private val items:List<String>,
    private val positions:MutableList<String>
) : HorizontalLayout() {

    private var positionsField: ComboBox<String>

    private val binder:Binder<OrderLinePresentation>

    private val bean = OrderLinePresentation()

    private val itemsField:ComboBox<String>

    private val amountField:IntegerField

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
                addBlurListener {writeBean()}
            }

        positionsField = ComboBox<String>()
            .apply {
                placeholder = "POSIZIONE"
                setItems(positions)
                isAllowCustomValue = false
                value = ""
                addBlurListener {writeBean()}
            }

        amountField = IntegerField()
            .apply {
                placeholder = "QUANTITÀ"
                value = 0
                addBlurListener {writeBean()}
            }

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

        resetButton = Button(Icon(VaadinIcon.CLOSE)) {binder.readBean(bean)}

        add(itemsField, positionsField, amountField, saveButton, resetButton)
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



}