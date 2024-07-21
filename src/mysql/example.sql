CREATE DATABASE  IF NOT EXISTS `testdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `testdb`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: testdb
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dcs`
--

DROP TABLE IF EXISTS `dcs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dcs` (
  `shortname` char(4) COLLATE utf8mb4_general_ci NOT NULL,
  `fullname` varchar(256) COLLATE utf8mb4_general_ci NOT NULL,
  `legal` varchar(256) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`shortname`),
  FULLTEXT KEY `dcs_fullname_fulltext` (`fullname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dcs`
--

LOCK TABLES `dcs` WRITE;
/*!40000 ALTER TABLE `dcs` DISABLE KEYS */;
INSERT INTO `dcs` VALUES ('FI6','Mariehamn','80273 Fay Prairie'),('QP4','Tripoli','5516 Dee Falls'),('ZI5','Bandar Seri Begawan','31300 Hills Estate');
/*!40000 ALTER TABLE `dcs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `name` varchar(250) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES ('AIR IONISER'),('AIR PURIFIER'),('AROMA LAMP'),('ATTIC FAN'),('BACHELOR GRILLER'),('BACK BOILER'),('BEVERAGE OPENER'),('BLENDER'),('BOX MANGLE'),('CAN OPENER'),('CENTRAL VACUUM CLEANER'),('CLOTHES DRYER'),('CLOTHES IRON'),('COLD-PRESSED JUICER'),('COMBO WASHER DRYER'),('DISHWASHER'),('DRAWER DISHWASHER'),('ELECTRIC WATER BOILER'),('EVAPORATIVE COOLER'),('EXHAUST HOOD'),('FAN HEATER'),('FLAME SUPERVISION DEVICE'),('FUTON DRYER'),('GAS APPLIANCE'),('GO-TO-BED MATCHBOX'),('HAIR DRYER'),('HAIR IRON'),('HUMIDIFIER'),('HVAC'),('ICEBOX'),('KIMCHI REFRIGERATOR'),('MANGLE (MACHINE)'),('MICATHERMIC HEATER'),('MICROWAVE OVEN'),('MOUSETRAP'),('OVEN'),('PAPER SHREDDER'),('SEWING MACHINE'),('SOLAR WATER HEATER'),('STEAM MOP'),('STOVE'),('SUMP PUMP'),('TELEVISION'),('TIE PRESS'),('TOASTER AND TOASTER OVENS'),('VACUUM CLEANER'),('WATER HEATER'),('WINDOW FAN');
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locations`
--

DROP TABLE IF EXISTS `locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `dc` char(4) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(15) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`dc`,`name`),
  CONSTRAINT `locations_ibfk_1` FOREIGN KEY (`dc`) REFERENCES `dcs` (`shortname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locations`
--

LOCK TABLES `locations` WRITE;
/*!40000 ALTER TABLE `locations` DISABLE KEYS */;
INSERT INTO `locations` VALUES ('FI6','BD - 97'),('FI6','BK - 43'),('FI6','CR - 75'),('FI6','DX - 37'),('FI6','LR - 24'),('FI6','NO - 75'),('FI6','NZ - 26'),('FI6','RT - 98'),('FI6','UD - 23'),('FI6','XT - 41'),('FI6','XW - 57'),('QP4','DK - 53'),('QP4','FA - 85'),('QP4','JP - 80'),('QP4','WP - 21'),('QP4','ZP - 09'),('QP4','ZT - 51'),('ZI5','BW - 21'),('ZI5','HF - 62'),('ZI5','HI - 92'),('ZI5','IQ - 59'),('ZI5','MH - 99'),('ZI5','NM - 81'),('ZI5','NT - 55'),('ZI5','PQ - 58'),('ZI5','ST - 30'),('ZI5','YS - 59');
/*!40000 ALTER TABLE `locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operators`
--

DROP TABLE IF EXISTS `operators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operators` (
  `uid` char(8) COLLATE utf8mb4_general_ci NOT NULL,
  `lastname` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `firstname` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(256) COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('OPERATOR','ADMIN') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'OPERATOR',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `localpassword` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operators`
--

LOCK TABLES `operators` WRITE;
/*!40000 ALTER TABLE `operators` DISABLE KEYS */;
INSERT INTO `operators` VALUES ('ADMIN','Gerhold','Edgardo','ADMIN@ritchie.info','ADMIN',1,'admin'),('ADRIAN.P','Pfannerstill','Jason','ADRIAN.P@monahan.net','OPERATOR',1,NULL),('AGATHA.S','Terry','Mark','AGATHA.S@huel.io','OPERATOR',1,NULL),('BRONWYN.','Schamberger','Kami','BRONWYN.@miller.io','OPERATOR',1,NULL),('CARROLL.','Price','Jordan','CARROLL.@vonrueden.net','OPERATOR',1,NULL),('DORSEY.L','Zemlak','Voncile','DORSEY.L@schroeder.info','OPERATOR',1,NULL),('HSIU.FAR','Pollich','Jim','HSIU.FAR@sipes.io','OPERATOR',1,NULL),('PERCY.LA','Hickle','Karin','PERCY.LA@rohan.net','OPERATOR',1,NULL),('QUEENIE.','Lueilwitz','Krystina','QUEENIE.@cummings.org','OPERATOR',1,NULL),('SAMMIE.M','Kulas','Shaunta','SAMMIE.M@bosco.biz','OPERATOR',1,NULL),('TOBIAS.R','Barrows','Lavern','TOBIAS.R@simonis.biz','OPERATOR',1,NULL);
/*!40000 ALTER TABLE `operators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `operator` char(8) COLLATE utf8mb4_general_ci NOT NULL,
  `datacenter` char(4) COLLATE utf8mb4_general_ci NOT NULL,
  `supplier` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `issued` datetime NOT NULL,
  `type` enum('INBOUND','OUTBOUND') COLLATE utf8mb4_general_ci NOT NULL,
  `subject` enum('SUPPLIER','SUPPLIER_DC','INTERNAL') COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('PENDING','COMPLETED','CANCELED') COLLATE utf8mb4_general_ci NOT NULL,
  `ref` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `remarks` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `operator` (`operator`),
  KEY `datacenter` (`datacenter`),
  KEY `supplier` (`supplier`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`operator`) REFERENCES `operators` (`uid`),
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`datacenter`) REFERENCES `dcs` (`shortname`),
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`supplier`) REFERENCES `suppliers` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders_lines`
--

DROP TABLE IF EXISTS `orders_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_lines` (
  `ownedby` int NOT NULL,
  `datacenter` char(8) COLLATE utf8mb4_general_ci NOT NULL,
  `item` varchar(250) COLLATE utf8mb4_general_ci NOT NULL,
  `pos` varchar(15) COLLATE utf8mb4_general_ci NOT NULL,
  `amount` int NOT NULL,
  `sn` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pt` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`ownedby`,`datacenter`,`item`,`pos`,`amount`),
  UNIQUE KEY `sn` (`sn`),
  UNIQUE KEY `pt` (`pt`),
  KEY `item` (`item`),
  KEY `datacenter` (`datacenter`,`pos`),
  CONSTRAINT `orders_lines_ibfk_1` FOREIGN KEY (`item`) REFERENCES `items` (`name`),
  CONSTRAINT `orders_lines_ibfk_2` FOREIGN KEY (`ownedby`) REFERENCES `orders` (`id`),
  CONSTRAINT `orders_lines_ibfk_3` FOREIGN KEY (`datacenter`, `pos`) REFERENCES `locations` (`dc`, `name`),
  CONSTRAINT `orders_lines_chk_1` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders_lines`
--

LOCK TABLES `orders_lines` WRITE;
/*!40000 ALTER TABLE `orders_lines` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders_lines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `operator` char(8) COLLATE utf8mb4_general_ci NOT NULL,
  `dc` char(4) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`operator`,`dc`),
  KEY `dc` (`dc`),
  CONSTRAINT `permissions_ibfk_1` FOREIGN KEY (`operator`) REFERENCES `operators` (`uid`),
  CONSTRAINT `permissions_ibfk_2` FOREIGN KEY (`dc`) REFERENCES `dcs` (`shortname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES ('ADMIN','FI6'),('ADRIAN.P','FI6'),('AGATHA.S','FI6'),('BRONWYN.','FI6'),('CARROLL.','FI6'),('HSIU.FAR','FI6'),('QUEENIE.','FI6'),('SAMMIE.M','FI6'),('TOBIAS.R','FI6'),('ADMIN','QP4'),('ADRIAN.P','QP4'),('AGATHA.S','QP4'),('CARROLL.','QP4'),('ADMIN','ZI5');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipping_sequence`
--

DROP TABLE IF EXISTS `shipping_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipping_sequence` (
  `counter` int NOT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`counter`,`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipping_sequence`
--

LOCK TABLES `shipping_sequence` WRITE;
/*!40000 ALTER TABLE `shipping_sequence` DISABLE KEYS */;
INSERT INTO `shipping_sequence` VALUES (0,2024),(0,2025),(0,2026),(0,2027),(0,2028);
/*!40000 ALTER TABLE `shipping_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shippings`
--

DROP TABLE IF EXISTS `shippings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shippings` (
  `number` text COLLATE utf8mb4_general_ci,
  `ownedby` int NOT NULL,
  `issued` datetime NOT NULL,
  `motive` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `hauler` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(128) COLLATE utf8mb4_general_ci NOT NULL,
  `filepath` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `numpack` int DEFAULT NULL,
  PRIMARY KEY (`ownedby`),
  CONSTRAINT `shippings_ibfk_1` FOREIGN KEY (`ownedby`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shippings`
--

LOCK TABLES `shippings` WRITE;
/*!40000 ALTER TABLE `shippings` DISABLE KEYS */;
/*!40000 ALTER TABLE `shippings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage`
--

DROP TABLE IF EXISTS `storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `storage` (
  `item` varchar(250) COLLATE utf8mb4_general_ci NOT NULL,
  `dc` char(4) COLLATE utf8mb4_general_ci NOT NULL,
  `pos` varchar(15) COLLATE utf8mb4_general_ci NOT NULL,
  `amount` int DEFAULT NULL,
  `sn` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pt` varchar(12) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`item`,`dc`,`pos`),
  UNIQUE KEY `sn` (`sn`),
  UNIQUE KEY `pt` (`pt`),
  UNIQUE KEY `storage_sn_pt_unique` (`sn`,`pt`),
  KEY `dc` (`dc`,`pos`),
  CONSTRAINT `storage_ibfk_1` FOREIGN KEY (`item`) REFERENCES `items` (`name`),
  CONSTRAINT `storage_ibfk_2` FOREIGN KEY (`dc`, `pos`) REFERENCES `locations` (`dc`, `name`),
  CONSTRAINT `storage_chk_1` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage`
--

LOCK TABLES `storage` WRITE;
/*!40000 ALTER TABLE `storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `legal` varchar(256) COLLATE utf8mb4_general_ci NOT NULL,
  `piva` char(11) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES ('BAILEY LLC','39644 Hand Shore','11728311425'),('BOYER, O\'CONNELL AND TRANTOW','0556 McKenzie Corners','46124930502'),('CASSIN-ROMAGUERA','4563 Agnes Mills','72093111059'),('CRUICKSHANK AND SONS','1891 Dach Trace','39313274248'),('DOOLEY, RUTHERFORD AND WEHNER','48004 Rafael Spurs','12788501633'),('FADEL, WEBER AND LABADIE','266 Winford Road','41213104681'),('FERRY GROUP','970 Mosciski Prairie','74619603284'),('FRANECKI LLC','746 Jack Inlet','73552147026'),('FRANECKI, TURCOTTE AND REILLY','17005 Barton Parks','99232300135'),('FRIESEN AND SONS','227 Alfonso Parkway','08503297527'),('GOLDNER-HODKIEWICZ','952 Dong Land','78478308395'),('GOODWIN, MAYERT AND BLOCK','55065 Clyde Locks','32951935163'),('GREEN, REICHERT AND WINDLER','9316 Robel Run','27104209387'),('GUSIKOWSKI-KOHLER','484 Mariko Rapid','47623349169'),('GUTKOWSKI AND SONS','055 Lebsack Views','59664444330'),('HALVORSON LLC','382 Deckow Crossroad','55393086796'),('HARRIS, BERGNAUM AND UPTON','8145 Lee Valleys','68950859599'),('HERMAN-QUIGLEY','571 Irving Lakes','62987503699'),('HETTINGER, HEGMANN AND BOGISICH','3251 Mervin Glen','43082503512'),('HIRTHE INC','0101 Durgan Mountains','84242894587'),('HUEL AND SONS','286 O\'Hara Rue','50775094340'),('JOHNS, KIRLIN AND LUETTGEN','8887 Herman Fork','55194328778'),('KERTZMANN, RUNOLFSDOTTIR AND WISOKY','576 Ferne Street','24431351094'),('KING-GUTMANN','2524 Conn Dale','12765836674'),('KOCH LLC','4529 Rohan Viaduct','39834244221'),('KOELPIN INC','424 Gaylord Hollow','85041570775'),('KREIGER-ERDMAN','9555 Stroman Wall','07179661420'),('KUHIC-HOWELL','8019 Wiza Ramp','23913175791'),('KULAS INC','9384 Chet Valley','63805703654'),('LABADIE AND SONS','143 Danilo Street','94645031446'),('MANN, JACOBI AND JACOBSON','21636 Lockman Burgs','47712313332'),('MCKENZIE AND SONS','766 Lang Stream','67384593756'),('MOEN GROUP','48672 Kling Villages','31094984792'),('MUELLER GROUP','97070 Elidia Vista','58493019185'),('PFEFFER INC','4105 Welch Meadow','11847945287'),('POSTE ITALIANE S.P.A.','Viale Europa, 190, 00144 Roma RM','01114601006'),('POUROS, HOMENICK AND BAUCH','47507 Hugh Orchard','82066287364'),('PREDOVIC, CRIST AND MITCHELL','431 Gaylord Landing','60746525301'),('QUIGLEY-SMITHAM','175 Schimmel Lodge','14498892605'),('RATKE INC','7557 Wilkinson Place','45157137948'),('RUNOLFSSON-NIKOLAUS','43992 Senger Ports','29102988745'),('SCHAMBERGER, KLING AND STROSIN','62025 Carroll Stravenue','17545487904'),('SCHINNER-KUB','71594 Mosciski Viaduct','48303841939'),('SCHMIDT GROUP','7322 Stracke Road','97540816695'),('SCHUMM INC','534 Benton Corner','63255842333'),('STOLTENBERG-STEUBER','21657 Runolfsdottir Ranch','35491446486'),('SWIFT, ORN AND ROHAN','368 Clelia Islands','10281660491'),('TROMP-GRANT','956 Deckow Lake','25956708113'),('TURCOTTE-MURAZIK','7175 Bryon Centers','59385281334'),('WILL GROUP','04172 Treena Highway','10106034513'),('ZEMLAK, ZBONCAK AND GRIMES','82283 Shanahan Crossroad','37626482136');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers_addresses`
--

DROP TABLE IF EXISTS `suppliers_addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers_addresses` (
  `supplier` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`supplier`,`address`),
  CONSTRAINT `suppliers_addresses_ibfk_1` FOREIGN KEY (`supplier`) REFERENCES `suppliers` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers_addresses`
--

LOCK TABLES `suppliers_addresses` WRITE;
/*!40000 ALTER TABLE `suppliers_addresses` DISABLE KEYS */;
INSERT INTO `suppliers_addresses` VALUES ('BAILEY LLC','0750 Smith Grove'),('BAILEY LLC','8493 Legros Manor'),('BOYER, O\'CONNELL AND TRANTOW','56683 Jeanene View'),('BOYER, O\'CONNELL AND TRANTOW','57770 Rico Ways'),('CASSIN-ROMAGUERA','65217 Carroll Ridge'),('CASSIN-ROMAGUERA','6538 Carolann Street'),('CRUICKSHANK AND SONS','80595 Anderson Ford'),('CRUICKSHANK AND SONS','832 Adolfo Park'),('DOOLEY, RUTHERFORD AND WEHNER','148 Renner Forest'),('DOOLEY, RUTHERFORD AND WEHNER','7684 Ricky Cove'),('FADEL, WEBER AND LABADIE','13969 Homenick Roads'),('FADEL, WEBER AND LABADIE','604 Willms Green'),('FERRY GROUP','058 Rolfson Shore'),('FERRY GROUP','26063 Carter Shoal'),('FRANECKI LLC','0426 Gutmann Vista'),('FRANECKI LLC','5505 Hirthe Shore'),('FRANECKI, TURCOTTE AND REILLY','0382 Stoltenberg Parkways'),('FRANECKI, TURCOTTE AND REILLY','7774 Towne Brook'),('FRIESEN AND SONS','438 Buckridge Knolls'),('FRIESEN AND SONS','59345 Mueller Well'),('GOLDNER-HODKIEWICZ','22117 Juliann Court'),('GOLDNER-HODKIEWICZ','42883 Weimann Summit'),('GOODWIN, MAYERT AND BLOCK','4023 Deloris Overpass'),('GOODWIN, MAYERT AND BLOCK','89984 Ocie Heights'),('GREEN, REICHERT AND WINDLER','151 So Summit'),('GREEN, REICHERT AND WINDLER','91157 Holly Path'),('GUSIKOWSKI-KOHLER','3385 Lind Isle'),('GUSIKOWSKI-KOHLER','9777 O\'Reilly Points'),('GUTKOWSKI AND SONS','187 Reilly Overpass'),('GUTKOWSKI AND SONS','8076 Langworth Burgs'),('HALVORSON LLC','4300 Lloyd Burgs'),('HALVORSON LLC','587 Sixta Dam'),('HARRIS, BERGNAUM AND UPTON','21262 Bayer Loaf'),('HARRIS, BERGNAUM AND UPTON','359 Barton Fork'),('HERMAN-QUIGLEY','034 Keitha Station'),('HERMAN-QUIGLEY','09684 Carter Valleys'),('HETTINGER, HEGMANN AND BOGISICH','107 Phylis Mountain'),('HETTINGER, HEGMANN AND BOGISICH','15144 Leonard Hollow'),('HIRTHE INC','0398 Clyde Grove'),('HIRTHE INC','693 Hansen Stravenue'),('HUEL AND SONS','367 Brock Burgs'),('HUEL AND SONS','8702 Loris Crossing'),('JOHNS, KIRLIN AND LUETTGEN','481 Omega Track'),('JOHNS, KIRLIN AND LUETTGEN','99266 Dorie Crescent'),('KERTZMANN, RUNOLFSDOTTIR AND WISOKY','3540 Ida Flats'),('KERTZMANN, RUNOLFSDOTTIR AND WISOKY','7039 Elly Loop'),('KING-GUTMANN','2916 Dannie Locks'),('KING-GUTMANN','825 Bayer Roads'),('KOCH LLC','263 Lilliana Expressway'),('KOCH LLC','331 Crona Track'),('KOELPIN INC','39138 Jeffrey Square'),('KOELPIN INC','4124 Jacobi Falls'),('KREIGER-ERDMAN','6245 Kozey Streets'),('KREIGER-ERDMAN','80788 Robel Glens'),('KUHIC-HOWELL','0248 Jamel Run'),('KUHIC-HOWELL','921 Melva Prairie'),('KULAS INC','09616 Bobbie Ramp'),('KULAS INC','219 Larry Overpass'),('LABADIE AND SONS','60680 Joshua Street'),('LABADIE AND SONS','913 Wolf Valleys'),('MANN, JACOBI AND JACOBSON','3186 Thaddeus Square'),('MANN, JACOBI AND JACOBSON','49823 Renner Rest'),('MCKENZIE AND SONS','2047 Marva Extensions'),('MCKENZIE AND SONS','749 Daniel Mount'),('MOEN GROUP','24426 Kreiger Streets'),('MOEN GROUP','9579 Irish Hills'),('MUELLER GROUP','157 Amos Overpass'),('MUELLER GROUP','5853 Dibbert Underpass'),('PFEFFER INC','703 Glynda Summit'),('PFEFFER INC','817 Oswaldo Street'),('POUROS, HOMENICK AND BAUCH','2567 Sharleen Trace'),('POUROS, HOMENICK AND BAUCH','756 King Road'),('PREDOVIC, CRIST AND MITCHELL','1225 Bednar Oval'),('PREDOVIC, CRIST AND MITCHELL','81441 Darron Island'),('QUIGLEY-SMITHAM','1488 Cummerata Vista'),('QUIGLEY-SMITHAM','2452 Gabriel Walks'),('RATKE INC','3299 Daniel Knoll'),('RATKE INC','778 Lee Gardens'),('RUNOLFSSON-NIKOLAUS','77884 Denesik Course'),('RUNOLFSSON-NIKOLAUS','8258 Ortiz Ports'),('SCHAMBERGER, KLING AND STROSIN','3475 Rau Mills'),('SCHAMBERGER, KLING AND STROSIN','919 Sharan Forge'),('SCHINNER-KUB','0475 Lindgren Roads'),('SCHINNER-KUB','811 Ramiro Fords'),('SCHMIDT GROUP','42214 Melvina Springs'),('SCHMIDT GROUP','549 Nubia Club'),('SCHUMM INC','7659 Sixta Corners'),('SCHUMM INC','78942 German Route'),('STOLTENBERG-STEUBER','335 Troy Views'),('STOLTENBERG-STEUBER','670 Maia Hill'),('SWIFT, ORN AND ROHAN','048 Keith Rest'),('SWIFT, ORN AND ROHAN','49143 Hal Turnpike'),('TROMP-GRANT','043 Charley Fort'),('TROMP-GRANT','890 Cyrus Drives'),('TURCOTTE-MURAZIK','07701 Joel Junctions'),('TURCOTTE-MURAZIK','868 O\'Keefe Heights'),('WILL GROUP','53286 Kozey Dale'),('WILL GROUP','7339 Miller Oval'),('ZEMLAK, ZBONCAK AND GRIMES','2387 Jasmin Pass'),('ZEMLAK, ZBONCAK AND GRIMES','56544 Bogisich Parkway');
/*!40000 ALTER TABLE `suppliers_addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `operator` text COLLATE utf8mb4_general_ci NOT NULL,
  `type` text COLLATE utf8mb4_general_ci NOT NULL,
  `timestamp` datetime NOT NULL,
  `item` text COLLATE utf8mb4_general_ci NOT NULL,
  `dc` text COLLATE utf8mb4_general_ci NOT NULL,
  `pos` text COLLATE utf8mb4_general_ci NOT NULL,
  `amount` int NOT NULL,
  `sn` text COLLATE utf8mb4_general_ci,
  `pt` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`),
  CONSTRAINT `transactions_chk_1` CHECK ((`amount` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'testdb'
--

--
-- Dumping routines for database 'testdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-07-17 11:59:44
