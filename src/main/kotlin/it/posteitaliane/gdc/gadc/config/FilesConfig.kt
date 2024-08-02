package it.posteitaliane.gdc.gadc.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
@ConfigurationProperties(prefix = "files")
class FilesConfig {

    var uploadDirectory:String="upload"
    var storageDirectory:Path=Path.of("docstorage")

}