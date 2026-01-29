package it.posteitaliane.gdc.gmd.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "textauth")
class TextAuthenticationConfig(
    var username:String = "user",
    var password:String = "password",
    var role:String = "ADMIN",
    var enable:Boolean = false
)