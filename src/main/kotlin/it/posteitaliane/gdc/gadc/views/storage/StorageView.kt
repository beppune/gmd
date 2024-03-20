package it.posteitaliane.gdc.gadc.views.storage

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.Main

@Route(value = "", layout = Main::class)
class StorageView(BO:BackOffice) : VerticalLayout() {

    private val provider: StorageProvider

    private val filterProvider:ConfigurableFilterDataProvider<Storage, Void, String>

    val grid: Grid<Storage>

    private val searchField:TextField

    init {
        provider = StorageProvider(BO.ss)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Storage::class.java, false)
        val dcColumn = grid.addColumn({"${it.dc.fullName} - ${it.dc.fullName}"}, "dc")
            .setHeader("Datacenter")
        val itemColumn = grid.addColumn("item")
            .setHeader("Merce")
        val posColumn = grid.addColumn("pos")
            .setHeader("Position")
        val amountColumn = grid.addColumn("amount")
            .setHeader("Quantità")
        val snColumn = grid.addColumn("sn")
            .setHeader("S/N")

        grid.setItems(filterProvider)

        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome merce"
                width = "50%"
                classNames.add("search")
                addKeyUpListener {
                    if( it.key == Key.ENTER ) {
                        filterProvider.setFilter(value.trim().lowercase())
                    }
                }
            }

        add(searchField)
        add(grid)
    }
}