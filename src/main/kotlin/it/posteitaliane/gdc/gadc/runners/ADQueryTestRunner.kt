package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.config.ADConfig
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*
import javax.naming.Context
import javax.naming.PartialResultException
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls


@Component
@Profile("testldap")
class ADQueryTestRunner(private val config: ADConfig, private val ctx: ApplicationContext) : CommandLineRunner {
    override fun run(vararg args: String?) {

        //example connection string
        // ldaps://pretedc01v.rete.poste:636/DC=rete,DC=poste
        //
        // search filter: (&(objectClass=person)(sAMAccountName=MANZOGI9))
        // search dn    :   DC=rete,DC=poste

        try {

            var env = Hashtable<String, String>()
                .apply {
                    put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
                    //put(Context.PROVIDER_URL, "ldaps://pretedc01v.rete.poste:636/DC=rete,DC=poste")
                    //put(Context.SECURITY_PROTOCOL, "ssl")
                    put(Context.PROVIDER_URL, "ldap://pretedc01v.rete.poste/DC=rete,DC=poste")
                    put(Context.SECURITY_PRINCIPAL, "RETE\\MANZOGI9")
                    put(Context.SECURITY_CREDENTIALS, "2Krum1r1")
                    put(Context.SECURITY_AUTHENTICATION, "simple")
                }

            println("-----ALPHA")
            var dir = InitialDirContext(env)

            var attrs = SearchControls()
                .apply {
                    returningAttributes = arrayOf("cn", "sn", "givenName", "mail", "proxyAddresses")
                    searchScope = SearchControls.SUBTREE_SCOPE
                }

            println("----BETA")
            var answer = dir.search(/*"ou=utenti filiale,ou=filiale 1to,OU=Polo Torino"*/"", "(&(objectClass=person)(sAMAccountName=MANZOGI9))", attrs)

            while (answer.hasMore()) {
                val n = answer.next()
                println(n.attributes.toString())
            }

            println("----GAMAM")


        }catch (ex:PartialResultException) {
            //do nothing
        }catch (ex:Exception) {
            ex.printStackTrace()
        }


        SpringApplication.exit(ctx)
    }
}