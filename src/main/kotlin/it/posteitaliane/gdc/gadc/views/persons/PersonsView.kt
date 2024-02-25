package it.posteitaliane.gdc.gadc.views.persons

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.editor.Editor
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.services.OperatorService

@Route("anag")
class PersonsView(private val service:OperatorService) : VerticalLayout() {

    private val personFilter:PersonFilter

    private val dataProvider:PersonDataProvider

    private val filterDataProvider: ConfigurableFilterDataProvider<Operator, Void, PersonFilter>

    val grid: Grid<Operator>

    private val searchField:TextField

    val editor: Editor<Operator>

    private val personBinder: Binder<Operator>

    private val firstNameField: TextField

    private val lastNameField: TextField

    private val emailField: TextField

    private val activeField: Checkbox

    init {

        personFilter = PersonFilter()

        dataProvider = PersonDataProvider(service)

        filterDataProvider = dataProvider.withConfigurableFilter()

        grid = Grid()

        editor = grid.editor

        /*val uidColumn = */grid.addColumn(Operator::username, "uid").setHeader("Username")
        val lastNameColumn = grid.addColumn(Operator::lastName, "lastName").setHeader("Cognome")
        val firstNameColumn = grid.addColumn(Operator::firstName, "firstName").setHeader("Nome")
        val emailColumn = grid.addColumn(Operator::email, "email").setHeader("Email")
        val activeColumn = grid.addColumn(operatorStatusRenderer()).setHeader("Active")
        val editColumn = grid.addComponentColumn { operator ->
            val editButton = Button("Edit") {
                    if( editor.isOpen ) {
                        editor.cancel()
                    }
                    grid.editor.editItem(operator) }
            editButton
        }.setWidth("150px").setFlexGrow(0).setHeader(VaadinIcon.EDIT.create())

        grid.setItems(filterDataProvider)

        personBinder = Binder(Operator::class.java)
        editor.binder = personBinder
        editor.setBuffered(true)

        firstNameField = TextField()
        firstNameField.setWidthFull()
        personBinder.forField(firstNameField)
            .asRequired("First Name must not be empty")
            .withStatusLabel(NativeLabel("Error"))
            .bind("firstName")
        firstNameColumn.editorComponent = firstNameField

        lastNameField = TextField()
        lastNameField.setWidthFull()
        personBinder.forField(lastNameField)
            .asRequired("Last Name must not be empty")
            .withStatusLabel(NativeLabel("Error"))
            .bind("lastName")
        lastNameColumn.editorComponent = lastNameField

        emailField = TextField()
        emailField.setWidthFull()
        personBinder.forField(emailField)
            .asRequired("Email must not be empty")
            .withStatusLabel(NativeLabel("Error"))
            .bind("email")
        emailColumn.editorComponent = emailField

        activeField = Checkbox()
        personBinder.forField(activeField)
            .bind({op->op.isActive},{op,isActive->op.isActive=isActive})
        activeColumn.editorComponent = activeField

        searchField = TextField().apply {
            width = "50%"
            placeholder = "Cerca"
            prefixComponent = Icon(VaadinIcon.SEARCH)
            valueChangeMode = ValueChangeMode.EAGER
            addValueChangeListener {
                personFilter.searchTerm = it.value
                filterDataProvider.setFilter(personFilter)

            }
        }

        val saveButton = Button("Save") {
            editor.save()
        }
        val cancelButton = Button(VaadinIcon.CLOSE.create()) { editor.cancel() } .apply {
            addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR)
        }

        val actions = HorizontalLayout(saveButton, cancelButton).apply {
            isPadding = false
        }

        editColumn.setEditorComponent(actions)

        editor.addSaveListener {
            println(it.item)
            service.update(it.item)
        }

        add(searchField, grid)

    }

    companion object {

        fun operatorStatusRenderer() : ComponentRenderer<Span, Operator> {
            val builder = SerializableBiConsumer<Span, Operator> { span, op ->
                val theme = "badge ${if(op.isActive)"success" else "error"}"
                span.element.setAttribute("theme", theme)
                span.text = if(op.isActive)"ACTIVE" else "DISABLED"
            }

            return ComponentRenderer({Span()}, builder)
        }

    }

}