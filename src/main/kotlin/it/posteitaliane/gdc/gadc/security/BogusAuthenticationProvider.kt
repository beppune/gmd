package it.posteitaliane.gdc.gadc.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class BogusAuthenticationProvider(): AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val user = authentication.principal as String
        val pass = authentication.credentials as String
        val granted = mutableListOf<SimpleGrantedAuthority>()

        println("====BOGUS====")

        return UsernamePasswordAuthenticationToken(user, pass, granted)
    }

    override fun supports(authentication: Class<*>) =
        authentication == UsernamePasswordAuthenticationToken::class.java
}