package it.posteitaliane.gdc.gadc

import com.vaadin.flow.spring.security.VaadinWebSecurity
import it.posteitaliane.gdc.gadc.security.LocalDBProvider
import it.posteitaliane.gdc.gadc.security.ReteAuthenticationProvider
import it.posteitaliane.gdc.gadc.security.TextAuthenticationConfig
import it.posteitaliane.gdc.gadc.security.TextAuthenticationProvider
import it.posteitaliane.gdc.gadc.views.LoginView
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.lang.Nullable
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
class SecurityConfiguration : VaadinWebSecurity() {

    private fun passwordEncoder() = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun localdbProvider(db:JdbcTemplate) = LocalDBProvider(db, passwordEncoder())

    @Bean
    fun reteProvider(db:JdbcTemplate) = ReteAuthenticationProvider(db)

    @Bean
    @ConditionalOnProperty(prefix = "textauth", name = arrayOf("enable"), havingValue = "true")
    fun textProvider(conf:TextAuthenticationConfig) = TextAuthenticationProvider(conf)

    @Bean
    fun authenticationManager(
        db:LocalDBProvider,
        rete:ReteAuthenticationProvider,
        @Nullable text:TextAuthenticationProvider?
    ) : AuthenticationManager {

        if(text == null ) return ProviderManager(rete,db)
        else return ProviderManager(rete,db,text)
    }

    @Override
    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun apiChain(http:HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/api/**")
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            httpBasic {  }
        }
        return http.build()
    }
}