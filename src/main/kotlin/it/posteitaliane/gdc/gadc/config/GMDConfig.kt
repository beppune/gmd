package it.posteitaliane.gdc.gadc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "gmd")
class GMDConfig {

    lateinit var firmName:String
    lateinit var firmLegal:String
    lateinit var firmPiva:String

}