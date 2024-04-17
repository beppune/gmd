package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.config.FilesConfig
import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.FileSystemUtils
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class FilesService(
    private val config:FilesConfig,
    private val db:JdbcTemplate
) {

    fun copyTemp(username:String, instream:InputStream) : String {

        FileSystemUtils.deleteRecursively( Path.of(config.uploadDirectory, username) )
        val dest = Files.createDirectory( Path.of(config.uploadDirectory, username) )

        val res = Files.write(dest.resolve("${username.uppercase()}_${LocalDate.now()}.pdf"), instream.readAllBytes())
        return res.toString()
    }

    private val QUERY_INSERT_FILE = "INSERT INTO SHIPPINGS (ownedby,issued,motive,hauler,address,filepath,numpack) +" +
            " VALUES(?,?,'motive','hauler','address',?,1)"
    private val QUERY_UPDATE_FILE = "UPDATE SHIPPINGS SET filepath = ? WHERE ownedby = ?"

    fun updateOrderFile(o:Order, uploadpath:String) {
        val count = db.queryForObject("SELECT COUNT(*) FROM SHIPPINGS WHERE ownedby = ${o.number}", Int::class.java)

        if (count == 0) {
            db.update(QUERY_INSERT_FILE, o.number, LocalDateTime.now(), uploadpath)
        } else {
            db.update(QUERY_UPDATE_FILE, uploadpath, o.number)
        }
    }

}