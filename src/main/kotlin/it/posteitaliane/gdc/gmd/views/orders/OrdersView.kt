package it.posteitaliane.gdc.gmd.views.orders

import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.AnchorTarget
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
import it.posteitaliane.gdc.gmd.events.EditOrderEvent
import it.posteitaliane.gdc.gmd.model.Datacenter
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.model.OrderLine
import it.posteitaliane.gdc.gmd.services.DatacenterService
import it.posteitaliane.gdc.gmd.services.OperatorService
import it.posteitaliane.gdc.gmd.services.OrderService
import it.posteitaliane.gdc.gmd.services.StorageService
import it.posteitaliane.gdc.gmd.services.SupplierService
import it.posteitaliane.gdc.gmd.views.MainLayout
import it.posteitaliane.gdc.gmd.views.forms.OperatorFilterForm
import it.posteitaliane.gdc.gmd.views.storage.StorageFilter
import jakarta.annotation.security.PermitAll
import java.time.format.DateTimeFormatter

@PermitAll
@Route(value = "orders", layout = MainLayout::class)
class OrdersView(
    os:OrderService,
    dcs:DatacenterService,
    ss: StorageService,
    sups: SupplierService,
    ops: OperatorService,
) : VerticalLayout() {
    private val isDetailsVisible:MutableList<Order> = mutableListOf()

    private val provider: OrdersProvider

    private val filterProvider:ConfigurableFilterDataProvider<Order, Void, StorageFilter>

    val grid:Grid<Order>

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

        /*val idRenderer = ComponentRenderer { o:Order->

            if( o.filepath.isNullOrEmpty() ) {
                Span("${o.number}")
            } else {
                Span().apply {
                    add(Anchor(o.filepath, o.number.toString()))
                }
            }
        }*/

        provider = OrdersProvider(os)

        filterProvider = provider.withConfigurableFilter()

        grid = Grid(Order::class.java, false)
        //grid.addColumn( idRenderer ).setHeader("Order °")
        grid.addColumn(ComponentRenderer({Span()}, subjectComponent()))
            .setHeader("Tipo").setSortProperty("subject")
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

        val collapseAllButton = Button("CHIUDI TUTTI I DETTAGLI") { _ ->
            val it = isDetailsVisible.iterator()
            while (it.hasNext()) {
                grid.setDetailsVisible(it.next(), false)
                it.remove()
            }
        }

        var filterForm = OperatorFilterForm(filterProvider, dcs, ss, sups, ops).apply {
            rowOne.apply {
                add( makeDate() )
                add( makeOperator() )
                isVisible = true
            }

            rowTwo.apply {
                add( makeOrder() )
                isVisible = true
            }

            rowThree.apply {
                add( makeItems() )
                add( makeDcs() )
                add( makeShowOthersField() )
                isVisible = true
            }

            rowFour.apply {
                add( makeOthers())
                isVisible = true
            }
        }

        add(filterForm)
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

    private fun subjectComponent(): SerializableBiConsumer<Span, Order> {
        return SerializableBiConsumer<Span,Order> { span, order ->
            val theme = "badge"

            if(order.filepath != null) {
                Anchor("docstorage/sample.pdf", order.subject.name).run {
                    setTarget(AnchorTarget.BLANK)
                    isRouterIgnore = true
                    span.add(this)
                }
            } else {
                span.add(order.subject.name)
            }

            span.run {
                val icon = when(order.type) {
                    Order.Type.INBOUND -> Icon(VaadinIcon.DOWNLOAD)
                    Order.Type.OUTBOUND -> Icon(VaadinIcon.UPLOAD)
                }

                add(icon)
            }

            span.element.setAttribute("theme", theme)
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

                    os.fillOrderLines(field!!)
                    val linesGrid = Grid(OrderLine::class.java, false).apply {
                        addComponentAsFirst(Paragraph("CIAONE"))
                        removeAll()
                        addColumn { it.item }.setHeader("Merce").setAutoWidth(true)
                        addColumn { it.position }.setHeader("Position").setAutoWidth(true)
                        addColumn { it.amount }.setHeader("Quantità").setAutoWidth(true)

                        addThemeVariants(GridVariant.LUMO_COMPACT)

                        setWidth(40.0F, Unit.REM)
                        setHeight(15.0F, Unit.REM)

                        setItems(field!!.lines)
                    }
                    val hl = HorizontalLayout(
                        linesGrid,
                        Details("Note", Paragraph(field!!.remarks)).apply { isOpened = true }
                    )


                    if( field!!.status == Order.Status.PENDING ) {
                        val edit = Button("MODIFICA ORDINE") {
                            ComponentUtil.fireEvent(ui.get(), EditOrderEvent(this, false, field!!))
                        }
                        hl.add(edit)
                    }

                    add(hl)
                }
            }

    }


}