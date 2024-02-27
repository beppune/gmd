package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.GAFaker
import it.posteitaliane.gdc.gadc.config.GMDConfig
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class CreateDatabaseRunner(val db:JdbcTemplate, val config:GMDConfig) : CommandLineRunner {

    val CREATE = """
                
        CREATE TABLE DCS(
            shortname   CHAR(4) NOT NULL,
            fullname    VARCHAR(256) NOT NULL,
            address     VARCHAR(256) NOT NULL,
            
            PRIMARY KEY(shortname)
        );
        
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
            
            amount      INT CHECK( amount >=0 ),
            
            sn          VARCHAR(250) NULL UNIQUE NULLS DISTINCT,
            pt          VARCHAR(12) NULL UNIQUE NULLS DISTINCT,
            
            PRIMARY KEY(item,dc,pos),
            UNIQUE NULLS DISTINCT(sn,pt),
            FOREIGN KEY(item) REFERENCES ITEMS(name),
            FOREIGN KEY(dc,pos) REFERENCES LOCATIONS(dc,name)
        );
        
        CREATE TABLE SUPPLIERS(
            name        VARCHAR(200) NOT NULL,
            legal       VARCHAR(256) NOT NULL,
            piva        CHAR(11) NOT NULL,
            
            PRIMARY KEY(name)
        );
        
        CREATE TABLE SUPPLIERS_ADDRESSES(
            supplier    VARCHAR(200) NOT NULL,
            address     VARCHAR(500) NOT NULL,
            
            PRIMARY KEY(supplier,address),
            FOREIGN KEY(supplier) REFERENCES SUPPLIERS(name)
            
        );
        
        CREATE TABLE ORDERS (
            id              UUID NOT NULL,
            operator        CHAR(8) NOT NULL,
            datacenter      CHAR(4) NOT NULL,
            supplier        VARCHAR(200) NOT NULL,
            issued          DATE NOT NULL,
            type            ENUM( 'INBOUND', 'OUTBOUND' ) NOT NULL,
            subject         ENUM( 'SUPPLIER', 'SUPPLIER_DC', 'INTERNAL') NOT NULL,
            status          ENUM( 'OPENED', 'PENDING', 'COMPLETED', 'CANCELED' ) NOT NULL,
            ref             VARCHAR(100) NOT NULL,
            
            remarks         VARCHAR(500) NULL,
            
            PRIMARY KEY(id),
            FOREIGN KEY(operator) REFERENCES OPERATORS(uid),
            FOREIGN KEY(datacenter) REFERENCES DCS(shortname),
            FOREIGN KEY(supplier) REFERENCES SUPPLIERS(name)
        );
        
    """.trimIndent()

    override fun run(vararg args: String?) {

        val faker = GAFaker()

        db.update(CREATE)

        for (i in 0..2) {
            db.update(
                "INSERT INTO DCS(shortname, fullname, address) VALUES(?,?,?)",
                faker.ga().dcshort(),
                faker.ga().dcfull(),
                faker.address().streetAddress()
                )
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

        faker.collection({faker.ga().uid()}).len(4).generate<List<String>>()
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

        faker.collection({faker.company().name().uppercase()})
            .len(20).generate<List<String>>()
            .forEach { company ->
                db.update("INSERT INTO SUPPLIERS(name,legal,piva) VALUES(?,?,?)", company, faker.address().streetAddress(), faker.ga().piva())

                db.update(
                    "INSERT INTO SUPPLIERS_ADDRESSES(supplier, address) VALUES(?,?)",
                    company,
                    faker.address().streetAddress()
                )
            }

        db.update("INSERT INTO SUPPLIERS(name,legal,piva) VALUES(?,?,?)", config.firmName, config.firmLegal, config.firmPiva)

    }


}