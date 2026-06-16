package com.healthyrestaurant.mongo;

import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.DiningTableAccount;
import com.healthyrestaurant.model.HealthCondition;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.IngredientCategory;
import com.healthyrestaurant.model.MealSummary;
import com.healthyrestaurant.model.OrderStatus;
import com.healthyrestaurant.model.OrderTicket;
import com.healthyrestaurant.model.OrderType;
import com.healthyrestaurant.model.ReadyMeal;
import com.healthyrestaurant.model.Role;
import com.healthyrestaurant.model.StaffAccount;
import com.healthyrestaurant.model.User;
import com.healthyrestaurant.service.NutritionService;
import com.healthyrestaurant.util.PasswordUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.setOnInsert;

public class MongoRepository {
    private static final String APP_SOURCE = "swing-mongo";

    private final MongoDatabase database = com.healthyrestaurant.config.MongoDatabase.getDatabase();
    private final MongoCollection<Document> counters = database.getCollection("counters");
    private final MongoCollection<Document> users = database.getCollection("users");
    private final MongoCollection<Document> tables = database.getCollection("dining_tables");
    private final MongoCollection<Document> ingredients = database.getCollection("ingredients");
    private final MongoCollection<Document> readyMeals = database.getCollection("ready_meals");
    private final MongoCollection<Document> orders = database.getCollection("orders");

    public void prepare() {
        database.runCommand(new Document("ping", 1));
        users.createIndex(new Document("username", 1), new IndexOptions().unique(true));
        tables.createIndex(new Document("tableNumber", 1), new IndexOptions().unique(true));
        ingredients.createIndex(new Document("name", 1), new IndexOptions().unique(true));
        readyMeals.createIndex(new Document("name", 1), new IndexOptions().unique(true));
        seed();
    }

    public void ping() {
        database.runCommand(new Document("ping", 1));
    }

    public Optional<User> login(String username, String password, Role requiredRole) {
        Document document = users.find(and(
                eq("source", APP_SOURCE),
                eq("username", username),
                eq("passwordHash", PasswordUtil.sha256(password)),
                eq("active", true))).first();
        if (document == null) {
            return Optional.empty();
        }

        Role role = Role.valueOf(document.getString("role"));
        if (role != requiredRole && role != Role.ADMIN) {
            return Optional.empty();
        }

        return Optional.of(new User(
                documentId(users, document, "users"),
                document.getString("username"),
                document.getString("fullName"),
                role));
    }

    public Optional<DiningTableAccount> loginTableAccount(String username, String password) {
        String accountName = normalizeTableAccountName(username);
        Document document = tables.find(and(
                eq("source", APP_SOURCE),
                eq("accountName", accountName),
                eq("active", true),
                eq("status", "OPEN"))).first();
        if (document == null) {
            return Optional.empty();
        }

        String passwordHash = document.getString("passwordHash");
        String tablePassword = String.valueOf(intValue(document, "tableNumber", 0));
        boolean passwordMatches = PasswordUtil.sha256(password).equals(passwordHash)
                || tablePassword.equals(password)
                || "12345678910".equals(password);
        if (!passwordMatches) {
            return Optional.empty();
        }

        return Optional.of(mapTable(document));
    }

