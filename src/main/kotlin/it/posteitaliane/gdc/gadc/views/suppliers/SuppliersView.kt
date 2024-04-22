package it.posteitaliane.gdc.gadc.views.suppliers

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.ErrorEvent
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.VaadinSession
import it.posteitaliane.gdc.gadc.model.Supplier
import it.posteitaliane.gdc.gadc.services.SupplierService
import it.posteitaliane.gdc.gadc.views.MainLayout
import java.util.stream.Stream

class SupplierDataProvider(
    private val sups:SupplierService
) : AbstractBackEndDataProvider<Supplier, String>() {
    override fun fetchFromBackEnd(p0: Query<Supplier, String>?): Stream<Supplier> {
        return sups.findAll().stream()
    }

    override fun sizeInBackEnd(p0: Query<Supplier, String>?): Int {
        return 1000
    }

}

class CustomErrorHandler : ErrorHandler {
    override fun error(p0: ErrorEvent?) {
        if (p0 != null) {
            p0.throwable.printStackTrace()
        }
    }

}

@Route(value = "suppliers", layout = MainLayout::class)
class SuppliersView(
    sups:SupplierService
) : VerticalLayout() {

    private val provider:SupplierDataProvider

    private val filterDataProvider:ConfigurableFilterDataProvider<Supplier, Void, String>

    private val grid:Grid<Supplier>
    init {

        VaadinSession.getCurrent().errorHandler = CustomErrorHandler()

        provider = SupplierDataProvider(sups)
        filterDataProvider = provider.withConfigurableFilter()

        setHeightFull()

        grid = Grid(Supplier::class.java, false)

        grid.addColumn("name").setHeader("Nome")
        grid.addColumn("piva").setHeader("P.IVA")
        grid.addColumn("legal").setHeader("Indirizzo")

        //grid.setItems(sups.findAll(true))
        grid.setItems(filterDataProvider)

        grid.setSortableColumns("name", "piva", "legal")

        add(grid)
    }
}