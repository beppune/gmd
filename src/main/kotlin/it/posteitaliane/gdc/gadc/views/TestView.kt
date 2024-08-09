package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.services.SupplierService
import it.posteitaliane.gdc.gadc.views.forms.OrderDetailsForm
import jakarta.annotation.security.RolesAllowed


@Route("test2")
@RolesAllowed("ADMIN")
class TestView(
    private val ss: StorageService,
    private val os:OrderService,
    private val dcs:DatacenterService,
    private val sups:SupplierService
) : Div() {


    init {
        val form = OrderDetailsForm(dcs,sups).apply { setWidth("40%") }
        add(form)
    }


}
