package it.posteitaliane.gdc.gadc.security

import it.posteitaliane.gdc.gadc.services.OperatorService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.PartialResultException
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls

class ADSelfAuthenticationProvider(private val ops:OperatorService) : AbstractUserDetailsAuthenticationProvider() {

    override fun authenticate(authentication: Authentication?): Authentication? {
        val username:String = authentication!!.principal as String
        val password:String = authentication!!.credentials as String

        try {

            var env = Hashtable<String, String>()
                .apply {
                    put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
                    //put(Context.PROVIDER_URL, "ldaps://pretedc01v.rete.poste:636/DC=rete,DC=poste")
                    //put(Context.SECURITY_PROTOCOL, "ssl")
                    put(Context.PROVIDER_URL, "ldap://pretedc01v.rete.poste/DC=rete,DC=poste")
                    put(Context.SECURITY_PRINCIPAL, "RETE\\$username")
                    put(Context.SECURITY_CREDENTIALS, password)
                    put(Context.SECURITY_AUTHENTICATION, "simple")
                }

            println("-----ALPHA")
            var dir = InitialDirContext(env)

            var attrs = SearchControls()
                .apply {
                    returningAttributes = arrayOf("cn", "sn", "givenName", "mail", "proxyAddresses")
                    searchScope = SearchControls.SUBTREE_SCOPE
                }

            // for later use
            //var answer = dir.search("", "(&(objectClass=person)(sAMAccountName=MANZOGI9))", attrs)


        }catch (ex: PartialResultException) {
            //do nothing
        }catch(ex: AuthenticationException){
            return null
        }catch (ex:Exception) {
            ex.printStackTrace()
            return null
        }

        val auth = UsernamePasswordAuthenticationToken(username, password)

        return auth
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!!.equals(this::class.java)
    }

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?
    ) {
        //no more checks
    }

    override fun retrieveUser(username: String, authentication: UsernamePasswordAuthenticationToken): UserDetails {
        val op = ops.get(username)

        return User.withUsername(op.username)
            .roles(op.role.name)
            .password(authentication.credentials as String)
            .build()
    }

}