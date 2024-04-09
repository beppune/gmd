package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.config.FilesConfig
import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

@Service
class FilesService(
    private val config:FilesConfig
) {

    fun copyTemp(username:String, instream:InputStream) : String {

        FileSystemUtils.deleteRecursively( Path.of(config.uploadDirectory, username) )
        val dest = Files.createDirectory( Path.of(config.uploadDirectory, username) )

        val res = Files.write(dest.resolve("${username.uppercase()}_${LocalDate.now()}.pdf"), instream.readAllBytes())
        return res.toString()
    }

    fun updateOrderFile(o:Order, uploadpath:String) {
        print(o)
        println(uploadpath)
    }

}