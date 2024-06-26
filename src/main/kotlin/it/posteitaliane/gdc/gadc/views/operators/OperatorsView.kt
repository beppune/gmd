package it.posteitaliane.gdc.gadc.views.operators

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.grid.editor.Editor
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.OperatorService
import it.posteitaliane.gdc.gadc.views.MainLayout
import it.posteitaliane.gdc.gadc.views.forms.OperatorForm
import jakarta.annotation.security.RolesAllowed

@RolesAllowed("ADMIN")
@Route(value = "anag", layout = MainLayout::class)
class OperatorsView(
    dcs:DatacenterService,
    private val ops:OperatorService
) : VerticalLayout() {

    private val DCS = dcs.findAll()

    private val personFilter:OperatorFilter

    private val dataProvider:OperatorDataProvider

    private val filterDataProvider: ConfigurableFilterDataProvider<Operator, Void, OperatorFilter>

    val grid: Grid<Operator>

    private val searchField:TextField

    val editor: Editor<Operator>

    private val personBinder: Binder<Operator>

    private val firstNameField: TextField

    private val lastNameField: TextField

    private val emailField: TextField

    private val activeField: Checkbox

    private val permissionsField: CheckboxGroup<Datacenter>

    private val addOperatorButton:Button

    private val opForm:OperatorForm

    private val dialog:Dialog

    init {
        setHeightFull()

        opForm = OperatorForm(dcs.findAll())

        dialog = Dialog().apply {
            isModal = true

            add(opForm)

        }


        addOperatorButton = Button("AGGIUNGI UTENZA").apply {
            addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.MEDIUM)

            addClickListener {
                dialog.open()
            }
        }

        personFilter = OperatorFilter()

        dataProvider = OperatorDataProvider(ops)

        filterDataProvider = dataProvider.withConfigurableFilter()

        grid = Grid()

        editor = grid.editor

        val uidColumn = grid.addColumn(Operator::username, "uid").setHeader("Username")
        val lastNameColumn = grid.addColumn(Operator::lastName, "lastName").setHeader("Cognome")
        val firstNameColumn = grid.addColumn(Operator::firstName, "firstName").setHeader("Nome")
        val emailColumn = grid.addColumn(Operator::email, "email").setHeader("Email")
        val activeColumn = grid.addColumn(operatorStatusRenderer()).setHeader("Active").setWidth("50px")
        val permissionsColumn = grid.addColumn({it.permissions.map(Datacenter::short).joinToString(", ")}).setHeader("Permessi")
        val editColumn = grid.addComponentColumn { operator ->
            val editButton = Button("Edit") {
                    if( editor.isOpen ) {
                        editor.cancel()
                    }
                    grid.editor.editItem(operator) }
            editButton
        }.setWidth("150px").setFlexGrow(0).setHeader(VaadinIcon.EDIT.create())

        grid.setItems(filterDataProvider)

        grid.sort(mutableListOf(GridSortOrder(uidColumn, SortDirection.ASCENDING)))

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

        permissionsField = CheckboxGroup<Datacenter>()
            .apply {
                setItems(DCS)
                setItemLabelGenerator(Datacenter::short)
            }
        personBinder.forField(permissionsField)
            .bind(
                { op ->
                    op.permissions.toSet()
                },{ op, permissions ->
                    op.permissions.clear()
                    op.permissions.addAll(permissions)
                })
        permissionsColumn.editorComponent = permissionsField


        searchField = TextField().apply {
            width = "50%"
            placeholder = "Cerca per username, cognome, nome, email..."
            classNames.add("search")
            prefixComponent = Icon(VaadinIcon.SEARCH)
        }

        searchField.addKeyUpListener {
            if( it.key == Key.ENTER ) {
                personFilter.searchTerm = searchField.value
                filterDataProvider.setFilter(personFilter)
            }



            if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                searchField.clear()
                personFilter.searchTerm = ""
                filterDataProvider.setFilter(null)
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
            ops.update(it.item)
            ops.updatePermissions(it.item, it.item.permissions)
        }

        dialog.footer.add(
            Button("ANNULLA").apply {
                addClassNames(LumoUtility.Margin.Left.MEDIUM, LumoUtility.Margin.Right.AUTO)
                addThemeVariants(ButtonVariant.LUMO_ERROR)
                addClickListener {
                    opForm.reset()
                    dialog.close()
                }
            },

            Button("AGGIUNGI").apply {
                addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.MEDIUM)
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener {
                    val op = if ( opForm.validate().isOk ) opForm.compile()
                    else null

                    if(op==null) {
                        Notification.show("Errore Form")
                        return@addClickListener
                    }

                    val (_, notok) = ops.create(op)

                    if(notok.isNullOrEmpty().not()) {
                        Notification.show(notok)
                        return@addClickListener
                    }

                    grid.dataProvider.refreshAll()
                    opForm.reset()
                    dialog.close()
                }
            }
        )


        add(HorizontalLayout(searchField, addOperatorButton).apply { setWidthFull() })

        add(grid)

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