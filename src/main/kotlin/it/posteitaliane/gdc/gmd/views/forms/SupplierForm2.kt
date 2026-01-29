package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H6
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.textfield.TextFieldVariant
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gmd.model.Supplier
import it.posteitaliane.gdc.gmd.services.SupplierService
import java.util.stream.Collectors

class SupplierForm2(private val sups:SupplierService): FormLayout() {

    private val nameField = TextField()
    private val pivaField = TextField()
    private val legalField = TextField()
    private val addressList = VerticalLayout()

    val binder = Binder(SupplierPresentation::class.java)
    var bean = SupplierPresentation()

    init {

        binder.bean = bean

        gui()

        bind()

    }

    fun setSupplier(s: Supplier?, isEnabled:Boolean=true) {

        nameField.isEnabled = true
        pivaField.isEnabled = true
        legalField.isEnabled = true

        if(s!=null) {
            bean = SupplierPresentation.fromSupplier(s)
            binder.bean = bean
            bean.addresses.forEach { makeAddress(it) }
            if(isEnabled.not()) {
                nameField.isEnabled = false
                pivaField.isEnabled = false
                legalField.isEnabled = false
            }
        } else {
            binder.bean = SupplierPresentation()
            addressList.removeAll()
        }
    }

    fun validate() = binder.validate()

    fun compile(): SupplierPresentation? {
        if(validate().isOk) {
            return SupplierPresentation(
                name = binder.bean.name,
                piva = binder.bean.piva,
                legal = binder.bean.legal
            ).apply {
                addresses.clear()
                addresses()
                    .map { it.value }
                    .collect(Collectors.toList())
                    .also {
                        addresses.addAll(it)
                    }
            }
        }

        return null
    }

    private fun addresses() =
        addressList.children
            .map { it as HorizontalLayout }
            .map { it.getComponentAt(0) as TextField }

    private fun bind() {
        binder.forField(nameField)
            .asRequired("Campo Obbligatorio")
            .bind({it.name},{it,value->it.name=value})

        binder.forField(pivaField)
            .asRequired("Campo Obbligatorio")
            .withValidator { value, _ ->
                val s = sups.findByName(value)
                if(s!=null) ValidationResult.error("Already registered with name: $value")
                else ValidationResult.ok()
            }
            .bind({it.piva},{it,value->it.piva=value})

        binder.forField(legalField)
            .asRequired("Campo Obbligatorio")
            .bind({it.legal},{it,value->it.legal=value})
    }

    private fun gui() {

        nameField.apply {
            placeholder = "NOME/RAGIONE SOCIALE"
            addThemeVariants(TextFieldVariant.LUMO_SMALL)
        }

        pivaField.apply {
            placeholder = "P. IVA"
            allowedCharPattern = "[0-9]"
            maxLength = 11
            minLength = 11
            addThemeVariants(TextFieldVariant.LUMO_SMALL)
        }

        legalField.apply {
            placeholder = "INDIRIZZO LEGALE"
            addThemeVariants(TextFieldVariant.LUMO_SMALL)
        }

        addressList.apply {
            style.setBorder("0.1rem")

            isPadding = false
            isSpacing = false
        }

        val form = HorizontalLayout().apply {
            style.setBorder("0.3rem")
            setWidthFull()

            val field = TextField().apply {
                setWidthFull()
                addThemeVariants(TextFieldVariant.LUMO_SMALL)
            }

            val button = Button("AGGIUNGI") {
                if(field.value.isNullOrEmpty().not()) {
                    makeAddress(field.value)
                    field.clear()
                }
            }
            button.addThemeVariants(ButtonVariant.LUMO_SMALL)

            add(field, button)
        }

        add(nameField, pivaField)
        add(legalField, 2)
        add(H6("Indirizzi").apply { style.setMarginTop("0.3REM") })
        add(addressList, 2)
        add(form, 2)
    }

    private fun makeAddress(s:String) {
        val hl = HorizontalLayout().apply { setWidthFull() }
        val field = TextField().apply {
            placeholder = "INDIRIZZO"
            value = s
            isEnabled = false
            setWidthFull()
            addThemeVariants(TextFieldVariant.LUMO_SMALL)
        }

        val button = Button("RIMUOVI") {
            addressList.remove(hl)
        }
        button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL)

        hl.add(field, button)
        addressList.add(hl)
    }

}