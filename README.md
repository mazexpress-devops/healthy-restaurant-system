# Healthy Restaurant Orders System

Java Swing and MySQL system for managing healthy restaurant orders.
The project is kept as a simple educational desktop system with three permissions:

```text
1 - Customer
2 - Chef
3 - Admin
```

Permissions are stored as integer values in `User`, not as a separate role class or table.

## Run

The Maven project runs the Swing desktop application by default:

```text
com.healthyrestaurant.ui.SwingApp
```

From the project folder:

```bash
mvn clean compile exec:java
```

You can also open the folder in NetBeans as a Maven project and run it from there.

## Requirements

- JDK 11 or newer.
- Maven.
- MySQL Server.

## Database Setup

Create and seed the database from the project folder:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

If your MySQL password is not empty, create:

```text
config/db.properties
```

Use this format:

```properties
db.url=jdbc:mysql://localhost:3306/healthy_restaurant?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
db.user=root
db.password=your_mysql_password
```

## Demo Accounts

Customer table accounts:

```text
table1 / 1
table2 / 2
...
table10 / 10
```

Staff accounts:

```text
chef / chef123
admin / admin123
```

## Project Structure

```text
database/                         MySQL schema and seed scripts
config/db.properties.example      Example MySQL connection config
src/main/java/com/healthyrestaurant/config
src/main/java/com/healthyrestaurant/dao
src/main/java/com/healthyrestaurant/model
src/main/java/com/healthyrestaurant/mysql
src/main/java/com/healthyrestaurant/service
src/main/java/com/healthyrestaurant/ui
src/main/java/com/healthyrestaurant/util
```
