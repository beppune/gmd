package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.events.EditOrderEvent
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.*
import it.posteitaliane.gdc.gadc.views.forms.OrderForm
import it.posteitaliane.gdc.gadc.views.operators.OperatorsView
import it.posteitaliane.gdc.gadc.views.orders.OrdersView
import it.posteitaliane.gdc.gadc.views.storage.StorageView
import it.posteitaliane.gdc.gadc.views.transactions.TransactionsView

class  MainLayout(
    config:GMDConfig,
    os:OrderService,
    dcs: DatacenterService,
    sups: SupplierService,
    ss: StorageService,
    ops: OperatorService,
    files:FilesService
) : AppLayout() {

    val dialog:Dialog

    val op:Operator
    init {

        op = ops.findAll().find { it.role == Operator.Role.ADMIN }!!

        val form = OrderForm(config.firmName, dcs, sups, os, ss, ops, files, Order.Type.INBOUND)

        dialog = Dialog()
            .apply {
                setWidthFull()
                setHeightFull()
                add(form)
            }

        dialog.footer.add(
            Button("ANNULLA")
                .apply {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    style.set("margin-inline-end", "auto")
                    addClickListener { dialog.close() }
                },
            Button("Reset")
                .apply {
                    addClickListener {
                        form.reset()
                    }
                },
            Button("Conferma")
                .apply {
                    addClickListener {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)

                        if( form.validate() ) {
                            val o = form.compileOrder()

                            val result = os.submit(o)

                            if(result.isError()) {
                                Notification.show(result.error)
                                println(result.error)
                                return@addClickListener
                            }

                            if( form.savePath.isNullOrEmpty().not() ) {
                                files.updateOrderFile(o, form.savePath!!)
                            }

                            if (content is StorageView) (content as StorageView).refresh()
                            if (content is OrdersView) (content as OrdersView).refresh()

                            dialog.close()

                        } else {
                            print("Order form non valido")
                        }
                    }
                }
        )

        val button = Button("ESEGUI UN'OPERAZIONE") { dialog.open() }

        button.addClassNames(LumoUtility.Margin.Left.MEDIUM)

        val toggle = DrawerToggle()

        val title = H1("Gestione Magazzino Datacenter")
        title.style.set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0")

        val operatorWidget = HorizontalLayout(
            Icon(VaadinIcon.USER),
            Span(op.username)
        ).apply {
            addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.MEDIUM, "pointer")
        }

        val nav = SideNav()
        nav.addItem(SideNavItem("Giacenze", StorageView::class.java))
        nav.addItem(SideNavItem("Ordini", OrdersView::class.java))
        nav.addItem(SideNavItem("Utenze", OperatorsView::class.java))
        nav.addItem(SideNavItem("Transazioni", TransactionsView::class.java))

        val scroller = Scroller(nav)
        scroller.className = LumoUtility.Padding.SMALL

        addToDrawer(scroller)
        addToNavbar(toggle, title, button, operatorWidget)

        addAttachListener {
            ComponentUtil.addListener(ui.get(), EditOrderEvent::class.java) {
                form.editOrder(it.o)
                dialog.header.add(H2("Modifica ordine n. ${it.o.number}"))
                dialog.open()
            }
        }
    }

}