package it.posteitaliane.gdc.gadc

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.spring.annotation.SpringComponent
import it.posteitaliane.gdc.gadc.model.Operator
import it.posteitaliane.gdc.gadc.services.OperatorService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

@SpringComponent
class SecurityService(
    private val ops:OperatorService
) {

    private val LOGOUT_URL = "/"

    fun getAuthenticatedUser() : UserDetails? {
        val principal = SecurityContextHolder
            .getContext()
            .authentication.principal

        if( principal is UserDetails ) {
            return principal
        }

        return null
    }

    fun op() : Operator {
        return ops.find(filter = getAuthenticatedUser()!!.username).first()
    }

    fun logout() {
        UI.getCurrent().page.setLocation(LOGOUT_URL)
        val handler = SecurityContextLogoutHandler()
        handler.logout(VaadinServletRequest.getCurrent().httpServletRequest, null, null)
    }

}