package it.posteitaliane.gdc.gadc.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class TextAuthenticationProvider(private val config:TextAuthenticationConfig) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {

        val username = authentication.principal
        val password = authentication.credentials

        if(config.enable.not()) {
            println("TEXTAUTH: not enabled")
            return null
        }

        if( password != config.password || username != config.username ) {
            println("TEXTAUTH: wrong username/password")
            return null
        }


        println("TEXTAUTH: authenticating [${username}] with role [${config.role}]")
        val roles = listOf(SimpleGrantedAuthority("ROLE_" + config.role))
        return UsernamePasswordAuthenticationToken(username, password, roles)

    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}