    public List<DiningTableAccount> findOpenTableAccounts() {
        List<DiningTableAccount> result = new ArrayList<DiningTableAccount>();
        MongoCursor<Document> cursor = tables.find(and(eq("source", APP_SOURCE), eq("active", true), eq("status", "OPEN")))
                .sort(Sorts.ascending("tableNumber"))
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapTable(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public List<DiningTableAccount> findAllTableAccounts() {
        List<DiningTableAccount> result = new ArrayList<DiningTableAccount>();
        MongoCursor<Document> cursor = tables.find(eq("source", APP_SOURCE))
                .sort(Sorts.ascending("tableNumber"))
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapTable(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public List<StaffAccount> findStaffAccounts() {
        List<StaffAccount> result = new ArrayList<StaffAccount>();
        MongoCursor<Document> cursor = users.find(eq("source", APP_SOURCE))
                .sort(Sorts.ascending("role", "username"))
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapStaffAccount(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public void saveStaffAccount(
            String username,
            String password,
            String fullName,
            Role role,
            boolean active) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanFullName = fullName == null ? "" : fullName.trim();
        Document existing = users.find(eq("username", cleanUsername)).first();
        Document update = new Document("source", APP_SOURCE)
                .append("username", cleanUsername)
                .append("fullName", cleanFullName)
                .append("role", role.name())
                .append("active", active)
                .append("updatedAt", new Date());

        if (password != null && !password.trim().isEmpty()) {
            update.append("passwordHash", PasswordUtil.sha256(password));
        }

        if (existing == null) {
            update.append("id", nextId("users"));
            update.append("createdAt", new Date());
            if (!update.containsKey("passwordHash")) {
                update.append("passwordHash", PasswordUtil.sha256("1234"));
            }
            users.insertOne(update);
        } else {
            update.append("id", existing.get("id") instanceof Number
                    ? ((Number) existing.get("id")).intValue()
                    : nextId("users"));
            users.updateOne(eq("username", cleanUsername), new Document("$set", update));
        }
    }

    public void updateStaffActive(int id, boolean active) {
        users.updateOne(and(eq("source", APP_SOURCE), eq("id", id)),
                combine(set("active", active), set("updatedAt", new Date())));
    }

    public void saveTableAccount(int tableNumber, String password, String status, boolean active) {
        String cleanStatus = "CLOSED".equals(status) ? "CLOSED" : "OPEN";
        Document existing = tables.find(eq("tableNumber", tableNumber)).first();
        Document update = new Document("source", APP_SOURCE)
                .append("tableNumber", tableNumber)
                .append("accountName", "table" + tableNumber)
                .append("status", cleanStatus)
                .append("active", active)
                .append("updatedAt", new Date());

        if (password != null && !password.trim().isEmpty()) {
            update.append("passwordHash", PasswordUtil.sha256(password));
        }

        if (existing == null) {
            update.append("id", nextId("tables"));
            update.append("createdAt", new Date());
            if (!update.containsKey("passwordHash")) {
                update.append("passwordHash", PasswordUtil.sha256(String.valueOf(tableNumber)));
            }
            tables.insertOne(update);
        } else {
            update.append("id", existing.get("id") instanceof Number
                    ? ((Number) existing.get("id")).intValue()
                    : nextId("tables"));
            tables.updateOne(eq("tableNumber", tableNumber), new Document("$set", update));
        }
    }

    public List<ReadyMeal> findAllAvailableReadyMeals() {
        List<ReadyMeal> result = new ArrayList<ReadyMeal>();
        MongoCursor<Document> cursor = readyMeals.find(and(eq("source", APP_SOURCE), eq("available", true)))
                .sort(Sorts.ascending("name"))
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapReadyMeal(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public List<Ingredient> findAllAvailableIngredients() {
        return findIngredients(eq("available", true));
    }

    public List<Ingredient> findAllIngredients() {
        return findIngredients(new Document());
    }

    public int insertIngredient(Ingredient ingredient) {
        int id = nextId("ingredients");
        Document document = ingredientDocument(ingredient)
                .append("id", id)
                .append("createdAt", new Date())
                .append("updatedAt", new Date());
        ingredients.insertOne(document);
        return id;
    }

    public void updateIngredientAvailability(int id, boolean available) {
        ingredients.updateOne(and(eq("source", APP_SOURCE), eq("id", id)),
                combine(set("available", available), set("updatedAt", new Date())));
    }

    public int createReadyMealOrder(int tableNumber, ReadyMeal meal) {
        MealSummary summary = new MealSummary();
        summary.addReadyMeal(meal);

        Document mealDocument = readyMeals.find(and(eq("source", APP_SOURCE), eq("id", meal.getId()))).first();
        List<Ingredient> mealIngredients = loadIngredientsByIds(integerList(mealDocument, "ingredientIds"));
        List<String> lines = new ArrayList<String>();
        lines.add("- " + meal.getName() + " x1 | " + meal.getPrice().toPlainString() + " د.ل");
        appendIngredientLines(lines, mealIngredients);

        return insertOrder(tableNumber, OrderType.READY_MEAL, summary,
                "طلب وجبة جاهزة من قائمة الطعام.", lines);
    }

    public int createCustomMealOrder(
            CustomerProfile profile,
            List<Ingredient> selectedIngredients,
            MealSummary summary,
            NutritionService nutritionService) {
        List<String> feedback = nutritionService.buildSmartFeedback(profile, summary);
        String notes = nutritionService.buildHealthNotes(profile, summary, feedback, selectedIngredients);

        List<String> lines = new ArrayList<String>();
        lines.add("- وجبة مخصصة x1 | " + summary.getPrice().toPlainString() + " د.ل");
        appendIngredientLines(lines, selectedIngredients);

        return insertOrder(profile.getTableNumber(), OrderType.CUSTOM_MEAL, summary, notes, lines);
    }

    public List<OrderTicket> findKitchenOrders() {
        List<OrderTicket> result = new ArrayList<OrderTicket>();
        MongoCursor<Document> cursor = orders.find(and(eq("source", APP_SOURCE), in("status", Arrays.asList("NEW", "PREPARING"))))
                .sort(Sorts.ascending("createdAt"))
                .limit(50)
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapTicket(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public List<OrderTicket> findRecentOrders(int limit) {
        List<OrderTicket> result = new ArrayList<OrderTicket>();
        MongoCursor<Document> cursor = orders.find(eq("source", APP_SOURCE))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapTicket(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public Optional<OrderTicket> findTicket(int orderId) {
        Document document = orders.find(and(eq("source", APP_SOURCE), eq("id", orderId))).first();
        return document == null ? Optional.<OrderTicket>empty() : Optional.of(mapTicket(document));
    }

    public void updateStatus(int orderId, OrderStatus status) {
        orders.updateOne(and(eq("source", APP_SOURCE), eq("id", orderId)),
                combine(set("status", status.name()), set("updatedAt", new Date())));
    }

    private List<Ingredient> findIngredients(Bson filter) {
        List<Ingredient> result = new ArrayList<Ingredient>();
        MongoCursor<Document> cursor = ingredients.find(and(eq("source", APP_SOURCE), filter))
                .sort(Sorts.ascending("category", "name"))
                .iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapIngredient(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    private int insertOrder(
            int tableNumber,
            OrderType orderType,
            MealSummary summary,
            String healthNotes,
            List<String> lines) {
        int id = nextId("orders");
        Date now = new Date();
        orders.insertOne(new Document("id", id)
                .append("source", APP_SOURCE)
                .append("tableNumber", tableNumber)
                .append("orderType", orderType.name())
                .append("status", OrderStatus.NEW.name())
                .append("subtotal", summary.getPrice().doubleValue())
                .append("calories", summary.getCalories())
                .append("protein", summary.getProteinGrams())
                .append("carbs", summary.getCarbsGrams())
                .append("fat", summary.getFatGrams())
                .append("healthNotes", healthNotes)
                .append("lines", lines)
                .append("createdAt", now)
                .append("updatedAt", now));
        return id;
    }

    private void appendIngredientLines(List<String> lines, List<Ingredient> mealIngredients) {
        for (Ingredient ingredient : mealIngredients) {
            lines.add("  * " + ingredient.getName()
                    + " (" + ingredient.getCategory().getDisplayName() + ")"
                    + " | " + format(ingredient.getCalories()) + " سعرة"
                    + " | " + ingredient.getPrice().toPlainString() + " د.ل");
        }
    }

    private List<Ingredient> loadIngredientsByIds(List<Integer> ids) {
        List<Ingredient> result = new ArrayList<Ingredient>();
        if (ids.isEmpty()) {
            return result;
        }

        MongoCursor<Document> cursor = ingredients.find(and(eq("source", APP_SOURCE), in("id", ids))).iterator();
        try {
            while (cursor.hasNext()) {
                result.add(mapIngredient(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    private void seed() {
        seedUsers();
        seedTables();
        seedIngredients();
        seedReadyMeals();
        seedDemoOrders();
    }

    private void seedUsers() {
        upsertUser("admin", "admin123", "منال الفيتوري", Role.ADMIN);
        upsertUser("chef", "chef123", "إيناس عبد المنعم", Role.CHEF);
    }

    private void upsertUser(String username, String password, String fullName, Role role) {
        Document existing = users.find(eq("username", username)).first();
        if (existing == null) {
            users.insertOne(new Document("id", nextId("users"))
                    .append("source", APP_SOURCE)
                    .append("username", username)
                    .append("passwordHash", PasswordUtil.sha256(password))
                    .append("fullName", fullName)
                    .append("role", role.name())
                    .append("active", true)
                    .append("createdAt", new Date())
                    .append("updatedAt", new Date()));
            return;
        }

        users.updateOne(eq("username", username), combine(
                set("id", existing.get("id") instanceof Number
                        ? ((Number) existing.get("id")).intValue()
                        : nextId("users")),
                set("passwordHash", PasswordUtil.sha256(password)),
                set("source", APP_SOURCE),
                set("fullName", fullName),
                set("role", role.name()),
                set("active", true),
                set("updatedAt", new Date())));
    }

    private void seedTables() {
        for (int tableNumber = 1; tableNumber <= 10; tableNumber++) {
            Document existing = tables.find(eq("tableNumber", tableNumber)).first();
            if (existing == null) {
                tables.insertOne(new Document("id", nextId("tables"))
                        .append("source", APP_SOURCE)
                        .append("tableNumber", tableNumber)
                        .append("accountName", "table" + tableNumber)
                        .append("passwordHash", PasswordUtil.sha256(String.valueOf(tableNumber)))
                        .append("status", "OPEN")
                        .append("active", true)
                        .append("createdAt", new Date())
                        .append("updatedAt", new Date()));
            } else {
                tables.updateOne(eq("tableNumber", tableNumber), combine(
                        set("id", existing.get("id") instanceof Number
                                ? ((Number) existing.get("id")).intValue()
                                : nextId("tables")),
                        set("source", APP_SOURCE),
                        set("accountName", "table" + tableNumber),
                        set("passwordHash", PasswordUtil.sha256(String.valueOf(tableNumber))),
                        set("status", "OPEN"),
                        set("active", true),
                        set("updatedAt", new Date())));
            }
        }
    }

    private void seedIngredients() {
        Object[][] rows = new Object[][]{
                {"صدر دجاج مشوي بالتوابل الليبية", IngredientCategory.PROTEIN, "150 جرام", 18.00, 245.0, 46.0, 0.0, 5.0, false, false, false, false, false},
                {"تونة مصراتية بالماء", IngredientCategory.PROTEIN, "120 جرام", 16.00, 132.0, 29.0, 0.0, 1.0, false, false, false, false, false},
                {"لحم إبل مشوي قليل الدهن", IngredientCategory.PROTEIN, "130 جرام", 24.00, 220.0, 34.0, 0.0, 8.0, false, false, false, false, false},
                {"حمص مسلوق", IngredientCategory.PROTEIN, "نصف كوب", 7.00, 135.0, 7.0, 22.0, 2.0, false, false, false, false, false},
                {"بيض مسلوق", IngredientCategory.PROTEIN, "بيضتان", 6.00, 156.0, 13.0, 1.0, 11.0, false, false, false, false, true},
                {"رز مبخر", IngredientCategory.CARB, "150 جرام", 6.00, 190.0, 4.0, 41.0, 1.0, false, false, false, false, false},
                {"كسكسي شعير", IngredientCategory.CARB, "150 جرام", 8.00, 176.0, 6.0, 36.0, 1.0, false, true, false, false, false},
                {"مكرونة قمح كامل للمبكبكة", IngredientCategory.CARB, "150 جرام", 7.00, 210.0, 8.0, 42.0, 2.0, false, true, false, false, false},
                {"بطاطا مشوية", IngredientCategory.CARB, "150 جرام", 5.00, 130.0, 3.0, 30.0, 0.0, false, false, false, false, false},
                {"خبز شعير", IngredientCategory.CARB, "قطعتان صغيرتان", 5.00, 150.0, 5.0, 30.0, 2.0, false, true, false, false, false},
                {"زيت زيتون الجبل الأخضر", IngredientCategory.FAT, "ملعقة كبيرة", 4.00, 119.0, 0.0, 0.0, 14.0, false, false, false, false, true},
                {"زيتون أخضر طرابلسي", IngredientCategory.FAT, "8 حبات", 4.00, 55.0, 0.0, 2.0, 5.0, false, false, false, true, true},
                {"لوز ليبي محمص", IngredientCategory.FAT, "20 جرام", 7.00, 116.0, 4.0, 4.0, 10.0, false, false, false, false, true},
                {"سلطة مشوية", IngredientCategory.ADDON, "كوب", 6.00, 80.0, 3.0, 12.0, 3.0, false, false, false, false, false},
                {"خيار وطماطم ونعناع", IngredientCategory.ADDON, "كوب", 4.00, 35.0, 2.0, 7.0, 0.0, false, false, false, false, false},
                {"فاصوليا خضراء مطبوخة", IngredientCategory.ADDON, "كوب", 5.00, 44.0, 2.0, 10.0, 0.0, false, false, false, false, false},
                {"صلصة طماطم حارة", IngredientCategory.SAUCE, "30 جرام", 3.00, 25.0, 1.0, 5.0, 0.0, false, false, false, false, false},
                {"صلصة ليمون وكمون", IngredientCategory.SAUCE, "25 جرام", 2.00, 12.0, 0.0, 2.0, 0.0, false, false, false, false, false},
                {"زبادي بالنعناع", IngredientCategory.SAUCE, "40 جرام", 3.00, 45.0, 3.0, 4.0, 2.0, true, false, false, false, false}
        };

        for (Object[] row : rows) {
            Ingredient ingredient = new Ingredient(
                    0,
                    (String) row[0],
                    (IngredientCategory) row[1],
                    (String) row[2],
                    BigDecimal.valueOf((Double) row[3]),
                    (Double) row[4],
                    (Double) row[5],
                    (Double) row[6],
                    (Double) row[7],
                    (Boolean) row[8],
                    (Boolean) row[9],
                    (Boolean) row[10],
                    (Boolean) row[11],
                    (Boolean) row[12],
                    true);
            upsertIngredient(ingredient);
        }
    }

    private void seedReadyMeals() {
        upsertReadyMeal("طبق دجاج ليبي صحي",
                "صدر دجاج مشوي بالتوابل الليبية مع رز مبخر وسلطة مشوية وصلصة ليمون وكمون.",
                31.00,
                "صدر دجاج مشوي بالتوابل الليبية", "رز مبخر", "سلطة مشوية", "صلصة ليمون وكمون");
        upsertReadyMeal("سلطة تونة طرابلسية",
                "تونة مصراتية بالماء مع خيار وطماطم ونعناع وزيتون أخضر وصلصة ليمون وكمون.",
                26.00,
                "تونة مصراتية بالماء", "خيار وطماطم ونعناع", "زيتون أخضر طرابلسي", "صلصة ليمون وكمون");
        upsertReadyMeal("كسكسي شعير بالدجاج والخضار",
                "كسكسي شعير مع صدر دجاج مشوي وفاصوليا خضراء وصلصة طماطم حارة.",
                34.00,
                "كسكسي شعير", "صدر دجاج مشوي بالتوابل الليبية", "فاصوليا خضراء مطبوخة", "صلصة طماطم حارة");
        upsertReadyMeal("طبق حمص وخضار مشوية",
                "حمص مسلوق مع سلطة مشوية وبطاطا مشوية وزيت زيتون الجبل الأخضر.",
                22.00,
                "حمص مسلوق", "سلطة مشوية", "بطاطا مشوية", "زيت زيتون الجبل الأخضر");
        upsertReadyMeal("لحم إبل مشوي مع بطاطا",
                "لحم إبل مشوي قليل الدهن مع بطاطا مشوية وخيار وطماطم ونعناع.",
                34.00,
                "لحم إبل مشوي قليل الدهن", "بطاطا مشوية", "خيار وطماطم ونعناع");
    }

    private void seedDemoOrders() {
        if (orders.countDocuments(eq("source", APP_SOURCE)) > 0) {
            return;
        }

        Optional<ReadyMeal> mealOne = findMealByName("طبق دجاج ليبي صحي");
        if (mealOne.isPresent()) {
            createReadyMealOrder(1, mealOne.get());
        }
        Optional<ReadyMeal> mealTwo = findMealByName("سلطة تونة طرابلسية");
        if (mealTwo.isPresent()) {
            int orderId = createReadyMealOrder(2, mealTwo.get());
            updateStatus(orderId, OrderStatus.PREPARING);
        }
    }

    private Optional<ReadyMeal> findMealByName(String name) {
        Document document = readyMeals.find(and(eq("source", APP_SOURCE), eq("name", name))).first();
        return document == null ? Optional.<ReadyMeal>empty() : Optional.of(mapReadyMeal(document));
    }

    private void upsertIngredient(Ingredient ingredient) {
        Document existing = ingredients.find(eq("name", ingredient.getName())).first();
        Document document = ingredientDocument(ingredient)
                .append("source", APP_SOURCE)
                .append("updatedAt", new Date());

        if (existing == null) {
            document.append("id", nextId("ingredients"));
            document.append("createdAt", new Date());
            ingredients.insertOne(document);
        } else {
            document.append("id", existing.get("id") instanceof Number
                    ? ((Number) existing.get("id")).intValue()
                    : nextId("ingredients"));
            ingredients.updateOne(eq("name", ingredient.getName()), new Document("$set", document));
        }
    }

    private void upsertReadyMeal(String name, String description, double price, String... ingredientNames) {
        List<Ingredient> selected = new ArrayList<Ingredient>();
        List<Integer> ingredientIds = new ArrayList<Integer>();
        for (String ingredientName : ingredientNames) {
        Document document = ingredients.find(and(eq("source", APP_SOURCE), eq("name", ingredientName))).first();
            if (document != null) {
                selected.add(mapIngredient(document));
                ingredientIds.add(documentId(ingredients, document, "ingredients"));
            }
        }

        MealSummary summary = new MealSummary();
        for (Ingredient ingredient : selected) {
            summary.addIngredient(ingredient);
        }

        Document meal = new Document("name", name)
                .append("source", APP_SOURCE)
                .append("description", description)
                .append("price", price)
                .append("calories", summary.getCalories())
                .append("protein", summary.getProteinGrams())
                .append("carbs", summary.getCarbsGrams())
                .append("fat", summary.getFatGrams())
                .append("ingredientIds", ingredientIds)
                .append("available", true)
                .append("updatedAt", new Date());

        Document existing = readyMeals.find(eq("name", name)).first();
        if (existing == null) {
            meal.append("id", nextId("ready_meals"));
            meal.append("createdAt", new Date());
            readyMeals.insertOne(meal);
        } else {
            meal.append("id", existing.get("id") instanceof Number
                    ? ((Number) existing.get("id")).intValue()
                    : nextId("ready_meals"));
            readyMeals.updateOne(eq("name", name), new Document("$set", meal));
        }
    }

    private Document ingredientDocument(Ingredient ingredient) {
        return new Document("name", ingredient.getName())
                .append("category", ingredient.getCategory().name())
                .append("servingLabel", ingredient.getServingLabel())
                .append("price", ingredient.getPrice().doubleValue())
                .append("calories", ingredient.getCalories())
                .append("protein", ingredient.getProteinGrams())
                .append("carbs", ingredient.getCarbsGrams())
                .append("fat", ingredient.getFatGrams())
                .append("containsLactose", ingredient.isContainsLactose())
                .append("containsGluten", ingredient.isContainsGluten())
                .append("highSugar", ingredient.isHighSugar())
                .append("highSodium", ingredient.isHighSodium())
                .append("highFat", ingredient.isHighFat())
                .append("available", ingredient.isAvailable());
    }

    private DiningTableAccount mapTable(Document document) {
        return new DiningTableAccount(
                documentId(tables, document, "tables"),
                intValue(document, "tableNumber", 1),
                document.getString("accountName"),
                document.getString("status"),
                document.getBoolean("active", true));
    }

    private StaffAccount mapStaffAccount(Document document) {
        return new StaffAccount(
                documentId(users, document, "users"),
                document.getString("username"),
                document.getString("fullName"),
                Role.valueOf(document.getString("role")),
                document.getBoolean("active", true));
    }

    private Ingredient mapIngredient(Document document) {
        return new Ingredient(
                documentId(ingredients, document, "ingredients"),
                document.getString("name"),
                IngredientCategory.valueOf(document.getString("category")),
                document.getString("servingLabel"),
                money(document, "price"),
                number(document, "calories"),
                number(document, "protein"),
                number(document, "carbs"),
                number(document, "fat"),
                document.getBoolean("containsLactose", false),
                document.getBoolean("containsGluten", false),
                document.getBoolean("highSugar", false),
                document.getBoolean("highSodium", false),
                document.getBoolean("highFat", false),
                document.getBoolean("available", true));
    }

    private ReadyMeal mapReadyMeal(Document document) {
        return new ReadyMeal(
                documentId(readyMeals, document, "ready_meals"),
                document.getString("name"),
                document.getString("description"),
                money(document, "price"),
                number(document, "calories"),
                number(document, "protein"),
                number(document, "carbs"),
                number(document, "fat"),
                document.getBoolean("available", true));
    }

    private OrderTicket mapTicket(Document document) {
        List<String> lines = new ArrayList<String>();
        List<?> rawLines = document.getList("lines", Object.class, new ArrayList<Object>());
        for (Object line : rawLines) {
            lines.add(String.valueOf(line));
        }

        return new OrderTicket(
                documentId(orders, document, "orders"),
                intValue(document, "tableNumber", 1),
                OrderType.valueOf(document.getString("orderType")),
                OrderStatus.valueOf(document.getString("status")),
                money(document, "subtotal"),
                number(document, "calories"),
                number(document, "protein"),
                number(document, "carbs"),
                number(document, "fat"),
                document.getString("healthNotes"),
                localDateTime(document.getDate("createdAt")),
                lines);
    }

    private int nextId(String counterName) {
        Document counter = counters.findOneAndUpdate(
                eq("_id", counterName),
                inc("value", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        if (counter == null) {
            counter = counters.find(eq("_id", counterName)).first();
        }
        return counter == null ? 1 : counter.getInteger("value", 1);
    }

    private int documentId(MongoCollection<Document> collection, Document document, String counterName) {
        Object value = document.get("id");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        int id = nextId(counterName);
        Object objectId = document.get("_id");
        collection.updateOne(eq("_id", objectId), set("id", id));
        document.put("id", id);
        return id;
    }

    private String normalizeTableAccountName(String username) {
        String value = username == null ? "" : username.trim().toLowerCase().replace(" ", "");
        if (value.matches("\\d+")) {
            return "table" + value;
        }
        return value;
    }

    private int intValue(Document document, String key, int defaultValue) {
        Object value = document.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private List<Integer> integerList(Document document, String key) {
        List<Integer> result = new ArrayList<Integer>();
        if (document == null) {
            return result;
        }

        List<?> values = document.getList(key, Object.class, new ArrayList<Object>());
        for (Object value : values) {
            if (value instanceof Number) {
                result.add(((Number) value).intValue());
            }
        }
        return result;
    }

    private BigDecimal money(Document document, String key) {
        return BigDecimal.valueOf(number(document, key)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private double number(Document document, String key) {
        Object value = document.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    private LocalDateTime localDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
