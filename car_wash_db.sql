-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: localhost    Database: car_wash_db
-- ------------------------------------------------------
-- Server version	8.0.44-0ubuntu0.22.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `car_id` int NOT NULL,
  `service_id` int NOT NULL,
  `team_id` int DEFAULT NULL,
  `vehicle_id` int DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location_lat` decimal(10,8) DEFAULT NULL,
  `location_lng` decimal(11,8) DEFAULT NULL,
  `scheduled_time` datetime NOT NULL,
  `status` enum('pending','assigned','in_progress','completed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `total_price` decimal(10,2) NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `completed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `car_id` (`car_id`),
  KEY `service_id` (`service_id`),
  KEY `team_id` (`team_id`),
  KEY `vehicle_id` (`vehicle_id`),
  KEY `idx_booking_status` (`status`),
  KEY `idx_booking_scheduled` (`scheduled_time`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`),
  CONSTRAINT `bookings_ibfk_4` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE SET NULL,
  CONSTRAINT `bookings_ibfk_5` FOREIGN KEY (`vehicle_id`) REFERENCES `company_vehicles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,1,1,1,1,1,'Ramallah, Ein Munjid',NULL,NULL,'2026-01-15 09:00:00','assigned',10.00,'Please arrive on time','2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(2,1,4,2,NULL,NULL,'Ramallah, Al-Irsal',NULL,NULL,'2026-01-16 10:00:00','pending',25.00,NULL,'2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(3,2,3,5,2,2,'Al-Bireh, Main Street',NULL,NULL,'2026-01-13 14:00:00','completed',50.00,NULL,'2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(4,6,5,3,5,5,'Ramallah, Downtown',NULL,NULL,'2026-01-14 11:00:00','in_progress',30.00,'Extra attention to wheels','2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(5,7,6,4,NULL,NULL,'Beituniya, Near Mosque',NULL,NULL,'2026-01-17 08:00:00','pending',20.00,NULL,'2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(6,8,7,2,4,4,'Ramallah, Old City',NULL,NULL,'2026-01-15 15:00:00','assigned',25.00,NULL,'2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(7,6,5,1,1,1,'Ramallah, Ein Munjid',NULL,NULL,'2026-01-12 10:00:00','completed',10.00,NULL,'2026-01-11 11:40:07','2026-01-11 11:40:07',NULL),(8,1,1,4,NULL,NULL,'Ramallah, Al-Masyoun',NULL,NULL,'2026-01-18 09:00:00','cancelled',20.00,'Changed my mind','2026-01-11 11:40:07','2026-01-11 11:40:07',NULL);
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cars`
--

DROP TABLE IF EXISTS `cars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cars` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `plate_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `color` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_plate` (`plate_number`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `cars_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cars`
--

LOCK TABLES `cars` WRITE;
/*!40000 ALTER TABLE `cars` DISABLE KEYS */;
INSERT INTO `cars` VALUES (1,1,'Toyota Camry','12-345-67','White',2020,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(2,1,'Honda Civic','23-456-78','Black',2019,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(3,2,'Hyundai Elantra','34-567-89','Silver',2021,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(4,1,'Mazda 3','45-678-90','Blue',2018,'2026-01-11 11:40:07','2026-01-11 11:40:07'),(5,6,'Kia Sportage','56-789-01','Red',2022,'2026-01-11 11:40:07','2026-01-11 11:40:07'),(6,6,'Mercedes C-Class','67-890-12','Black',2021,'2026-01-11 11:40:07','2026-01-11 11:40:07'),(7,7,'BMW X5','78-901-23','White',2023,'2026-01-11 11:40:07','2026-01-11 11:40:07'),(8,8,'Chevrolet Malibu','89-012-34','Gray',2019,'2026-01-11 11:40:07','2026-01-11 11:40:07');
/*!40000 ALTER TABLE `cars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_vehicles`
--

DROP TABLE IF EXISTS `company_vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_vehicles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plate_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` int DEFAULT NULL,
  `status` enum('available','in_use','maintenance') COLLATE utf8mb4_unicode_ci DEFAULT 'available',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `plate_number` (`plate_number`),
  KEY `idx_vehicle_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_vehicles`
--

LOCK TABLES `company_vehicles` WRITE;
/*!40000 ALTER TABLE `company_vehicles` DISABLE KEYS */;
INSERT INTO `company_vehicles` VALUES (1,'CW-001','Toyota Hilux',2022,'available','2026-01-11 11:37:41','2026-01-11 11:37:41'),(2,'CW-002','Nissan Patrol',2021,'available','2026-01-11 11:37:41','2026-01-11 11:37:41'),(3,'CW-003','Ford Ranger',2023,'available','2026-01-11 11:37:41','2026-01-11 11:37:41'),(4,'CW-004','Mitsubishi L200',2022,'maintenance','2026-01-11 11:37:41','2026-01-11 11:37:41'),(5,'CW-005','Isuzu D-Max',2023,'available','2026-01-11 11:40:07','2026-01-11 11:40:07'),(6,'CW-006','Toyota Land Cruiser',2021,'in_use','2026-01-11 11:40:07','2026-01-11 11:40:07');
/*!40000 ALTER TABLE `company_vehicles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('booking','payment','general') COLLATE utf8mb4_unicode_ci DEFAULT 'general',
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,1,'Booking Confirmed','Your Normal Wash is scheduled for Jan 15 at 9:00 AM','booking',1,'2026-01-11 11:40:09'),(2,1,'Booking Pending','Your VIP Wash request is waiting for team assignment','booking',0,'2026-01-11 11:40:09'),(3,2,'Service Completed','Your Full Detailing has been completed','booking',1,'2026-01-11 11:40:09'),(4,6,'Wash In Progress','Team Delta is currently washing your car','booking',0,'2026-01-11 11:40:09'),(5,9,'New Job Assigned','You have a new job at Ramallah, Ein Munjid','booking',0,'2026-01-11 11:40:09'),(6,10,'Job In Progress','Job at Ramallah, Downtown is ongoing','booking',1,'2026-01-11 11:40:09');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `price` decimal(10,2) NOT NULL,
  `duration` int NOT NULL COMMENT 'Duration in minutes',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
INSERT INTO `services` VALUES (1,'Normal Wash','Basic exterior wash',10.00,30,1,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(2,'VIP Wash','Premium wash with wax',25.00,60,1,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(3,'Steam Wash','Deep steam cleaning',30.00,45,1,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(4,'Interior Cleaning','Vacuum and detailing',20.00,40,1,'2026-01-11 11:37:41','2026-01-11 11:37:41'),(5,'Full Detailing','Complete service',50.00,90,1,'2026-01-11 11:37:41','2026-01-11 11:37:41');
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teams` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `employee_id` int DEFAULT NULL,
  `status` enum('available','busy','offline') COLLATE utf8mb4_unicode_ci DEFAULT 'available',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `employee_id` (`employee_id`),
  KEY `idx_team_status` (`status`),
  CONSTRAINT `teams_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teams`
--

LOCK TABLES `teams` WRITE;
/*!40000 ALTER TABLE `teams` DISABLE KEYS */;
INSERT INTO `teams` VALUES (1,'Team Alpha',3,'available','2026-01-11 11:37:41','2026-01-11 11:37:41'),(2,'Team Beta',4,'available','2026-01-11 11:37:41','2026-01-11 11:37:41'),(3,'Team Gamma',NULL,'offline','2026-01-11 11:37:41','2026-01-11 11:37:41'),(4,'Team Delta',9,'available','2026-01-11 11:40:07','2026-01-11 11:40:07'),(5,'Team Echo',10,'busy','2026-01-11 11:40:07','2026-01-11 11:40:07'),(6,'Team Foxtrot',NULL,'available','2026-01-11 11:40:07','2026-01-11 11:40:07');
/*!40000 ALTER TABLE `teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `booking_id` int DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `type` enum('credit','debit') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `booking_id` (`booking_id`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,NULL,100.00,'credit','Added funds to wallet','2026-01-11 11:40:07'),(2,2,NULL,200.00,'credit','Added funds to wallet','2026-01-11 11:40:07'),(3,6,NULL,150.00,'credit','Added funds to wallet','2026-01-11 11:40:07'),(4,7,NULL,100.00,'credit','Added funds to wallet','2026-01-11 11:40:07'),(5,8,NULL,75.00,'credit','Added funds to wallet','2026-01-11 11:40:07'),(6,1,1,10.00,'debit','Payment for Normal Wash','2026-01-11 11:40:07'),(7,1,2,25.00,'debit','Payment for VIP Wash','2026-01-11 11:40:07'),(8,2,3,50.00,'debit','Payment for Full Detailing','2026-01-11 11:40:07'),(9,6,4,30.00,'debit','Payment for Steam Wash','2026-01-11 11:40:07'),(10,6,7,10.00,'debit','Payment for Normal Wash','2026-01-11 11:40:07'),(11,8,6,25.00,'debit','Payment for VIP Wash','2026-01-11 11:40:07'),(12,1,8,20.00,'credit','Refund for cancelled booking','2026-01-11 11:40:07');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('customer','employee','manager') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_user_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
-- Default password for all users is "Password123" (bcrypt hashed)
INSERT INTO `users` VALUES (1,'Ahmad Ali','ahmad@example.com','0599123456','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','customer','2026-01-11 11:37:41','2026-01-11 11:37:41'),(2,'Sarah Mohammed','sarah@example.com','0598765432','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','customer','2026-01-11 11:37:41','2026-01-11 11:37:41'),(3,'Khaled Ibrahim','khaled@example.com','0597654321','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','employee','2026-01-11 11:37:41','2026-01-11 11:37:41'),(4,'Omar Hassan','omar@example.com','0596543210','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','employee','2026-01-11 11:37:41','2026-01-11 11:37:41'),(5,'Fatima Nasser','fatima@example.com','0595432109','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','manager','2026-01-11 11:37:41','2026-01-11 11:37:41'),(6,'Mohammed Saleh','mohammed@example.com','0597777777','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','customer','2026-01-11 11:40:07','2026-01-11 11:40:07'),(7,'Layla Hassan','layla@example.com','0598888888','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','customer','2026-01-11 11:40:07','2026-01-11 11:40:07'),(8,'Yusuf Khaled','yusuf@example.com','0599999999','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','customer','2026-01-11 11:40:07','2026-01-11 11:40:07'),(9,'Nour Ahmad','nour@example.com','0596666666','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','employee','2026-01-11 11:40:07','2026-01-11 11:40:07'),(10,'Rami Zaid','rami@example.com','0595555555','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','employee','2026-01-11 11:40:07','2026-01-11 11:40:07'),(11,'Dina Mahmoud','dina@example.com','0594444444','$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi','manager','2026-01-11 11:40:07','2026-01-11 11:40:07');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-11 13:53:10
