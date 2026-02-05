package it.posteitaliane.gdc.gmd.views.storage

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gmd.model.Storage
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.OperatorService
import it.posteitaliane.gdc.gmd.services.StorageService
import it.posteitaliane.gdc.gmd.services.SupplierService
import it.posteitaliane.gdc.gmd.views.MainLayout
import it.posteitaliane.gdc.gmd.views.forms.OperatorFilterForm
import it.posteitaliane.gdc.gmd.views.forms.StorageFilterForm
import jakarta.annotation.security.PermitAll
import org.slf4j.Logger

@PermitAll
@Route(value = "", layout = MainLayout::class)
class StorageView(
    ss:StorageService,
    dcs:DatacenterService,
    sups: SupplierService,
    ops: OperatorService,
    private val logger:Logger
) : VerticalLayout() {
    fun refresh() {
        grid.dataProvider.refreshAll()
    }

    private val provider: StorageProvider

    private val filterProvider:ConfigurableFilterDataProvider<Storage, Void, StorageFilter>

    val grid: Grid<Storage>

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

//        var gridFilter = StorageFilterForm( filterProvider, dcs, ss)
//        add(gridFilter)
        var filterForm = OperatorFilterForm(filterProvider, dcs, ss, sups, ops).apply {

            rowOne.apply {
                add( makeItems() )
                add( makeShowOthersField() )
                isVisible = true
            }

            rowTwo.apply {
                add( makeDcs())
                isVisible = true
            }

            rowThree.apply {
                add( makeOthers() )
                isVisible = true
            }

        }
        add(filterForm)
        add(grid)
    }
}