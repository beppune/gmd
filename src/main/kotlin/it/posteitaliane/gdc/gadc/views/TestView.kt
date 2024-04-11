package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.Registration
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderLinePresentation
import java.util.*


open class LoadLineForm(
    protected val BO:BackOffice,
    dc:Datacenter
) : FlexLayout() {

    protected val nullBean=OrderLinePresentation()

    protected val itemsField:ComboBox<String>
    protected val positionField:ComboBox<String>
    protected val amountInt:IntegerField
    protected val snField:TextField
    protected val ptField:TextField

    val binder:Binder<OrderLinePresentation> = Binder()

    protected val itemBlurRegistration:Registration

    init {
        classNames.add(LumoUtility.Gap.SMALL)

        itemsField = ComboBox<String>().apply {
            setItems(BO.os.findItems())
            placeholder = "MERCE"
            isAllowCustomValue = false
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

            itemBlurRegistration = addBlurListener {
                if( value.isNullOrEmpty().not() ) {
                    amountInt.value = 1
                    amountInt.isEnabled = false
                } else {
                    amountInt.clear()
                    amountInt.isEnabled = true
                }
            }
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

        binder.forField(snField)
            .withValidator { value, ctx ->
                if( BO.ss.findBySn(value) == null ) ValidationResult.ok()
                else ValidationResult.error("S/N registrato")
            }
            .bind({it.sn},{line,value->line.sn=value})

        binder.forField(ptField)
            .withValidator { value, ctx ->
                if( BO.ss.findByPt(value) == null ) ValidationResult.ok()
                else ValidationResult.error("PT registrato")
            }
            .bind({it.pt},{line,value->line.pt=value})

    }

    open fun reset() {
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

    private val snCombobox:ComboBox<String>
    private val ptComboBox:ComboBox<String>

    class TrackingChangedEvent(form:UnloadLineForm, val snpt:Pair<String?,String?>) : ComponentEvent<UnloadLineForm>(form, false)

    init {

        remove(snField, ptField)
        binder.removeBinding(snField)
        binder.removeBinding(ptField)

        //This derived class manage the enable state by itself
        itemBlurRegistration.remove()

        snCombobox = ComboBox<String>().apply { isAllowCustomValue = false }
        ptComboBox = ComboBox<String>().apply { isAllowCustomValue = false }

        setDc(dc)

        positionField.addValueChangeListener {
            queryStorage().also(::setHints)
        }

        itemsField.addValueChangeListener {
            queryStorage().also(::setHints)
        }

        snCombobox.addValueChangeListener {
            querySn().also(::setTracked)
        }

        ptComboBox.addValueChangeListener {
            queryPt().also(::setTracked)

        }

        binder.forField(snCombobox)
            .withValidator { value, _ ->
                if( value.isNullOrEmpty() || BO.ss.findBySn(value) != null ) ValidationResult.ok()
                else ValidationResult.error("S/N non in giacenza")
            }
            .bind({it.sn},{line,value->line.sn=value})

        binder.forField(ptComboBox)
            .withValidator { value, _ ->
                if( value.isNullOrEmpty() || BO.ss.findByPt(value) != null ) ValidationResult.ok()
                else ValidationResult.error("PT non in giacenza")
            }
            .bind({it.pt},{line,value->line.pt=value})

        add(snCombobox, ptComboBox)

    }

    fun addTrackingChangedEventListener(listener: ComponentEventListener<TrackingChangedEvent>) : Registration {
        return addListener(TrackingChangedEvent::class.java, listener)
    }

    override fun setDc(dc:Datacenter) {

        storage = BO.ss.find(dc = dc)

        val pos = storage.map(Storage::pos).distinct()
        positionField.setItems(pos)

        val items = storage.map(Storage::item).distinct()
        itemsField.setItems(items)

        snCombobox.setItems(storage.map(Storage::sn).filterNotNull().distinct())
        ptComboBox.setItems(storage.map(Storage::pt).filterNotNull().distinct())
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

    private fun querySn() = storage
        .filter { st -> st.sn == snCombobox.value }.distinct()


    private fun queryPt() = storage
        .filter { st -> st.pt == ptComboBox.value }.distinct()

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

    private fun setTracked(res:List<Storage>) {

        if( res.size == 1 ) {
            itemsField.run {
                value = res.first().item
                isEnabled = false
            }

            positionField.run {
                value = res.first().pos
                isEnabled = false
            }

            amountInt.run {
                value = 1
                isEnabled = false
            }

            snCombobox.run {
                value = res.first().sn
            }

            ptComboBox.run {
                value = res.first().pt
            }
        } else {
            itemsField.isEnabled = true
            positionField.isEnabled = true
            amountInt.isEnabled = true
        }

    }
}

@Route("test")
class TestView(
    private val bo:BackOffice
) : VerticalLayout() {

    init {

        val dc = bo.dcs.findAll(true).first()

        val form = LoadLineForm(bo, dc)
        val form2 = UnloadLineForm(bo, dc)
        val form3 = UnloadLineForm(bo, dc)
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
            ).apply {
                form2.addTrackingChangedEventListener {
                    Notification.show("${if (it.snpt.first == null) "SN" else "PT" } Changed")
                }
            },

            HorizontalLayout(
                form3,
                Button(Icon(VaadinIcon.MINUS)) { form3.reset() },
                Button(Icon(VaadinIcon.EXCLAMATION)) { form3.validate() }
            ).apply {
                form3.addTrackingChangedEventListener {
                    Notification.show("${if (it.snpt.first == null) "SN" else "PT" } Changed")
                }
            }
        )

        add(
            Select<Datacenter>().apply {

                value = dc

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