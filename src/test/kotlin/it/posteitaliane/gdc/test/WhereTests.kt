package it.posteitaliane.gdc.test

import it.posteitaliane.gdc.gmd.sql.select
import it.posteitaliane.gdc.gmd.sqlfun.LIKE
import org.junit.jupiter.api.Test

class WhereTests {

    @Test
    fun test() {
        select( "one", "two", "three")
            .from("table")
            .where( "one" LIKE "ONE" )
    }

}