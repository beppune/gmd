package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.GAFaker
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class CreateDatabaseRunner(val db:JdbcTemplate) : CommandLineRunner {

    val CREATE = """
        
        CREATE TABLE PERSONS (
            id          UUID NOT NULL,
        
            lastname    VARCHAR(100) NOT NULL,
            firstname   VARCHAR(100) NOT NULL,
            
            cf          CHAR(16) NULL UNIQUE NULLS DISTINCT,
            birthdate   DATE NULL,
            
            PRIMARY KEY(id)
        );
        CREATE INDEX ON PERSONS(lastname);
        CREATE INDEX ON PERSONS(firstname);
        
        CREATE TABLE DCS(
            shortname   CHAR(4) NOT NULL,
            fullname    VARCHAR(255) NOT NULL,
            
            PRIMARY KEY(shortname)
        );
        
        CREATE TABLE IDPAPERS(
            number  VARCHAR(255) NOT NULL,
            type    ENUM( 'CI', 'PASS', 'PA', 'PU' ) NOT NULL,
            expires DATE NULL,
            
            owner   UUID NOT NUll,
                        
            PRIMARY KEY(number),
            FOREIGN KEY(owner) REFERENCES PERSONS(id)
        );
        
        CREATE VIEW PAPERS_OWNERS AS SELECT ID,LASTNAME,FIRSTNAME,CF,BIRTHDATE,TYPE,NUMBER,EXPIRES  FROM IDPAPERS JOIN PERSONS ON(OWNER=ID);
        
        CREATE TABLE LOCATIONS(
            dc      CHAR(4) NOT NULL,
            name    VARCHAR(15) NOT NULL,
            
            PRIMARY KEY(dc,name),
            FOREIGN KEY(dc) REFERENCES DCS(shortname)
        );
        
        CREATE TABLE OPERATORS(
            uid             CHAR(8) NOT NULL,
            lastname        VARCHAR(100) NOT NULL,
            firstname       VARCHAR(100) NOT NULL,
            email           VARCHAR(256) NOT NULL UNIQUE,
            role            ENUM( 'OPERATOR','ADMIN' ) NOT NULL DEFAULT 'OPERATOR',
            active          BOOL NOT NULL DEFAULT TRUE,
            localpassword   VARCHAR(50) NULL,
            
            PRIMARY KEY(uid)
        );
        
        CREATE TABLE PERMISSIONS(
            operator    CHAR(8) NOT NULL,
            dc          CHAR(4) NOT NULL,
            
            PRIMARY KEY(operator,dc),
            FOREIGN KEY (operator) REFERENCES OPERATORS(uid),
            FOREIGN KEY (dc) REFERENCES DCS(shortname)
        );
        
        CREATE TABLE ITEMS(
            name    VARCHAR(250) NOT NULL,
             
            PRIMARY KEY(name)
        );
        
        CREATE TABLE STORAGE(
            item        VARCHAR(250) NOT NULL,
            dc          CHAR(4) NOT NULL,
            pos         VARCHAR(15) NOT NULL,
            
            amount      INT CHECK( amount >0 ),
            
            sn          VARCHAR(250) NULL UNIQUE NULLS DISTINCT,
            pt          VARCHAR(12) NULL UNIQUE NULLS DISTINCT,
            
            PRIMARY KEY(item,dc,pos),
            UNIQUE NULLS DISTINCT(sn,pt),
            FOREIGN KEY(item) REFERENCES ITEMS(name),
            FOREIGN KEY(dc,pos) REFERENCES LOCATIONS(dc,name)
        );
        
    """.trimIndent()

    override fun run(vararg args: String?) {

        val faker = GAFaker()

        db.update(CREATE)

        for (i in 0..1000) {
            db.update(
                "INSERT INTO PERSONS(id, lastname, firstname, cf, birthdate) VALUES(?,?,?,?,?)",
                UUID.randomUUID(),
                faker.name().lastName(),
                faker.name().firstName(),
                faker.ga().cf(),
                faker.ga().birthdate()
            )
        }

        for (i in 0..2) {
            db.update("INSERT INTO DCS(shortname, fullname) VALUES(?,?)", faker.ga().dcshort(), faker.ga().dcfull())
        }

        db.queryForList("SELECT shortname FROM DCS", String::class.java).stream()
            .forEach { shortname ->
                for (i in 0..6) {
                    db.update(
                        "INSERT INTO LOCATIONS(dc,name) VALUES(?,?)",
                        shortname,
                        faker.expression("#{examplify 'AA1'}").uppercase()
                    )
                }
            }

        db.queryForList("SELECT id FROM PERSONS", UUID::class.java).stream()
            .forEach { uuid ->
                db.update(
                    "INSERT INTO IDPAPERS(number, type, expires, owner) VALUES(?,?,?,?)",
                        faker.ga().idnumber(),
                        faker.ga().idtype(),
                        faker.ga().expiration(),
                        uuid
                    )
            }

        listOf("MANZOGI9", "MARTA231", "CENCIO12", "MARIA342")
            .apply {
                forEach { uid ->
                    db.update(
                        "INSERT INTO OPERATORS(uid,lastname,firstname,email) VALUES(?,?,?,?)",
                        uid,
                        faker.name().lastName(),
                        faker.name().firstName(),
                        "${uid}@${faker.internet().domainName()}"
                    )
                }

                val admin = get(Random.nextInt(0, size))

                db.update("UPDATE OPERATORS SET role = 'ADMIN' WHERE uid = ?",  admin)

                println("Admin is: $admin")
            }
            .forEach { op ->
                val dcs = db.queryForList("SELECT shortname FROM DCS", String::class.java)
                dcs.subList(0, Random.nextInt(1, dcs.size)).forEach { dc ->
                    db.update("INSERT INTO PERMISSIONS(operator, dc) VALUES(?,?)", op, dc)
                }
            }



        faker.collection({faker.appliance().equipment()}).len(100,100).generate<List<String>>()
            .stream().sorted().distinct().forEach {
                db.update("INSERT INTO ITEMS(name) VALUES(?)", it.uppercase())
            }

        faker.collection({faker.computer().type()}).len(10).generate<List<String>>()
            .stream().distinct().forEach { name ->
                db.update("INSERT INTO ITEMS(name) VALUES(?)", name.uppercase())
            }
    }


}