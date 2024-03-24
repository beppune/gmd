package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H1
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
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.BackOffice
import it.posteitaliane.gdc.gadc.views.forms.OrderForm
import it.posteitaliane.gdc.gadc.views.operators.OperatorsView
import it.posteitaliane.gdc.gadc.views.orders.OrdersView
import it.posteitaliane.gdc.gadc.views.storage.StorageView

class MainLayout(op: Operator, bo:BackOffice, config:GMDConfig) : AppLayout() {

    val dialog:Dialog
    init {

        val form = OrderForm(bo, config.firmName, Order.Type.INBOUND)

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
                    style.set("margin-inline-end", "auto");
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
                            Notification.show("Service", 2, Notification.Position.TOP_CENTER)
                            form.compileOrder().also {
                                println(it)
                                println(it.lines)
                            }
                            dialog.close()
                        }
                    }
                }
        )

        val button = Button("CARICO") { dialog.open() }

        val toggle = DrawerToggle()

        val title = H1("Gestione Magazzino Datacenter")
        title.style.set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0")

        var operatorWidget = HorizontalLayout(
            Icon(VaadinIcon.USER),
            Span(op.username)
        ).apply {
            addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.MEDIUM, "pointer")
        }

        val nav = SideNav()
        nav.addItem(SideNavItem("Giacenze", StorageView::class.java))
        nav.addItem(SideNavItem("Ordini", OrdersView::class.java))
        nav.addItem(SideNavItem("Utenze", OperatorsView::class.java))

        val scroller = Scroller(nav)
        scroller.className = LumoUtility.Padding.SMALL

        addToDrawer(scroller)
        addToNavbar(toggle, title, button, operatorWidget)


    }

}