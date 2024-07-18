package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.config.ADConfig
import it.posteitaliane.gdc.gadc.model.Operator
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Component
import javax.naming.directory.Attributes

data class SamaAccount(
    val firstName:String,
    val lastName:String,
    val username:String,
    val emails:List<String>
)

class SamaMapper : AttributesMapper<SamaAccount> {
    override fun mapFromAttributes(attrs: Attributes?): SamaAccount {

        return SamaAccount(
            firstName = attrs?.get("givenName") as String,
            lastName = attrs?.get("sn") as String,
            emails = attrs?.get("proxyAddresses") as List<String>,
            username = attrs?.get("cn") as String
        )

    }

}

@Component
@Profile("test")
class ADQueryTestRunner(private val config:ADConfig, private val ctx:ApplicationContext) : CommandLineRunner {
    override fun run(vararg args: String?) {

        //example connection string
        // ldaps://pretedc01v.rete.poste:636/DC=rete,DC=poste
        //
        // search filter: (&(objectClass=person)(sAMAccountName=MANZOGI9))
        // search dn    :   DC=rete,DC=poste

        val ldapCtx = LdapContextSource()
            .apply {
                setUrl("ldaps://${config.serverName}:636")
                setBase(config.baseDN)
                userDn = "RETE\\MANZOGI9"
                password = "2Krum1r1"
                authenticationSource
            }

        val ldap = LdapTemplate(ldapCtx)

        var q = query()
            .base(config.baseDN)
            .filter("(&(objectClass=person)(sAMAccountName=${ldapCtx.userDn}))")

        ldap.search(q, SamaMapper()).also(::println)

        SpringApplication.exit(ctx)
    }
}