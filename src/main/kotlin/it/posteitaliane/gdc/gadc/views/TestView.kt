package it.posteitaliane.gdc.gadc.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.router.Route
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.services.*
import it.posteitaliane.gdc.gadc.views.forms.OrderDetailsForm
import jakarta.annotation.security.RolesAllowed


@Route("test2")
@RolesAllowed("ADMIN")
class TestView(
    private val ss: StorageService,
    private val os:OrderService,
    private val dcs:DatacenterService,
    private val sups:SupplierService,
    private val sec:SecurityService,
    private val files: FilesService,
    private val conf: GMDConfig,
) : Div() {


    init {
        val form = OrderDetailsForm(
            dcs = sec.op().permissions,
            sups = sups.findAll(true),
            firm = sups.findAll().filter { it.piva == conf.firmPiva }.first()
        )
        form.addTypeChangeListener {
            Notification.show(it.type.name)
        }
        form.addFileUploadListener {
            files.copyTemp(sec.op().username, it.stream)
        }
        add(form)

        val ok = Button("OK") { form.validate() }
        val reset = Button("RESET") { form.reset() }
        add(reset, ok)
    }


}
