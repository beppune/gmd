package it.posteitaliane.gdc.gadc.views.transactions

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Transaction
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.ReportService
import it.posteitaliane.gdc.gadc.services.TransactionsService
import it.posteitaliane.gdc.gadc.views.MainLayout
import java.io.InputStream
import java.time.format.DateTimeFormatter

@Route(value = "transactions", layout = MainLayout::class)
class TransactionsView(
    dcs:DatacenterService,
    trs:TransactionsService,

    private val rpt:ReportService
) : VerticalLayout() {

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

        provider = TransactionDataProvider(trs)

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
                setItems(dcs.findAll())
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

            val reportButton = Button("Report Excel") {
                val f = TransactionFilter(dc = dcSelect.value, from = fromPicker.value?.atStartOfDay())
                println("VIEW: $filter")
                val sr = StreamResource("Report Transazioni.xlsx", InputStreamFactory {transactionReportStream(f)})
                sr.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                val hidden = Anchor(sr, "Report Excel").apply {
                    element.setAttribute("style", "display:none")
                    element.classList.add("report_anchor")
                }

                UI.getCurrent().element.appendChild(hidden.element)
                UI.getCurrent().page.executeJs("$0.click()", hidden.element)
            }

            UI.getCurrent().addAfterNavigationListener {
                val els = UI.getCurrent().element.children.filter { el -> el.classList.contains("report_anchor") }
                UI.getCurrent().element.removeChild(els.toList())
            }

            add(dcSelect,fromPicker, toPicker)
            add(reportButton)
            add(clearButton)
        }

        add(filters)
        add(grid)
    }

    private fun transactionReportStream(filter:TransactionFilter) : InputStream {
        println("METHOD: $filter")
        return rpt.runreport(filter)
    }

}