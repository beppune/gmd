package it.posteitaliane.gdc.gadc.views.transactions

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.MainLayout
import java.time.format.DateTimeFormatter

@Route(value = "transactions", layout = MainLayout::class)
class TransactionsView(BO:BackOffice) : VerticalLayout() {
    fun refresh() {
        grid.dataProvider.refreshAll()
    }

    private var filter:TransactionFilter

    private val provider: TransactionDataProvider

    private val filterProvider:ConfigurableFilterDataProvider<Transaction, Void, TransactionFilter>

    val grid: Grid<Transaction>

    val filters:HorizontalLayout

    val defaultFormatter:DateTimeFormatter

    init {
        setHeightFull()

        filter = TransactionFilter()

        defaultFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")

        provider = TransactionDataProvider(BO.trs)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Transaction::class.java, false)

        grid.addColumn("operator").setSortProperty("operator")
        grid.addColumn("type").isSortable = false
        val timeStampColumn = grid.addColumn({defaultFormatter.format(it.timestamp)}).setHeader("Data").setSortProperty("timestamp")
        grid.addColumn("item").setSortProperty("item")
        grid.addColumn("dc").setSortProperty("dc")
        grid.addColumn("pos").setSortProperty("dc")
        grid.addColumn("amount").isSortable = false
        grid.addColumn("sn").isSortable = false
        grid.addColumn("pt").isSortable = false

        grid.setItems(filterProvider)

        grid.sort(mutableListOf(GridSortOrder(timeStampColumn, SortDirection.DESCENDING)))

        filters = HorizontalLayout().apply {

            val dcSelect = Select<Datacenter>().apply {
                placeholder = "Datacenter"
                setItemLabelGenerator { dc -> "${dc.short} - ${dc.fullName}" }
                setItems(BO.dcs.findAll())
                addValueChangeListener {
                    if(it.value != null) {
                        filter.dc = it.value
                        filterProvider.setFilter(filter)
                    }
                }
            }

            val fromPicker = DatePicker().apply {
                placeholder = "Dal"
                classNames.add(LumoUtility.Margin.Left.MEDIUM)
                addValueChangeListener {
                    if(it.value != null) {
                        filter.from = it.value.atStartOfDay()
                        filterProvider.setFilter(filter)
                    }
                }
            }

            val toPicker = DatePicker().apply {
                placeholder = "Al"
                classNames.add(LumoUtility.Margin.Left.MEDIUM)
                addValueChangeListener {
                    if(it.value != null) {
                        filter.to = it.value.atTime(23, 59, 59)
                        filterProvider.setFilter(filter)
                    }
                }
            }

            val clearButton = Button("Annulla Filtro") {
                dcSelect.clear()
                fromPicker.clear()
                toPicker.clear()
                classNames.add(LumoUtility.Margin.Left.MEDIUM)
                filter = TransactionFilter()
                filterProvider.setFilter(null)
            }

            setWidthFull()

            add(dcSelect,fromPicker, toPicker)
            add(clearButton)
        }

        add(filters)
        add(grid)
    }

    fun reloadStorage() {
        grid.dataProvider.refreshAll()
    }
}