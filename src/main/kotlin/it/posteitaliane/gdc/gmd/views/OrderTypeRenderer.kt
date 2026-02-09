package it.posteitaliane.gdc.gmd.views

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.AnchorTarget
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import it.posteitaliane.gdc.gmd.model.Order

fun orderTypeRenderer(): ComponentRenderer<out Component, Order.Status> =
    ComponentRenderer { status ->
        val theme = "badge " + when (status) {
            Order.Status.PENDING -> ""
            Order.Status.COMPLETED -> "success"
            Order.Status.CANCELED -> "error"
        }

        val span = Span()
        span.element.setAttribute("theme", theme)

        span.text = when (status) {
            Order.Status.PENDING -> "IN CORSO"
            Order.Status.COMPLETED -> "CHIUSO"
            Order.Status.CANCELED -> "ANNULATO"
        }
        return@ComponentRenderer span
    }

fun orderSubjectComponent(subject: Order.Subject): ComponentRenderer<out Component, Order.Subject> =
    ComponentRenderer { subject ->
        val theme = "badge"
        val span = Span()

//        span.run {
//            val icon = when(subject) {
//                Order.Type.INBOUND -> Icon(VaadinIcon.DOWNLOAD)
//                Order.Type.OUTBOUND -> Icon(VaadinIcon.UPLOAD)
//            }
//
//            add(icon)
//        }

        span.element.setAttribute("theme", theme)

        return@ComponentRenderer span
    }

fun orderTypeLabel(o:Order.Type): String {
    val label = when(o) {
        Order.Type.INBOUND -> "CARICO"
        Order.Type.OUTBOUND -> "SCARICO"
    }
    return label
}