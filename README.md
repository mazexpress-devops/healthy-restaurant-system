# Healthy Restaurant Orders System

## Java Swing / NetBeans Run

This Maven project is configured to run the Swing desktop application by default:

```text
com.healthyrestaurant.ui.SwingApp
```

Open the folder in NetBeans 10 as a Maven project, make sure JDK and Maven are configured, then click **Run Project**. The project targets Java 8 syntax for easier lab compatibility.

Database teaching points are kept in simple layers:

- `Database.java` loads the MySQL JDBC driver and opens connections with `DriverManager`.
- DAO classes use `PreparedStatement` and `ResultSet`.
- Swing screens use `JTable` with `DefaultTableModel` to show meals, ingredients, and orders.
- The UI calls services/DAO classes instead of writing SQL inside button handlers.
- Customer ordering uses ready-made table accounts (`table1` to `table10`), so customers do not create personal accounts.
- The first screen only chooses the intended user path. Customer, chef, and admin screens are not shown together.
- Chef and admin work screens stay hidden until login succeeds.
- `database/seed.sql` fills the system with Libyan sample data and two kitchen demo orders.

Current database mode:

- The Swing app currently uses local MySQL Server 8.4 at `127.0.0.1:3306`.
- Local MySQL data files are stored in `mysql-data/`.
- Import data with `database/schema.sql` and `database/seed.sql`.
- Customer table logins: `table1` / `1` through `table10` / `10`.
- Chef login: `chef` / `chef123`
- Admin login: `admin` / `admin123`

منظومة Java + MySQL لحجز طلبات مطعم صحي، مبنية على الدراسة المرفقة. النظام يعمل بواجهة كونسول ويغطي ثلاثة أدوار:

- الزبون: تصفح الوجبات الجاهزة أو إنشاء وجبة مخصصة.
- الشيف: استلام الطلبات وعرض المكونات والملاحظات الصحية وتحديث الحالة.
- مدير النظام: إدارة المكونات والقيم الغذائية والتوفر.

## أهم الوظائف

- حساب السعرات والسعر والمغذيات الكبرى بشكل لحظي.
- إدخال بيانات الزبون: العمر، الوزن، الطول، الهدف الرياضي، والحالة الصحية.
- منع المكونات المتعارضة مع الحالات الصحية مثل اللاكتوز، الغلوتين، السكري، الضغط، ودهون الكبد.
- إرسال الطلب للمطبخ مع كل التفاصيل الصحية والغذائية.
- حفظ الطلبات والمكونات والوجبات في MySQL.

## المتطلبات

- JDK 11 أو أحدث.
- Maven.
- MySQL Server.

## إعداد قاعدة البيانات

من داخل مجلد المشروع:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

عدّل إعدادات الاتصال عند الحاجة في:

```text
config/db.properties
```

## التشغيل

```bash
mvn clean compile exec:java
```

## النسخة الشغالة حاليا مع MongoDB

بما أن الجهاز الحالي لا يحتوي على Java/Maven في PATH ولا يوجد MySQL مثبت، تمت إضافة نسخة ويب شغالة على MongoDB المحلي الموجود لديك.

رابط التشغيل:

```text
http://127.0.0.1:3000
```

قاعدة البيانات:

```text
mongodb://127.0.0.1:27017/healthy_restaurant
```

تشغيلها مرة أخرى:

```powershell
npm.cmd start
```

المزايا المحسنة في نسخة Mongo:

- واجهة زبون بمسارين مطابقين للدراسة: تصفح المنيو وإنشاء وجبة مخصصة.
- إدخال بيانات العمر، الوزن، الطول، الهدف، والحالة الصحية.
- حساب لحظي للسعرات والسعر والبروتين والكربوهيدرات والدهون.
- منع تلقائي للمكونات التي تتعارض مع السكري، اللاكتوز، الغلوتين، الضغط، ودهون الكبد.
- شاشة شيف تعرض الطلبات والمكونات والملاحظات الصحية وتحديث الحالة.
- إشعارات للطاولة عند تغير حالة الطلب.
- طباعة فاتورة الطلب.
- لوحة مدير لإدارة المكونات، الوجبات الجاهزة، حسابات الطاقم، والتقارير.

## حسابات تجريبية

```text
مدير النظام:
username: admin
password: admin123

الشيف:
username: chef
password: chef123
```

## هيكل المشروع

```text
database/                         سكربتات MySQL
config/db.properties              إعدادات الاتصال
src/main/java/com/healthyrestaurant/app
src/main/java/com/healthyrestaurant/model
src/main/java/com/healthyrestaurant/dao
src/main/java/com/healthyrestaurant/service
src/main/java/com/healthyrestaurant/util
```
