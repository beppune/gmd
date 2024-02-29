package it.posteitaliane.gdc.gadc.views.orders

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.BackOffice
import java.sql.Date
import java.text.SimpleDateFormat

@Route("orders")
class OrdersView(val BO:BackOffice) : VerticalLayout() {

    private val provider: OrdersProvider

    private val filterProvider:ConfigurableFilterDataProvider<Order, Void, String>

    val grid:Grid<Order>

    private val searchField:TextField

    init {

        provider = OrdersProvider(BO.os)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Order::class.java, false)
        val operatorColumn = grid.addColumn({"${it.op.firstName} ${it.op.lastName}"}, "op")
            .setHeader("Operatore")
        val typeColumn = grid.addColumn({"CARICO INTERNO"}, "type")
            .setHeader("Tipo")
        val datacenterColumn = grid.addColumn({it.dc.fullName}, "dc")
            .setHeader("DC")
        val issuedColumn = grid.addColumn({dateFormatter().format(Date.valueOf(it.issued))}, "issued")
            .setHeader("Data")
        val statusColumn = grid.addColumn(ComponentRenderer({Span()}, statusComponent()))
                .setHeader("Stato")
        val refColumn = grid.addColumn("ref").setHeader("Referente")

        grid.setItems(filterProvider)

        grid.setItemDetailsRenderer(OrderDetailsComponent.createOrderDetails())


        searchField = TextField()
            .apply {
                prefixComponent = Icon(VaadinIcon.SEARCH)
                placeholder = "Cerca per nome utente, datacenter, referente..."
                width = "50%"
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

    fun dateFormatter(): SimpleDateFormat {
        return SimpleDateFormat("dd / MM / yyyy")
    }

    class OrderDetailsComponent: VerticalLayout() {

        companion object {
            fun createOrderDetails() : ComponentRenderer<OrderDetailsComponent, Order> {
                return ComponentRenderer({OrderDetailsComponent()}, OrderDetailsComponent::order.setter)
            }
        }

        var order:Order?=null
            set(value) {
                field = value
                add(value.toString())
            }

    }


}