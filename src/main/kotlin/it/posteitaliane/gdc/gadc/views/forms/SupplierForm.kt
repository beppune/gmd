package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationResult
import it.posteitaliane.gdc.gadc.model.Supplier
import it.posteitaliane.gdc.gadc.services.SupplierService

data class SupplierPresentation(
    var name:String?=null,
    var piva:String?=null,
    var legal:String?=null
)

class SupplierForm(
    private val sups:SupplierService
) : FormLayout() {
    fun reset() {
        binder.readBean(null)
    }

    fun validate() = binder.validate().isOk

    fun compile() = Supplier(
        name = nameField.value,
        piva = pivaField.value,
        legal = legalField.value
    )

    private val nameField:TextField

    private val pivaField:TextField

    private val legalField:TextArea

    private val binder:Binder<SupplierPresentation>

    init {

        binder = Binder(SupplierPresentation::class.java, false)

        nameField = TextField().apply {
            placeholder = "NOME"
        }
        binder.forField(nameField)
            .asRequired("Obbligatorio")
            .withValidator { value, _ ->
                if( sups.findAll().map(Supplier::name).contains(value) ) ValidationResult.error("Nome già registrata")
                else ValidationResult.ok()
            }
            .bind("name")

        pivaField = TextField().apply {
            allowedCharPattern = "[0-9]"
            maxLength = 11
            minLength = 11
            placeholder = "PARTITA IVA"
        }
        binder.forField(pivaField)
            .asRequired("Obbligatorio")
            .withValidator { value, _ ->
                if( sups.findAll().map(Supplier::piva).contains(value) ) ValidationResult.error("P. Iva già registrata")
                else ValidationResult.ok()
            }
            .bind("piva")

        legalField = TextArea().apply {
            placeholder = "Indirizzo Legale"
        }
        binder.forField(legalField)
            .asRequired("Obbligatorio")
            .withValidator { value, _ ->
                if( sups.findAll().map(Supplier::legal).contains(value) ) ValidationResult.error("Indirizzo già registrato")
                else ValidationResult.ok()
            }
            .bind("legal")

        binder.readBean(null)

        add(nameField, 2)
        add(pivaField, 2)
        add(legalField, 2)
    }

}