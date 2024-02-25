package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Datacenter

@Route
class Main : VerticalLayout() {

    private fun getDcs() : List<Datacenter> = listOf(
        Datacenter("TO1", "TORINO").apply { locations = mutableListOf("SALA 1", "SALA 2", "MAGAZZINO") },
        Datacenter("RM1", "EUROPA").apply { locations = mutableListOf("SALA 1", "SALA 2") },
        Datacenter("RMB", "CONGRESSI").apply { locations = mutableListOf("HH1", "HH2") }
    )

    init {

        val grid = Grid(Datacenter::class.java, false) .apply {
            addColumn(Datacenter::short).setHeader("DC")
            addColumn(Datacenter::fullName).setHeader("Nome")
            addColumn({it.locations.joinToString(", ")}).setHeader("Locali")

            setItems(getDcs())
        }

        add(grid)


    }

}