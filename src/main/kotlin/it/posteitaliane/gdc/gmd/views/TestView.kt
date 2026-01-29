package it.posteitaliane.gdc.gmd.views

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H5
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.Route
import com.vaadin.flow.theme.lumo.LumoUtility
import it.posteitaliane.gdc.gmd.config.GMDConfig
import it.posteitaliane.gdc.gmd.model.Order
import it.posteitaliane.gdc.gmd.services.*
import it.posteitaliane.gdc.gmd.views.forms.OrderDetailsForm
import it.posteitaliane.gdc.gmd.views.forms.ShippingForm
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

    private var filename:String?=null

    init {
        val shipping = ShippingForm()
        val form = OrderDetailsForm(
            dcs = sec.op().permissions,
            sups = sups.findAll(true),
            firm = sups.findAll().filter { it.piva == conf.firmPiva }.first()
        )
        form.addFileUploadListener {
            filename = files.copyTemp(sec.op().username, it.stream)
        }

        val sd = VerticalLayout(
            H5("DETTAGLI SPEDIZIONE").apply { style.setFontWeight(Style.FontWeight.BOLDER) },
            shipping
        ).apply {
            isPadding = false
            addClassNames(LumoUtility.Gap.SMALL)
        }

        add(form)

        form.addSubjectChangeListener {
            if( it.subject != Order.Subject.INTERNAL) {
                addComponentAtIndex(1, sd)
            } else {
                remove(sd)
            }
        }

        val ok = Button("OK") {
            if( form.validate().isOk ) {
                form.compile().also {
                    filename = it?.filename
                    Notification.show(it.toString())
                }
            }
        }
        val reset = Button("RESET") { form.reset() }
        add(reset, ok)
    }


}
