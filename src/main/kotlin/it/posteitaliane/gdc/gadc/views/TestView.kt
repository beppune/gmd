package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

data class Info(var office:String, var score:Int?, var sn:String?)
data class Person(var name:String?=null)

class PersonField : CustomField<Person>() {

    private val nameField:TextField = TextField()

    init {
        add(nameField)
    }

    override fun setPresentationValue(person: Person?) {
        if( person != null ) {
            nameField.value = person.name
        }
    }

    override fun generateModelValue(): Person = Person(nameField.value)



}

@Route("test2")
@AnonymousAllowed
class TextView : Div() {

    private val binder = Binder(Person::class.java)

    private val pField = PersonField()

    init {

        binder.forField(pField)
            .asRequired()
            .bind({it}, {it, value->it.name=value.name})

        add(
            pField,
            Button("Validate") {
                binder.validate()
            },
            Button("Clear") {
                binder.bean = Person()
            }
        )
    }

}
