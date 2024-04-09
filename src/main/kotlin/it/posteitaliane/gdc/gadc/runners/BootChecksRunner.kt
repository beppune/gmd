package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.config.FilesConfig
import org.jboss.logging.Logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.pathString

@Component
class BootChecksRunner(
    private val config:FilesConfig
) : ApplicationRunner{

    private val logger = Logger.getLogger("BootChecks")

    override fun run(args: ApplicationArguments?) {

        // Check if directories exist

        val uploadPath = Path.of("", config.uploadDirectory)
        if( uploadPath.notExists() ) {
            logger.info("Creating upload-path: ${uploadPath.pathString}")
            Files.createDirectory(uploadPath)
        }

        val storagePath = Path.of("", config.storageDirectory)
        if( storagePath.notExists() ) {
            logger.info("Creating storage-path: ${storagePath.pathString}")
            Files.createDirectory(storagePath)
        }

    }


}