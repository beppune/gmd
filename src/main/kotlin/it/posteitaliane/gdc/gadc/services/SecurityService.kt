package it.posteitaliane.gdc.gadc.services

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.spring.annotation.SpringComponent
import it.posteitaliane.gdc.gadc.model.Operator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

@SpringComponent
class SecurityService(
    private val db:JdbcTemplate,
    private val ops:OperatorService
) {

    private val LOGOUT_URL = "/"

    fun getAuthenticatedUser() : UserDetails {
        val auth = SecurityContextHolder.getContext().authentication
        val username = auth.principal as String
        val roles = db.queryForObject("SELECT role FROM operators WHERE uid = ?", String::class.java, username)

        return User.withUsername(username)
            .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
            .password("")
            .roles(roles)
            .build()
    }

    fun op() : Operator {
        return ops.find(filter = getAuthenticatedUser().username).first()
    }

    fun logout() {
        UI.getCurrent().page.setLocation(LOGOUT_URL)
        val handler = SecurityContextLogoutHandler()
        handler.logout(VaadinServletRequest.getCurrent().httpServletRequest, null, null)
    }

}