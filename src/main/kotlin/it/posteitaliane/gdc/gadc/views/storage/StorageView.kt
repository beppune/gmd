package it.posteitaliane.gdc.gadc.views.storage

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.views.MainLayout

@Route(value = "", layout = MainLayout::class)
class StorageView(
    ss:StorageService
) : VerticalLayout() {
    fun refresh() {
        grid.dataProvider.refreshAll()
    }

    private val provider: StorageProvider

    private val filterProvider:ConfigurableFilterDataProvider<Storage, Void, String>

    val grid: Grid<Storage>

    private val searchField:TextField

    init {
        setHeightFull()
        provider = StorageProvider(ss)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Storage::class.java, false)
        grid.addColumn({"${it.dc.short} - ${it.dc.fullName}"}, "dc")
            .setHeader("Datacenter")
        val itemColumn = grid.addColumn("item")
            .setHeader("Merce")
        grid.addColumn("pos")
            .setHeader("Position")
        grid.addColumn("amount")
            .setHeader("Quantità")
        grid.addColumn("sn")
            .setHeader("S/N")
        grid.addColumn("pt")

        grid.setItems(filterProvider)

        grid.sort(mutableListOf(GridSortOrder(itemColumn, SortDirection.ASCENDING)))

        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome merce"
                width = "50%"
                classNames.add("search")
            }

        searchField.addKeyUpListener {
            if( it.key == Key.ENTER ) {
                filterProvider.setFilter(searchField.value.lowercase().trim())
            }

            if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                searchField.clear()
                filterProvider.setFilter(null)
            }
        }

        add(searchField)
        add(grid)
    }
}