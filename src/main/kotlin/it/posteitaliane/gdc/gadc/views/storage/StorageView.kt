package it.posteitaliane.gdc.gadc.views.storage

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Storage
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.views.MainLayout
import jakarta.annotation.security.PermitAll
import org.slf4j.Logger

@PermitAll
@Route(value = "", layout = MainLayout::class)
class StorageView(
    ss:StorageService,
    dcs:DatacenterService,
    private val logger:Logger
) : VerticalLayout() {
    fun refresh() {
        grid.dataProvider.refreshAll()
    }

    private val provider: StorageProvider

    private val filterProvider:ConfigurableFilterDataProvider<Storage, Void, StorageFilter>

    val grid: Grid<Storage>

    private val searchField:TextField

    private val dcSelect: CheckboxGroup<Datacenter>

    private var storageFilter: StorageFilter

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

        storageFilter = StorageFilter()

        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome merce"
                width = "50%"
                classNames.add("search")
            }

        dcSelect = CheckboxGroup<Datacenter>().apply {
            setItems(dcs.findAll())
            setItemLabelGenerator { it.short }
        }

        searchField.addKeyUpListener {
            if( it.key == Key.ENTER ) {
                storageFilter.key = searchField.value.lowercase().trim()
                filterProvider.setFilter(storageFilter)
                logger.info(storageFilter.toString())
            }

            if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                searchField.clear()
                storageFilter.key = null
                filterProvider.setFilter(storageFilter)
                logger.info(storageFilter.toString())
            }
        }

        dcSelect.addValueChangeListener {
            storageFilter.dcs.clear()
            if(  it.value.size != 0 ) storageFilter.dcs.addAll(it.value)
            filterProvider.setFilter(storageFilter)
            logger.info(storageFilter.toString())
        }

        add(HorizontalLayout(searchField, dcSelect).apply { setWidthFull() })
        add(grid)
    }
}