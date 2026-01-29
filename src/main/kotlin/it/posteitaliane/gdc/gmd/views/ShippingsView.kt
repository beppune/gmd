package it.posteitaliane.gdc.gmd.views

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import it.posteitaliane.gdc.gmd.model.Shipping
import it.posteitaliane.gdc.gmd.services.ShippingService

//@Route(value = "shippings", layout = MainLayout::class)
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