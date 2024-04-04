package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Shipping
import it.posteitaliane.gdc.gadc.services.ShippingService

@Route(value = "shippings", layout = MainLayout::class)
class ShippingsView(
    private val shs:ShippingService
) : VerticalLayout() {

    private val grid:Grid<Shipping>

    init {

        grid = Grid(Shipping::class.java, false)

        grid.addColumn({it.number ?: "-"})
            .setHeader("Numero")

        grid.addColumn({it.issued})
            .setHeader("Ultima Modifica")

        grid.addColumn({it.order.number})
            .setHeader("Ordine")

        grid.addColumn({"${it.order.dc.short} - ${it.order.dc.fullName}"})
            .setHeader("Datacenter")

        grid.addColumn({"${it.order.type}"})
            .setHeader("Tipo")

        grid.addColumn({it.order.supplier.name})
            .setHeader("Fornitore")

        grid.setItems(shs.findAll())

        add(grid)
    }

}