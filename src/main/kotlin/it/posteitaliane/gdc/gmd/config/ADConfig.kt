package it.posteitaliane.gdc.gmd.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ad")
class ADConfig {

    lateinit var serverName:String
    lateinit var baseDN:String

}