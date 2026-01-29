package it.posteitaliane.gdc.gmd.views.suppliers

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.model.Supplier
import it.posteitaliane.gdc.gmd.services.SupplierService
import it.posteitaliane.gdc.gmd.views.MainLayout
import it.posteitaliane.gdc.gmd.views.forms.SupplierForm
import it.posteitaliane.gdc.gmd.views.forms.SupplierForm2
import jakarta.annotation.security.RolesAllowed

@RolesAllowed("ADMIN")
@Route(value = "suppliers", layout = MainLayout::class)
class SuppliersView(
    sups:SupplierService
) : VerticalLayout() {

    private val dialog:Dialog

    private val provider:SupplierDataProvider

    private val filterDataProvider:ConfigurableFilterDataProvider<Supplier, Void, String>

    private val grid:Grid<Supplier>

    private val searchField:TextField

    init {

        provider = SupplierDataProvider(sups)
        filterDataProvider = provider.withConfigurableFilter()

        setHeightFull()

        grid = Grid(Supplier::class.java, false)

        grid.addColumn("name").setHeader("Nome")
        grid.addColumn("piva").setHeader("P.IVA")
        grid.addColumn("legal").setHeader("Indirizzo Legale")

        grid.setItems(filterDataProvider)

        grid.setSortableColumns("name", "piva", "legal")

        searchField = TextField().apply {
            prefixComponent = Icon(VaadinIcon.SEARCH)
            placeholder = "Cerva per Nome, P.Iva, Indirizzo..."
            width = "50%"
            classNames.add("search")
        }

        searchField.addKeyUpListener {
            if (it.key == Key.ENTER) {
                val value = if(searchField.value.isNullOrEmpty()) null else searchField.value
                filterDataProvider.setFilter(value)
            }

            if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                searchField.clear()
                filterDataProvider.setFilter(null)
            }
        }


        val form = SupplierForm(sups)

        dialog = Dialog().apply {
            add(form)
        }

        dialog.header.add(H3("Aggiungi Fornitore"))

        dialog.footer.add(
            Button("ANNULLA").apply {
                addThemeVariants(ButtonVariant.LUMO_ERROR)
                classNames.add(LumoUtility.Margin.Right.AUTO)

                addClickListener {
                    form.reset()
                    dialog.close()
                }
            },
            Button("AGGIUNGI").apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                classNames.add(LumoUtility.Margin.Left.AUTO)

                addClickListener {
                    if( form.validate() ) {
                        val (_, notok) = sups.create( form.compile() )

                        if( notok.isNullOrEmpty() ) {
                            form.reset()
                            dialog.close()
                        }
                    }
                }
            }
        )

        val addSupplierButton = Button("AGGIUNGI FORNITORE").apply {
            addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.MEDIUM)

            addClickListener {
                dialog.open()
            }
        }

        add(HorizontalLayout(searchField, addSupplierButton).apply { setWidthFull() })

        grid.setItemDetailsRenderer(SupplierComponent.createOrderDetails(sups))
        add(grid)
    }

    class SupplierComponent(private val sups:SupplierService): VerticalLayout() {


        companion object {
            fun createOrderDetails(sups: SupplierService) : ComponentRenderer<SupplierComponent, Supplier> {
                return ComponentRenderer({ SupplierComponent(sups) }, SupplierComponent::setSupplier )
            }
        }

        fun setSupplier(s:Supplier?=null) {
            if(s!=null) {
                removeAll()
                val form = SupplierForm2(sups)
                form.setSupplier(s, false)
                add(form)
            }
        }
    }
}