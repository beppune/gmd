package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gadc.views.operators.OperatorsView
import it.posteitaliane.gdc.gadc.views.orders.OrdersView
import it.posteitaliane.gdc.gadc.views.storage.StorageView

class Main : AppLayout() {

    init {

        val toggle = DrawerToggle()

        val title = H1("Gestione Magazzino Datacenter")
        title.style.set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0")

        val nav = SideNav()
        nav.addItem(SideNavItem("Giacenze", StorageView::class.java))
        nav.addItem(SideNavItem("Ordini", OrdersView::class.java))
        nav.addItem(SideNavItem("Utenze", OperatorsView::class.java))

        val scroller = Scroller(nav)
        scroller.className = LumoUtility.Padding.SMALL

        addToDrawer(scroller)
        addToNavbar(toggle, title)

    }

}