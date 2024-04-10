package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderLinePresentation
import java.util.*


open class LoadLineForm(
    protected val BO:BackOffice,
    dc:Datacenter
) : FlexLayout() {

    protected val nullBean= OrderLinePresentation()

    protected val itemsField:ComboBox<String>
    protected val positionField:ComboBox<String>
    protected val amountInt:IntegerField
    protected val snField:TextField
    protected val ptField:TextField

    val binder:Binder<OrderLinePresentation> = Binder()

    init {
        classNames.add(LumoUtility.Gap.SMALL)

        itemsField = ComboBox<String>().apply {
            setItems(BO.os.findItems())
            placeholder = "MERCE"
            isAllowCustomValue = true
            value = ""
            minWidth = "75px"
            maxWidth = "250px"
        }

        positionField = ComboBox<String>().apply {
            setItems(dc.locations)

            placeholder = "POSIZIONE"
            isAllowCustomValue = false
            value = ""
            minWidth = "75px"
            maxWidth = "250px"
        }

        amountInt = IntegerField()
            .apply {
                placeholder = "#"
                value = 0
                width = "5.6rem"
            }

        snField = TextField().apply {
            prefixComponent = Span("S/N")
            value = ""
        }

        ptField = TextField().apply {

            prefixComponent = Span("PT")
            maxLength = 8
            minLength = 8
            allowedCharPattern = "\\d"
            value = ""
            width = "8rem"
        }

        itemsField.setItems(BO.os.findItems())
        positionField.setItems(dc.locations)

        this.add(
            itemsField,
            positionField,
            amountInt,
            snField,
            ptField
        )

        bind()

        binder.readBean(nullBean)

    }

    private fun bind() {

        binder.isFieldsValidationStatusChangeListenerEnabled = false

        binder.forField(itemsField)
            .asRequired("Obbligatorio")
            .bind({it.item},{line,value->line.item=value})

        binder.forField(positionField)
            .asRequired("Obbligatorio")
            .bind({it.position},{line,value->line.position=value})

        binder.forField(amountInt)
            .asRequired("Obbligatorio")
            .withValidator { value, _ ->
                if(value>0) ValidationResult.ok()
                else ValidationResult.error("Obbligatorio maggiore di zero")
            }
            .bind({it.amount},{line,value->line.amount=value})
    }

    fun reset() {
        binder.readBean(nullBean)
    }

    open fun setDc(dc:Datacenter) {
        positionField.setItems(dc.locations)

    }

    fun validate() = binder.validate().isOk

    fun compile() = OrderLinePresentation(
        UUID.randomUUID(),
        item = itemsField.value,
        position = positionField.value,
        amount = amountInt.value,
        sn = snField.value,
        pt = ptField.value
    )

}

class UnloadLineForm(BO:BackOffice, dc:Datacenter) : LoadLineForm(BO, dc) {

    private var storage =  BO.ss.find(dc = dc)

    init {
        setDc(dc)

        positionField.addValueChangeListener {
            queryStorage().also(::setHints)
        }

        itemsField.addValueChangeListener {
            queryStorage().also(::setHints)
        }
    }

    override fun setDc(dc:Datacenter) {

        storage = BO.ss.find(dc = dc)
        storage.forEach(::println)

        val pos = storage.map(Storage::pos).distinct()
        positionField.setItems(pos)

        val items = storage.map(Storage::item).distinct()
        itemsField.setItems(items)
    }

    private fun queryStorage() = storage
            .filter { st ->
                var r = true
                if( itemsField.value.isNullOrEmpty().not() ) {
                    r =  r.and(st.item == itemsField.value )
                }

                if( positionField.value.isNullOrEmpty().not() ) {
                    r = r.and( st.pos == positionField.value )
                }

                r
            }

    private fun setHints(res:List<Storage>) {
        if( res.size == 1 ) {
            amountInt.max = res.first().amount
            return
        }

        if( itemsField.value.isNullOrEmpty() ) {
            itemsField.setItems(res.map(Storage::item))
            return
        }

        if( positionField.value.isNullOrEmpty() ) {
            positionField.setItems(res.map(Storage::pos))
            return
        }
    }
}

@Route("test")
class TestView(
    private val bo:BackOffice
) : VerticalLayout() {

    init {

        val form = LoadLineForm(bo, bo.dcs.findAll(true).first())
        val form2 = UnloadLineForm(bo, bo.dcs.findAll(true).first())
        add(
            HorizontalLayout(
                form,
                Button(Icon(VaadinIcon.MINUS)) { form.reset() },
                Button(Icon(VaadinIcon.EXCLAMATION)) { form.validate() }
            )
        )

        add(
            HorizontalLayout(
                form2,
                Button(Icon(VaadinIcon.MINUS)) { form2.reset() },
                Button(Icon(VaadinIcon.EXCLAMATION)) { form2.validate() }
            )
        )

        add(
            Select<Datacenter>().apply {

                setItems(bo.dcs.findAll(true))

                setItemLabelGenerator { "${it.short} - ${it.fullName}" }

                addValueChangeListener {
                    form.setDc(it.value)
                    form2.setDc(it.value)
                }
            }
        )

    }

}