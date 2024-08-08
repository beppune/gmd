package it.posteitaliane.gdc.gadc.config

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.annotation.SpringComponent
import it.posteitaliane.gdc.gadc.CustomErrorHandler

@SpringComponent
class ServletInitializer : VaadinServiceInitListener{
    override fun serviceInit(ev: ServiceInitEvent) {
        println("£££££££££")
        ev.source.addSessionInitListener {
            it.session.errorHandler = CustomErrorHandler()
        }
    }
}