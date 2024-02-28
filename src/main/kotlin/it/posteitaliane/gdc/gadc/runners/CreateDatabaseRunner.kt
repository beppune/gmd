package it.posteitaliane.gdc.gadc.runners

import it.posteitaliane.gdc.gadc.GAFaker
import it.posteitaliane.gdc.gadc.config.GMDConfig
import it.posteitaliane.gdc.gadc.model.Order
import it.posteitaliane.gdc.gadc.services.BackOffice
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class CreateDatabaseRunner(val db:JdbcTemplate, val config:GMDConfig, val bo:BackOffice) : CommandLineRunner {

    val CREATE = """
                
        CREATE TABLE DCS(
            shortname   CHAR(4) NOT NULL,
            fullname    VARCHAR(256) NOT NULL,
            address     VARCHAR(256) NOT NULL,
            
            PRIMARY KEY(shortname)
        );
        CREATE INDEX ON DCS(fullname);
        
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
        CREATE INDEX ON OPERATORS(lastname);
        CREATE INDEX ON OPERATORS(firstname);
        
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
            status          ENUM( 'PENDING', 'COMPLETED', 'CANCELED' ) NOT NULL,
            ref             VARCHAR(100) NOT NULL,
            
            remarks         VARCHAR(500) NULL,
            
            PRIMARY KEY(id),
            FOREIGN KEY(operator) REFERENCES OPERATORS(uid),
            FOREIGN KEY(datacenter) REFERENCES DCS(shortname),
            FOREIGN KEY(supplier) REFERENCES SUPPLIERS(name)
        );
        
        
        
        CREATE TABLE ORDERS_LINES(
            ownedby         UUID NOT NULL,
            datacenter      CHAR(8) NOT NULL,
            item            VARCHAR(250) NOT NULL,
            pos             VARCHAR(15) NOT NULL,
            amount          INT NOT NULL CHECK ( amount > 0),
            sn              VARCHAR(100) NULL UNIQUE NULLS DISTINCT,
            
            PRIMARY KEY(ownedby,datacenter,item,pos,amount),
            FOREIGN KEY(item) REFERENCES ITEMS(name),
            FOREIGN KEY(ownedby) REFERENCES ORDERS(id),
            FOREIGN KEY(datacenter,pos) REFERENCES LOCATIONS(dc,name)
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

        faker.collection({faker.computer().type()}).len(10).generate<List<String>>().distinct()
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

        //val items = db.queryForList("SELECT name FROM ITEMS", String::class.java)

        for (i in 0..25) {
            val op = bo.ops.findAll().random()
            val dc = op.permissions.random()
            //val supplier = BO.sups.findAll().random()
            /*val item = items.random()
            val pos = db.queryForList("SELECT name FROM LOCATIONS WHERE dc = ?", String::class.java, dc.short)
                .random()
            val amount = Random.nextInt(1, 6)
            */

            val o = bo.from(op).place {
                receiveFromDc(dc)
            }

            bo.register(o)

            bo.os.findAll()
                .forEach {
                    db.update(
                        "UPDATE ORDERS SET status = ? WHERE id = ?",
                        Order.Status.entries.toTypedArray().random().name,
                        it.number
                        )
                }
        }

        val items = db.queryForList(
            "SELECT name FROM ITEMS",
            String::class.java
        )

        bo.os.findAll().forEach { o ->

            for ( i in 0..Random.nextInt(1, 5) ) {

                val i = items.random()
                var a = Random.nextInt(1, 20)
                var sn:String?=null

                Random.nextBoolean().also {
                    if( it ) {
                        sn = faker.ga().sn()
                        a = 1
                    }
                }

                db.update(
                    "INSERT INTO ORDERS_LINES(ownedby,datacenter,item,pos,amount,sn) VALUES(?,?,?,?,?,?)",
                    o.number,
                    o.dc.short,
                    i,
                    bo.dcs.findAll(true).find { it.short == o.dc.short }!!.locations.random(),
                    a,
                    sn
                )
            }

        }
    }


}