package it.posteitaliane.gdc.gadc.security

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.PartialResultException
import javax.naming.directory.InitialDirContext

class ReteAuthenticationProvider(private val db:JdbcTemplate) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        val username:String = authentication.principal as String
        val password:String = authentication.credentials as String

        try {

            var env = Hashtable<String, String>()
                .apply {
                    put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
                    //put(Context.PROVIDER_URL, "ldaps://pretedc01v.rete.poste:636/DC=rete,DC=poste")
                    //put(Context.SECURITY_PROTOCOL, "ssl")
                    put(Context.PROVIDER_URL, "ldap://pretedc01v.rete.poste/DC=rete,DC=poste")
                    put(Context.SECURITY_PRINCIPAL, "RETE\\${username.uppercase()}")
                    put(Context.SECURITY_CREDENTIALS, password)
                    put(Context.SECURITY_AUTHENTICATION, "simple")
                }

            //println("-----ALPHA")
            InitialDirContext(env)

            /*var attrs = SearchControls()
                .apply {
                    returningAttributes = arrayOf("cn", "sn", "givenName", "mail", "proxyAddresses")
                    searchScope = SearchControls.SUBTREE_SCOPE
                }*/

            // for later use
            //var answer = dir.search("", "(&(objectClass=person)(sAMAccountName=MANZOGI9))", attrs)


        }catch (ex: PartialResultException) {
            ex.printStackTrace()
            return null
        }catch(ex: AuthenticationException){
            println("RETE: invalid credentials [RETE\\${username}]")
            return null
        }catch (ex:Exception) {
            ex.printStackTrace()
            return null
        }

        val map = try {
            db.queryForMap("SELECT uid,role,active FROM operators WHERE uid = ?", username)
        }catch (ex:DataAccessException) {
            ex.printStackTrace()
            null
        }

        if( map == null ) {
            println("RETE: Username not registered [${username}]")
            return null
        }

        if( map["active"].toString() == "0" ) {
            println("RETE: Username disabled [${username}]")
            return null
        }

        val roles = listOf(SimpleGrantedAuthority("ROLE_" + map["role"] as String))

        return UsernamePasswordAuthenticationToken(username, password, roles)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}