package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.events.EditOrderEvent
import it.posteitaliane.gdc.gadc.model.Datacenter
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.views.MainLayout
import jakarta.annotation.security.PermitAll
import java.time.format.DateTimeFormatter

@PermitAll
@Route(value = "orders", layout = MainLayout::class)
class OrdersView(
    os:OrderService,
    dcs:DatacenterService
) : VerticalLayout() {

    private val provider: OrdersProvider

    private val filterProvider:ConfigurableFilterDataProvider<Order, Void, OrdersFilter>

    val grid:Grid<Order>

    private val searchField:TextField

    private val dcSelect:CheckboxGroup<Datacenter>

    private val ordersFilter:OrdersFilter

    private fun makeTypeLabel(o:Order): String {
        var label =""
        label += when(o.type) {
            Order.Type.INBOUND -> "CARICO"
            Order.Type.OUTBOUND -> "SCARICO"
        }

        label += when(o.subject) {
            Order.Subject.INTERNAL -> " INTERNO"
            Order.Subject.SUPPLIER -> " DA FORNITORE"
            Order.Subject.SUPPLIER_DC -> " DA MOVING"
        }
        return label
    }



    init {

        ordersFilter = OrdersFilter()

        setHeightFull()

        val idRenderer = ComponentRenderer { o:Order->

            if( o.filepath.isNullOrEmpty() ) {
                Span("${o.number}")
            } else {
                Span().apply {
                    add(Anchor(o.filepath, o.number.toString()))
                }
            }
        }

        provider = OrdersProvider(os)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Order::class.java, false)
        grid.addColumn( idRenderer ).setHeader("Order °")
        grid.addColumn({"${it.op.firstName} ${it.op.lastName}"}, "operator")
            .setHeader("Operatore")
        grid.addColumn({makeTypeLabel(it)}, "type")
            .setHeader("Tipo")
        grid.addColumn({it.dc.fullName}, "datacenter")
            .setHeader("DC")
        val issuedColumn = grid.addColumn({dateFormatter().format(it.issued)}, "issued")
            .setHeader("Data")
        grid.addColumn(ComponentRenderer({Span()}, statusComponent()))
                .setHeader("Stato").setSortProperty("status")
        grid.addColumn("ref").setHeader("Referente").setSortProperty("ref")

        grid.setItems(filterProvider)

        grid.sort(mutableListOf(GridSortOrder(issuedColumn, SortDirection.DESCENDING)))

        grid.setItemDetailsRenderer(OrderDetailsComponent.createOrderDetails(os))

        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome utente, datacenter, referente..."
                width = "50%"
                classNames.add("search")

            }
            searchField. addKeyUpListener {
                    if( it.key == Key.ENTER) {
                        ordersFilter.searchKery = searchField.value.trim().lowercase()
                        filterProvider.setFilter(ordersFilter)
                    }


                    if( it.key.toString() == "Escape" || it.key.toString() == "Delete" ) {
                        searchField.clear()
                        filterProvider.setFilter(null)
                    }
                }

        dcSelect = CheckboxGroup<Datacenter>().apply {
            setItems(dcs.findAll())
            setItemLabelGenerator { it.short }

            addValueChangeListener {
                ordersFilter.dcs.clear()
                ordersFilter.dcs.addAll(it.value)

                filterProvider.setFilter(ordersFilter)
            }
        }

        add(HorizontalLayout(searchField, dcSelect).apply { setWidthFull() })
        add(grid)
    }

    private fun statusComponent(): SerializableBiConsumer<Span, Order> {
        return SerializableBiConsumer<Span, Order> { span, order ->
            val theme = "badge " + when(order.status){
                Order.Status.PENDING -> ""
                Order.Status.COMPLETED -> "success"
                Order.Status.CANCELED -> "error"
            }

            span.element.setAttribute("theme", theme)

            span.text = when(order.status){
                Order.Status.PENDING-> "IN CORSO"
                Order.Status.COMPLETED -> "CHIUSO"
                Order.Status.CANCELED -> "ANNULATO"
            }
        }
    }

    fun dateFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")
    }

    fun refresh() {
        grid.dataProvider.refreshAll()
        /*val o = grid.dataCommunicator.getItem(1)
        grid.select(o)*/
    }

    class OrderDetailsComponent(
        private val os:OrderService
    ) : VerticalLayout() {

        companion object {
            fun createOrderDetails(os:OrderService) : ComponentRenderer<OrderDetailsComponent, Order> {
                return ComponentRenderer({OrderDetailsComponent(os)}, OrderDetailsComponent::order.setter)
            }
        }

        var order:Order?=null
            set(value) {
                field = value

                if(field != null) {

                    if( field!!.status == Order.Status.PENDING ) {
                        val edit = Button("MODIFICA ORDINE") {
                            ComponentUtil.fireEvent(ui.get(), EditOrderEvent(this, false, field!!))
                        }
                        add(edit)
                    }

                    add(Details("Note", Paragraph(field!!.remarks)))

                    os.fillOrderLines(field!!)
                    field!!.lines.forEach { add(Span( "${it.item} ${it.position} ${it.amount}" +
                            " ${if(it.sn!=null) it.sn else ""}" +
                            " ${if(it.pt!=null) it.pt else ""}"
                    ))}
                }
            }

    }


}