package it.posteitaliane.gdc.gmd.views.forms

import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Operator

data class OperatorPresentation(
    var username:String?=null,
    var firstname:String?=null,
    var lastname:String?=null,
    var email:String?=null,
    var active:Boolean=true,
    var role:Operator.Role=Operator.Role.OPERATOR,
    val permissions:MutableList<Datacenter> = mutableListOf(),
    var localPassword:String?=null,
)

class OperatorForm(private val dcs:List<Datacenter>) : FormLayout() {

    private val userNameField:TextField

    private val activeField:Checkbox

    private val roleSelect:Select<Operator.Role>

    private val emailField:EmailField

    private val lastNameField:TextField

    private val firstNameField:TextField

    private val datacenterGroup:CheckboxGroup<Datacenter>

    private val localPasswordField: PasswordField

    private val binder:Binder<OperatorPresentation>

    init {

        userNameField = TextField().apply {
            placeholder = "USERNAME"
        }

        emailField = EmailField().apply { placeholder = "@" }

        activeField = Checkbox("ABILITATO", true)

        roleSelect = Select<Operator.Role>().apply {
            prefixComponent = Span("RUOLO")
            setItems(Operator.Role.entries)

            value = Operator.Role.OPERATOR
        }

        lastNameField = TextField().apply { placeholder = "COGNOME" }

        firstNameField = TextField().apply { placeholder = "NOME" }

        datacenterGroup = CheckboxGroup<Datacenter>("PERMESSI").apply {
            setItems(dcs)
            setItemLabelGenerator { "${it.short} - ${it.fullName}" }

        }

        localPasswordField = PasswordField()
            .apply {
                placeholder = "LOCAL DB PASSWORD"
                isEnabled = true
                isReadOnly = false
                isRequired = false
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

        binder.forField(emailField)
            .asRequired("Obbligatorio")
            .bind("email")

        binder.forField(activeField)
            .bind("active")

        binder.forField(roleSelect)
            .asRequired("Obbligatorio")
            .bind("role")

        binder.forField(datacenterGroup)
            .bind({it.permissions.toSet()},{op, list ->
                op.permissions.clear()
                op.permissions.addAll(list)
            })

        binder.forField(localPasswordField)
            .bind("localPassword")
            /*.bind(
                {""},
                { bean, value -> bean.localPassword = value }
            )*/

        add(userNameField, activeField)
        add(emailField, 2)
        add(roleSelect, 2)
        add(lastNameField)
        add(firstNameField)
        add(datacenterGroup, 2)
        add(localPasswordField)

        width = "500px"

    }

    fun validate() = binder.validate()

    fun compile() : Operator {
        var op = OperatorPresentation()
        binder.writeBean(op)

        return Operator(
            username = op.username!!,
            role = Operator.Role.OPERATOR,
            email = op.email!!,
            isActive = op.active,
            lastName = op.lastname!!,
            firstName = op.firstname!!,
            localPassword = op.localPassword
        ).apply {
            permissions.addAll(datacenterGroup.value)
        }
    }

    fun reset() {
        binder.readBean(null)
    }
}