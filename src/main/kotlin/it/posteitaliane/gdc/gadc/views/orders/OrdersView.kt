package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.events.EditOrderEvent
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.views.MainLayout
import java.time.format.DateTimeFormatter

@Route(value = "orders", layout = MainLayout::class)
class OrdersView(
    os:OrderService
) : VerticalLayout() {

    private val provider: OrdersProvider

    private val filterProvider:ConfigurableFilterDataProvider<Order, Void, String>

    val grid:Grid<Order>

    private val searchField:TextField

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

        setHeightFull()

        provider = OrdersProvider(os)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Order::class.java, false)
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
                addKeyUpListener {
                    if( it.key == Key.ENTER) {
                        filterProvider.setFilter(value.trim().lowercase())
                    }
                }
            }


        add(searchField)
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

                    os.fillOrderLines(field!!)
                    field!!.lines.forEach { add(Span( "${it.item} ${it.position} ${it.amount}" +
                            " ${if(it.sn!=null) it.sn else ""}" +
                            " ${if(it.pt!=null) it.pt else ""}"
                    ))}
                }
            }

    }


}