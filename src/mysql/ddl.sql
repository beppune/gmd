  DROP DATABASE IF EXISTS testdb;
  CREATE DATABASE testdb;
  USE testdb;
  
        CREATE TABLE DCS(
            shortname   CHAR(4) NOT NULL,
            fullname    VARCHAR(256) NOT NULL,
            legal     VARCHAR(256) NOT NULL,
            
            PRIMARY KEY(shortname)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        CREATE FULLTEXT INDEX dcs_fullname_fulltext ON DCS(fullname);
        
        CREATE TABLE LOCATIONS(
            dc      CHAR(4) NOT NULL,
            name    VARCHAR(15) NOT NULL,
            
            PRIMARY KEY(dc,name),
            FOREIGN KEY(dc) REFERENCES DCS(shortname)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE OPERATORS(
            uid             CHAR(8) NOT NULL,
            lastname        VARCHAR(100) NOT NULL,
            firstname       VARCHAR(100) NOT NULL,
            email           VARCHAR(256) NOT NULL UNIQUE,
            role            ENUM( 'OPERATOR','ADMIN' ) NOT NULL DEFAULT 'OPERATOR',
            active          BOOL NOT NULL DEFAULT TRUE,
            localpassword   VARCHAR(50) NULL,
            
            PRIMARY KEY(uid)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE PERMISSIONS(
            operator    CHAR(8) NOT NULL,
            dc          CHAR(4) NOT NULL,
            
            PRIMARY KEY(operator,dc),
            FOREIGN KEY (operator) REFERENCES OPERATORS(uid),
            FOREIGN KEY (dc) REFERENCES DCS(shortname)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE ITEMS(
            name    VARCHAR(250) NOT NULL,
             
            PRIMARY KEY(name)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE STORAGE(
            item        VARCHAR(250) NOT NULL,
            dc          CHAR(4) NOT NULL,
            pos         VARCHAR(15) NOT NULL,
            
            amount      INT CHECK( amount > 0 ),
            
            sn          VARCHAR(250) NULL UNIQUE,
            pt          VARCHAR(12) NULL UNIQUE,
            
            PRIMARY KEY(item,dc,pos),
            FOREIGN KEY(item) REFERENCES ITEMS(name),
            FOREIGN KEY(dc,pos) REFERENCES LOCATIONS(dc,name)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        CREATE UNIQUE INDEX storage_sn_pt_unique ON STORAGE(sn,pt);
        
        CREATE TABLE SUPPLIERS(
            name        VARCHAR(200) NOT NULL,
            legal       VARCHAR(256) NOT NULL,
            piva        CHAR(11) NOT NULL,
            
            PRIMARY KEY(name)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE SUPPLIERS_ADDRESSES(
            supplier    VARCHAR(200) NOT NULL,
            address     VARCHAR(500) NOT NULL,
            
            PRIMARY KEY(supplier,address),
            FOREIGN KEY(supplier) REFERENCES SUPPLIERS(name)
            
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE ORDERS (
            id              INTEGER AUTO_INCREMENT,
            operator        CHAR(8) NOT NULL,
            datacenter      CHAR(4) NOT NULL,
            supplier        VARCHAR(200) NOT NULL,
            issued          DATETIME NOT NULL,
            type            ENUM( 'INBOUND', 'OUTBOUND' ) NOT NULL,
            subject         ENUM( 'SUPPLIER', 'SUPPLIER_DC', 'INTERNAL') NOT NULL,
            status          ENUM( 'PENDING', 'COMPLETED', 'CANCELED' ) NOT NULL,
            ref             VARCHAR(100) NOT NULL,
            
            remarks         VARCHAR(1000) NULL,
            
            PRIMARY KEY(id),
            FOREIGN KEY(operator) REFERENCES OPERATORS(uid),
            FOREIGN KEY(datacenter) REFERENCES DCS(shortname),
            FOREIGN KEY(supplier) REFERENCES SUPPLIERS(name)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        
        
        CREATE TABLE ORDERS_LINES(
            ownedby         INTEGER NOT NULL,
            datacenter      CHAR(8) NOT NULL,
            item            VARCHAR(250) NOT NULL,
            pos             VARCHAR(15) NOT NULL,
            amount          INT NOT NULL CHECK ( amount > 0),
            sn              VARCHAR(100) NULL,
            pt              VARCHAR(100) NULL,
            
            PRIMARY KEY(ownedby,datacenter,item,pos,amount),
            FOREIGN KEY(item) REFERENCES ITEMS(name),
            FOREIGN KEY(ownedby) REFERENCES ORDERS(id),
            FOREIGN KEY(datacenter,pos) REFERENCES LOCATIONS(dc,name)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE TRANSACTIONS(
            id              INTEGER AUTO_INCREMENT PRIMARY KEY,
            operator        TEXT NOT NULL,
            type            TEXT NOT NULL,
            timestamp       DATETIME NOT NULL,
            item            TEXT NOT NULL,
            dc              TEXT NOT NULL,
            pos             TEXT NOT NULL,
            amount          INTEGER NOT NULL CHECK(amount > 0),
            sn              TEXT NULL,
            pt              TEXT NULL
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        CREATE TABLE SHIPPING_SEQUENCE(
			counter		INTEGER NOT NULL,
            year		INTEGER NOT NULL,
            
            PRIMARY KEY(counter,year)
        );
        INSERT INTO SHIPPING_SEQUENCE(counter,year) VALUES(0,YEAR(CURRENT_DATE));
        INSERT INTO SHIPPING_SEQUENCE(counter,year) VALUES(0,YEAR(CURRENT_DATE) + 1);
        INSERT INTO SHIPPING_SEQUENCE(counter,year) VALUES(0,YEAR(CURRENT_DATE) + 2);
        INSERT INTO SHIPPING_SEQUENCE(counter,year) VALUES(0,YEAR(CURRENT_DATE) + 3);
        INSERT INTO SHIPPING_SEQUENCE(counter,year) VALUES(0,YEAR(CURRENT_DATE) + 4);
        
		DELIMITER //
		CREATE FUNCTION get_next_shipping_number() RETURNS TEXT
			CONTAINS SQL DETERMINISTIC
		BEGIN
			SET @new_counter = 0;
            SET @thisyear = YEAR(CURRENT_DATE());
            SELECT MAX(counter) + 1 INTO @new_counter FROM SHIPPING_SEQUENCE WHERE year = @thisyear;
            INSERT INTO SHIPPING_SEQUENCE(counter, year) VALUES(@new_counter,@thisyear);
            RETURN CONCAT('',@new_counter,'/',@thisyear);
		END //
		DELIMITER ;
        
        CREATE TABLE SHIPPINGS(
			number		TEXT NULL,
            ownedby		INTEGER NOT NULL,
            issued		DATETIME NOT NULL,
            motive		VARCHAR(500) NOT NULL,
            hauler		VARCHAR(50) NOT NULL,
            address     VARCHAR(128) NOT NULL,
            filepath    VARCHAR(60) NULL,
            numpack     INT,

            PRIMARY KEY(ownedby),
            FOREIGN KEY(ownedby) REFERENCES ORDERS (id)
        ) CHARACTER SET = utf8mb4 , COLLATE = utf8mb4_general_ci ;
        
        