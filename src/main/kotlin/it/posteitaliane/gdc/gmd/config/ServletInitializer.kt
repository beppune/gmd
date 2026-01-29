package it.posteitaliane.gdc.gmd.config

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.spring.annotation.SpringComponent
import it.posteitaliane.gdc.gmd.CustomErrorHandler

@SpringComponent
class ServletInitializer : VaadinServiceInitListener{
    override fun serviceInit(ev: ServiceInitEvent) {
        ev.source.addSessionInitListener {
            it.session.errorHandler = CustomErrorHandler()
        }
    }
}