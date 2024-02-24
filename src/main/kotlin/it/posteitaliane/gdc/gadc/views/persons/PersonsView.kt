package it.posteitaliane.gdc.gadc.views.persons

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.editor.Editor
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Person
import it.posteitaliane.gdc.gadc.services.PersonService

@Route("anag")
class PersonsView(private val service:PersonService) : VerticalLayout() {

    private val personFilter:PersonFilter

    private val dataProvider:PersonDataProvider

    private val filterDataProvider: ConfigurableFilterDataProvider<Person, Void, PersonFilter>

    val grid: Grid<Person>

    private val searchField:TextField

    val editor: Editor<Person>

    private val personBinder: Binder<Person>

    private val firstNameField: TextField

    private val lastNameField: TextField

    init {

        personFilter = PersonFilter()

        dataProvider = PersonDataProvider(service)

        filterDataProvider = dataProvider.withConfigurableFilter()

        grid = Grid()

        editor = grid.editor

        //grid.addColumn(Person::uuid, "uuid").setHeader("ID")
        val lastNameColumn = grid.addColumn(Person::lastName, "lastName").setHeader("Cognome")
        val firstNameColumn = grid.addColumn(Person::firstName, "firstName").setHeader("Nome")
        val editColumn = grid.addComponentColumn {person ->
            val editButton = Button("Edit") {
                    if( editor.isOpen ) {
                        editor.cancel()
                    }
                    grid.editor.editItem(person) }
            editButton
        }.setWidth("150px").setFlexGrow(0).setHeader(VaadinIcon.EDIT.create())

        grid.setItems(filterDataProvider)

        personBinder = Binder(Person::class.java)
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

        searchField = TextField().apply {
            width = "50%"
            placeholder = "Search"
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

}