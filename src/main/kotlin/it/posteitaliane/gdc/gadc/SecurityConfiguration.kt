package it.posteitaliane.gdc.gadc

import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.security.VaadinWebSecurity
import it.posteitaliane.gdc.gadc.services.OperatorService
import it.posteitaliane.gdc.gadc.views.LoginView
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@SpringComponent
@Configuration
class SecurityConfiguration(
    private val ops:OperatorService
) : VaadinWebSecurity() {


    override fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(AntPathRequestMatcher("/api/**"))
                .authenticated()
        }
            .httpBasic(Customizer.withDefaults())
        return super.filterChain(http)

    }

    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(AntPathRequestMatcher("/public/**"))
                .permitAll()
        }
        super.configure(http)

        setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun userDetailsService() = object : UserDetailsService {
        override fun loadUserByUsername(username: String?): UserDetails? {
            try {
                val op = ops.find(filter = username?.uppercase()).first()

                return User
                    .withUsername(op.username)
                    .roles(op.role.name)
                    .password("{noop}${op.localPassword}")
                    .build()
            }catch (ex:NoSuchElementException) {
                println("Username not found: $username")
                return null
            }
        }

    }
}