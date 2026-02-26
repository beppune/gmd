package it.posteitaliane.gdc.test

import it.posteitaliane.gdc.gmd.SelectBuilder
import it.posteitaliane.gdc.gmd.sqlfun.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SqlTest {

    @Test
    fun testSelect() {
         var query = SelectBuilder().select("uid", "last_name", "first_name")
         var expected = "SELECT uid, last_name, first_name "
         assertEquals(expected, query.build() )

         expected = "SELECT uid AS user_id, last_name, first_name "
         query = SelectBuilder().select("uid" AS "user_id", "last_name", "first_name")
         assertEquals(expected, query.build() )
     }

    @Test
    fun testFromJoin() {
        var query = SelectBuilder()
            .select("uid", "last_name", "first_name")
            .from("table_one")
            .join("table_two" ON ("uid" to "user_id") )

        var expected = "SELECT uid, last_name, first_name FROM table_one JOIN table_two ON(uid = user_id) "
        assertEquals(expected, query.build() )

    }

    @Test
    fun testFilter() {
        var query = SelectBuilder()
            .select("CONCAT(lastname, ' ', firstname)" AS "op", "datacenter", "supplier", "type", "subject")
            .from("orders")
                .join("operators" ON ("operator" to "uid") )
            .where(
                "subject" LIKE "INTERNAL",
                "datacenter" IN listOf("TOR", "TOT"),
            )

        var expected = "SELECT CONCAT(lastname, ' ', firstname) AS op, datacenter, supplier, type, subject FROM orders JOIN operators ON(operator = uid) WHERE subject LIKE 'INTERNAL%' AND datacenter IN ('TOR', 'TOT') "
        println(query.build() )
        assertEquals(expected, query.build() )
    }
}