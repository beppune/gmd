package it.posteitaliane.gdc.gadc.services

import it.posteitaliane.gdc.gadc.config.FilesConfig
import it.posteitaliane.gdc.gadc.model.Order
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import java.io.IOException
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

    private val QUERY_INSERT_FILE = "INSERT INTO SHIPPINGS (ownedby,issued,motive,hauler,address,filepath,numpack) " +
            " VALUES(?,?,'motive','hauler','address',?,1)"
    private val QUERY_UPDATE_FILE = "UPDATE SHIPPINGS SET filepath = ? WHERE ownedby = ?"

    fun updateOrderFile(o:Order, uploadpath:String) {
        println(uploadpath)
        val count = db.queryForObject("SELECT COUNT(*) FROM SHIPPINGS WHERE ownedby = ${o.number}", Int::class.java)

        try {


            val from = Path.of(uploadpath)
            val dest = Path.of( config.storageDirectory, from.fileName.toString() )

            FileSystemUtils.copyRecursively(from, dest)
            FileSystemUtils.deleteRecursively(from)

            dest.fileName.also(::println)

            if (count == 0) {
                db.update(QUERY_INSERT_FILE, o.number, LocalDateTime.now(), dest.fileName)
            } else {
                db.update(QUERY_UPDATE_FILE, dest.fileName, o.number)
            }

        }catch (ex:DataAccessException) {
            ex.printStackTrace()
        }catch (ex:IOException) {
            ex.printStackTrace()
        }
    }

}