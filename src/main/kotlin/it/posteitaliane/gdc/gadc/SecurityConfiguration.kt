package it.posteitaliane.gdc.gadc

import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.security.VaadinWebSecurity
import it.posteitaliane.gdc.gadc.services.OperatorService
import it.posteitaliane.gdc.gadc.views.LoginView
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@SpringComponent
@Configuration
class SecurityConfiguration(
    private val ops:OperatorService
) : VaadinWebSecurity() {



    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(AntPathRequestMatcher("/public/**"))
                .permitAll()
        }
        super.configure(http)

        setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun userDetailsManager() = object : UserDetailsManager {
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

        override fun createUser(user: UserDetails?) {
            TODO("Not available as Maganer")
        }

        override fun updateUser(user: UserDetails?) {
            TODO("Not available as Maganer")
        }

        override fun deleteUser(username: String?) {
            TODO("Not yet implemented")
        }

        override fun changePassword(oldPassword: String?, newPassword: String?) {
            TODO("Not available as Maganer")
        }

        override fun userExists(username: String?): Boolean {
            return ops.find(filter = username).size == 1
        }

    }
}