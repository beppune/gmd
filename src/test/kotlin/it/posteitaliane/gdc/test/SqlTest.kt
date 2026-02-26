package it.posteitaliane.gdc.test

import it.posteitaliane.gdc.gmd.sql.AS
import it.posteitaliane.gdc.gmd.sql.CONCAT
import it.posteitaliane.gdc.gmd.sql.REPLACE
import it.posteitaliane.gdc.gmd.sql.dq
import it.posteitaliane.gdc.gmd.sql.q
import it.posteitaliane.gdc.gmd.sql.select
import it.posteitaliane.gdc.gmd.sqlfun.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
/*

SELECT
	id,operator,datacenter,supplier,issued,type,subject,status,ref,remarks,
	CONCAT(lastname, ' ', firstname) AS opfullname,
	REPLACE(fullname, 'DC ', '') AS dcfullname
FROM ORDERS
	JOIN OPERATORS ON uid=operator
	JOIN DCS ON shortname=datacenter
WHERE
	datacenter IN ( 'TOR', 'TOT' )
    AND
    operator IN ( 'EXTA1KD', 'FIN3G6X', 'FIN3HDM', 'AVF3Y1B' )
    AND
    EXISTS (SELECT 1 FROM orders_lines WHERE pos IN ('A-P05') )

 */
class SqlTest {

    @Test
    fun testSelect() {
         var query = select("uid", "last_name", "first_name")
         var expected = "SELECT uid, last_name, first_name "
         assertEquals(expected, query.build() )

         expected = "SELECT uid AS user_id, last_name, first_name "
         query = select("uid" AS "user_id", "last_name", "first_name")
         assertEquals(expected, query.build() )
     }

    @Test
    fun testFromJoin() {
        var query = select("uid", "last_name", "first_name")
            .from("table_one")
            .join("table_two" ON ("uid" to "user_id") )

        var expected = "SELECT uid, last_name, first_name FROM table_one JOIN table_two ON(uid = user_id) "
        assertEquals(expected, query.build() )

    }

    @Test
    fun testFilter() {
        var query = select( CONCAT("lastname", " ", "firstname") AS "op", "datacenter", "supplier", "type", "subject")
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

    @Test
    fun testProj() {
        select( CONCAT("op", " ", "first_name", " quoted ".q(), " double quoted ".dq() )  AS "hello", "ciao" )
            .build().also(::println)

        select(REPLACE("colname", "DC_", "FF_") AS "newname" )
            .from("table")
            .where( "colname" LIKE "DC_" )
            .build().also(::println)
    }
}