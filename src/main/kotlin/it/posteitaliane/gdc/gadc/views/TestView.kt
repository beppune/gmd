package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.services.DatacenterService
import it.posteitaliane.gdc.gadc.services.OrderService
import it.posteitaliane.gdc.gadc.services.StorageService
import it.posteitaliane.gdc.gadc.views.forms.ShippingForm
import jakarta.annotation.security.RolesAllowed


@Route("test2")
@RolesAllowed("ADMIN")
class TestView(
    private val ss: StorageService,
    private val os:OrderService,
    private val dcs:DatacenterService
) : Div() {


    init {
        val form = ShippingForm().apply { setWidth("40%") }
        val button = Button("RESET") {
            form.reset()
        }
        val button2 = Button("OK") {
            val e = form.validate()
            if(e.hasErrors()) {
                return@Button
            }

            val c = form.compile()
            Notification.show(c.toString())
        }
        add(form,button,button2)
    }


}
