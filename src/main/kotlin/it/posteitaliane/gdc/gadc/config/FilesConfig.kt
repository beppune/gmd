package it.posteitaliane.gdc.gadc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "files")
class FilesConfig {

    var uploadDirectory:String="upload"
    var storageDirectory:String="docstorage"

}