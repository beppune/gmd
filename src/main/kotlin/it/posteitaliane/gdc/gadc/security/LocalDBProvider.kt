package it.posteitaliane.gdc.gadc.security

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder

class LocalDBProvider(private val db:JdbcTemplate, private val pe:PasswordEncoder) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {

        val username = authentication.principal as String
        val password = authentication.principal as String

        val map = try {
            db.queryForMap("SELECT uid,role,active,localpassword FROM operators WHERE UID = ?", username)
        } catch (_:DataAccessException) {
            null
        }

        if( map == null ) {
            println("LOCALDB: Username not FOUND [${username}]")
            return null
        }

        if( pe.matches(password, "{noop}${map["localpassword"] as String}") ) {
            println("LOCALDB: Username not registered [${username}]")
            return null
        }

        if( map["active"].toString() == "0" ) {
            println("LOCALDB: Username disabled [${username}]")
            return null
        }

        val roles = listOf(SimpleGrantedAuthority("ROLE_" + map["role"] as String))

        return UsernamePasswordAuthenticationToken(username, password, roles)

    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}