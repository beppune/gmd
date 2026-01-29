package it.posteitaliane.gdc.gmd

import com.vaadin.flow.server.ErrorEvent
import com.vaadin.flow.server.ErrorHandler

class CustomErrorHandler: ErrorHandler {
    override fun error(er: ErrorEvent) {
        er.throwable.printStackTrace()
    }
}

