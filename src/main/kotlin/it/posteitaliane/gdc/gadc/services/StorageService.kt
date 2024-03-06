package it.posteitaliane.gdc.gadc.services

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class StorageService {

    fun snIsRegistered(sn:String): Boolean {
        return sn == "SNISALREADYINSTORAGE"
    }

}