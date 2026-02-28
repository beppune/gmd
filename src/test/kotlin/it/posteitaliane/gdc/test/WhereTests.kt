package it.posteitaliane.gdc.test

import it.posteitaliane.gdc.gmd.sql.EXISTS
import it.posteitaliane.gdc.gmd.sql.NOT
import it.posteitaliane.gdc.gmd.sql.select
import it.posteitaliane.gdc.gmd.sqlfun.IN
import it.posteitaliane.gdc.gmd.sqlfun.LIKE
import it.posteitaliane.gdc.gmd.sqlfun.existsIn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class WhereTests {

    @Test
    fun test() {
        val query = select( "one", "two", "three")
            .from("table")
            .where(
                NOT("one" LIKE "ONE"),
                "two" IN listOf("TWO", "THREE"),
                ).build()

        val expected = "SELECT one, two, three FROM table WHERE NOT (one LIKE 'ONE%') AND two IN ('TWO', 'THREE') "

        assertEquals(expected, query)
    }

    @Test
    fun exists() {
        val query = select( "one", "two", "three")
            .from("table")
            .where(
                "one" IN listOf("ONE", "TWO"),
                "two" IN listOf("TWO", "THREE"),
                EXISTS( existsIn("order_lines", "pos" IN listOf("A-01") ) )
                ).build().also(::println)
    }

}