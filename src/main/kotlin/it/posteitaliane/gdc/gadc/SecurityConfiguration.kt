package it.posteitaliane.gdc.gadc

import com.vaadin.flow.spring.security.VaadinWebSecurity
import it.posteitaliane.gdc.gadc.security.LocalDBProvider
import it.posteitaliane.gdc.gadc.security.ReteAuthenticationProvider
import it.posteitaliane.gdc.gadc.security.TextAuthenticationConfig
import it.posteitaliane.gdc.gadc.security.TextAuthenticationProvider
import it.posteitaliane.gdc.gadc.views.LoginView
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
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
    fun authenticationManager(db:JdbcTemplate, textauth:TextAuthenticationConfig) : AuthenticationManager {
        val list = mutableListOf(
            LocalDBProvider(db, passwordEncoder()),
            ReteAuthenticationProvider(db)
        ).apply {
            if(textauth.enable) {
                add(TextAuthenticationProvider(textauth))
            }
        }
        return ProviderManager(list)
    }

    @Override
    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }

    @Bean
    open fun apiChain(http:HttpSecurity): SecurityFilterChain {
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