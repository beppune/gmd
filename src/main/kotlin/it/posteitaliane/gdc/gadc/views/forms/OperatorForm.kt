package it.posteitaliane.gdc.gadc.views.forms

import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator

data class OperatorPresentation(
    var username:String?=null,
    var firstname:String?=null,
    var lastname:String?=null,
    var active:Boolean=true,
    val permissions:MutableList<Datacenter> = mutableListOf()
)
class OperatorForm(private val dcs:List<Datacenter>) : VerticalLayout() {

    private val userNameField:TextField

    private val activeField:Checkbox

    private val lastNameField:TextField

    private val firstNameField:TextField

    private val datacenterGroup:CheckboxGroup<Datacenter>

    private val binder:Binder<OperatorPresentation>

    init {

        userNameField = TextField().apply {
            placeholder = "USERNAME"
        }

        activeField = Checkbox("ABILITATO", true)

        lastNameField = TextField().apply { placeholder = "COGNOME" }

        firstNameField = TextField().apply { placeholder = "NOME" }

        datacenterGroup = CheckboxGroup<Datacenter>("PERMESSI").apply {
            setItems(dcs)
            setItemLabelGenerator { "${it.short} - ${it.fullName}" }

        }

        binder = Binder(OperatorPresentation::class.java, false)

        binder.forField(userNameField)
            .asRequired("Obbligatorio")
            .bind("username")

        binder.forField(lastNameField)
            .asRequired("Obbligatorio")
            .bind("lastname")

        binder.forField(firstNameField)
            .asRequired("Obbligatorio")
            .bind("firstname")

        binder.forField(activeField)
            .bind("active")

        binder.forField(datacenterGroup)
            .bind({it.permissions.toSet()},{op, list ->
                op.permissions.clear()
                op.permissions.addAll(list)
            })
    }

    fun validate() = binder.validate()

    fun compile() : Operator {
        var op = OperatorPresentation()
        binder.writeBean(op)

        return Operator(
            username = op.username!!,
            role = Operator.Role.OPERATOR,
            email = "@",
            isActive = op.active,
            lastName = op.lastname!!,
            firstName = op.firstname!!,
            localPassword = null
        )
    }

    fun reset() {
        binder.readBean(null)
    }
}