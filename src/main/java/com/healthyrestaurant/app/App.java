package com.healthyrestaurant.app;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.dao.IngredientDao;
import com.healthyrestaurant.dao.OrderDao;
import com.healthyrestaurant.dao.ReadyMealDao;
import com.healthyrestaurant.dao.UserDao;
import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.Goal;
import com.healthyrestaurant.model.HealthCondition;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.IngredientCategory;
import com.healthyrestaurant.model.MealSummary;
import com.healthyrestaurant.model.OrderStatus;
import com.healthyrestaurant.model.OrderTicket;
import com.healthyrestaurant.model.ReadyMeal;
import com.healthyrestaurant.model.Role;
import com.healthyrestaurant.model.User;
import com.healthyrestaurant.service.AuthService;
import com.healthyrestaurant.service.NutritionService;
import com.healthyrestaurant.service.OrderService;
import com.healthyrestaurant.util.ConsoleInput;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    private final ConsoleInput input = new ConsoleInput(System.in);
    private final IngredientDao ingredientDao = new IngredientDao();
    private final ReadyMealDao readyMealDao = new ReadyMealDao();
    private final OrderDao orderDao = new OrderDao();
    private final NutritionService nutritionService = new NutritionService();
    private final OrderService orderService = new OrderService(orderDao, nutritionService);
    private final AuthService authService = new AuthService(new UserDao());

    public static void main(String[] args) {
        new App().run();
    }

    private void run() {
        printHeader();
        if (!databaseIsReady()) {
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("1) واجهة الزبون");
            System.out.println("2) واجهة الشيف");
            System.out.println("3) واجهة مدير النظام");
            System.out.println("0) خروج");
            int choice = input.readInt("اختر: ", 0, 3);

            try {
                if (choice == 1) {
                    customerMenu();
                } else if (choice == 2) {
                    chefMenu();
                } else if (choice == 3) {
                    adminMenu();
                } else {
                    System.out.println("تم إغلاق النظام.");
                    return;
                }
            } catch (SQLException e) {
                System.out.println("حدث خطأ في قاعدة البيانات: " + e.getMessage());
            }
        }
    }

    private void printHeader() {
        System.out.println("===============================================");
        System.out.println("      Healthy Restaurant Orders System");
        System.out.println("        منظومة حجز طلبات مطعم صحي");
        System.out.println("===============================================");
    }

    private boolean databaseIsReady() {
        try (Connection ignored = Database.getConnection()) {
            return true;
        } catch (SQLException e) {
            System.out.println("تعذر الاتصال بقاعدة البيانات.");
            System.out.println("تأكد من تشغيل MySQL ثم نفذ:");
            System.out.println("mysql -u root -p < database/schema.sql");
            System.out.println("mysql -u root -p < database/seed.sql");
            System.out.println("ثم عدل config/db.properties إذا كانت كلمة مرور MySQL مختلفة.");
            System.out.println("تفاصيل الخطأ: " + e.getMessage());
            return false;
        }
    }

    private void customerMenu() throws SQLException {
        while (true) {
            System.out.println();
            System.out.println("واجهة الزبون");
            System.out.println("1) تصفح الوجبات الجاهزة");
            System.out.println("2) إنشاء وجبة مخصصة");
            System.out.println("0) رجوع");
            int choice = input.readInt("اختر: ", 0, 2);

            if (choice == 1) {
                placeReadyMealOrder();
            } else if (choice == 2) {
                buildCustomMeal();
            } else {
                return;
            }
        }
    }

    private void placeReadyMealOrder() throws SQLException {
        List<ReadyMeal> meals = readyMealDao.findAllAvailable();
        if (meals.isEmpty()) {
            System.out.println("لا توجد وجبات جاهزة متاحة حاليا.");
            return;
        }

        printReadyMeals(meals);
        int mealId = input.readInt("أدخل رقم الوجبة أو 0 للرجوع: ", 0, Integer.MAX_VALUE);
        if (mealId == 0) {
            return;
        }

        Optional<ReadyMeal> selected = meals.stream()
                .filter(meal -> meal.getId() == mealId)
                .findFirst();
        if (!selected.isPresent()) {
            System.out.println("رقم الوجبة غير موجود في القائمة.");
            return;
        }

        int tableNumber = input.readInt("رقم الطاولة: ", 1, 999);
        int orderId = orderService.placeReadyMealOrder(tableNumber, selected.get());
        System.out.println("تم إرسال الطلب رقم #" + orderId + " إلى المطبخ.");
    }

    private void buildCustomMeal() throws SQLException {
        CustomerProfile profile = readCustomerProfile();
        double mealTarget = nutritionService.estimateMealTarget(profile);
        System.out.println("هدف الوجبة التقريبي: " + nutritionService.format(mealTarget) + " سعرة.");

        List<Ingredient> availableIngredients = ingredientDao.findAllAvailable();
        if (availableIngredients.isEmpty()) {
            System.out.println("لا توجد مكونات متاحة حاليا.");
            return;
        }

        List<Ingredient> selectedIngredients = new ArrayList<>();
        MealSummary summary = new MealSummary();

        while (true) {
            printMealSummary(summary);
            IngredientCategory category = chooseCategory(true);
            if (category == null) {
                break;
            }

            List<Ingredient> categoryIngredients = availableIngredients.stream()
                    .filter(ingredient -> ingredient.getCategory() == category)
                    .collect(Collectors.toList());
            if (categoryIngredients.isEmpty()) {
                System.out.println("لا توجد مكونات متاحة في هذه الفئة.");
                continue;
            }

            printIngredients(categoryIngredients);
            int ingredientId = input.readInt("أدخل رقم المكون أو 0 للرجوع للفئات: ", 0, Integer.MAX_VALUE);
            if (ingredientId == 0) {
                continue;
            }

            Optional<Ingredient> selected = categoryIngredients.stream()
                    .filter(ingredient -> ingredient.getId() == ingredientId)
                    .findFirst();
            if (!selected.isPresent()) {
                System.out.println("رقم المكون غير موجود في هذه الفئة.");
                continue;
            }

            Ingredient ingredient = selected.get();
            boolean alreadySelected = selectedIngredients.stream().anyMatch(item -> item.getId() == ingredient.getId());
            if (alreadySelected) {
                System.out.println("هذا المكون مضاف مسبقا.");
                continue;
            }

            List<String> errors = nutritionService.validateIngredient(ingredient, profile);
            if (!errors.isEmpty()) {
                System.out.println("تم منع المكون حفاظا على سلامتك:");
                errors.forEach(error -> System.out.println("- " + error));
                continue;
            }

            selectedIngredients.add(ingredient);
            summary.addIngredient(ingredient);
            System.out.println("تمت إضافة: " + ingredient.getName());
            nutritionService.buildSmartFeedback(profile, summary)
                    .forEach(message -> System.out.println("تنبيه: " + message));
        }

        if (selectedIngredients.isEmpty()) {
            System.out.println("لم يتم اختيار أي مكونات.");
            return;
        }

        printMealSummary(summary);
        if (!input.readYesNo("تأكيد وإرسال الطلب؟")) {
            System.out.println("تم إلغاء الطلب.");
            return;
        }

        int orderId = orderService.placeCustomMealOrder(profile, selectedIngredients, summary);
        System.out.println("تم إرسال الوجبة المخصصة رقم #" + orderId + " إلى شاشة الشيف.");
    }

    private CustomerProfile readCustomerProfile() {
        int tableNumber = input.readInt("رقم الطاولة: ", 1, 999);
        int age = input.readInt("العمر: ", 5, 120);
        double weight = input.readDouble("الوزن بالكيلو: ", 20, 350);
        double height = input.readDouble("الطول بالسنتيمتر: ", 90, 250);
        Goal goal = chooseGoal();
        Set<HealthCondition> conditions = chooseHealthConditions();
        return new CustomerProfile(tableNumber, age, weight, height, goal, conditions);
    }

    private Goal chooseGoal() {
        Goal[] goals = Goal.values();
        System.out.println("الهدف الرياضي:");
        for (int i = 0; i < goals.length; i++) {
            System.out.println((i + 1) + ") " + goals[i].getDisplayName());
        }
        int choice = input.readInt("اختر الهدف: ", 1, goals.length);
        return goals[choice - 1];
    }

    private Set<HealthCondition> chooseHealthConditions() {
        HealthCondition[] conditions = HealthCondition.values();
        System.out.println("الحالات الصحية، اكتب الأرقام مفصولة بفاصلة أو 0 إذا لا يوجد:");
        for (int i = 0; i < conditions.length; i++) {
            System.out.println((i + 1) + ") " + conditions[i].getDisplayName());
        }

        Set<HealthCondition> selected = EnumSet.noneOf(HealthCondition.class);
        String raw = input.readText("اختيارك: ");
        if (raw.trim().equals("0") || raw.trim().isEmpty()) {
            return selected;
        }

        String[] parts = raw.split("[,،\\s]+");
        for (String part : parts) {
            try {
                int index = Integer.parseInt(part);
                if (index >= 1 && index <= conditions.length) {
                    selected.add(conditions[index - 1]);
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid tokens and keep the valid selections.
            }
        }
        return selected;
    }

    private IngredientCategory chooseCategory(boolean allowFinish) {
        IngredientCategory[] categories = IngredientCategory.values();
        System.out.println();
        System.out.println("اختر فئة المكون:");
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ") " + categories[i].getDisplayName());
        }
        if (allowFinish) {
            System.out.println("0) إنهاء اختيار المكونات");
        }

        int min = allowFinish ? 0 : 1;
        int choice = input.readInt("اختر: ", min, categories.length);
        if (allowFinish && choice == 0) {
            return null;
        }
        return categories[choice - 1];
    }

    private void chefMenu() throws SQLException {
        Optional<User> loggedIn = login(Role.CHEF);
        if (!loggedIn.isPresent()) {
            return;
        }

        System.out.println("مرحبا " + loggedIn.get().getFullName());
        while (true) {
            List<OrderTicket> orders = orderDao.findKitchenOrders();
            System.out.println();
            System.out.println("طلبات المطبخ المفتوحة:");
            if (orders.isEmpty()) {
                System.out.println("لا توجد طلبات مفتوحة.");
            } else {
                printTicketSummary(orders);
            }

            int orderId = input.readInt("أدخل رقم الطلب لعرضه أو 0 للرجوع: ", 0, Integer.MAX_VALUE);
            if (orderId == 0) {
                return;
            }

            Optional<OrderTicket> ticket = orderDao.findTicket(orderId);
            if (!ticket.isPresent()) {
                System.out.println("الطلب غير موجود.");
                continue;
            }

            printTicket(ticket.get());
            OrderStatus status = chooseOrderStatus();
            if (status != null) {
                orderDao.updateStatus(orderId, status);
                System.out.println("تم تحديث حالة الطلب إلى: " + status.getDisplayName());
            }
        }
    }

    private OrderStatus chooseOrderStatus() {
        System.out.println("تحديث حالة الطلب:");
        System.out.println("1) قيد التحضير");
        System.out.println("2) جاهز");
        System.out.println("3) مكتمل");
        System.out.println("4) ملغي");
        System.out.println("0) بدون تغيير");
        int choice = input.readInt("اختر: ", 0, 4);
        if (choice == 1) {
            return OrderStatus.PREPARING;
        }
        if (choice == 2) {
            return OrderStatus.READY;
        }
        if (choice == 3) {
            return OrderStatus.COMPLETED;
        }
        if (choice == 4) {
            return OrderStatus.CANCELLED;
        }
        return null;
    }

    private void adminMenu() throws SQLException {
        Optional<User> loggedIn = login(Role.ADMIN);
        if (!loggedIn.isPresent()) {
            return;
        }

        System.out.println("مرحبا " + loggedIn.get().getFullName());
        while (true) {
            System.out.println();
            System.out.println("لوحة مدير النظام");
            System.out.println("1) عرض المكونات");
            System.out.println("2) إضافة مكون جديد");
            System.out.println("3) تفعيل/إيقاف مكون");
            System.out.println("4) عرض آخر الطلبات");
            System.out.println("0) رجوع");
            int choice = input.readInt("اختر: ", 0, 4);

            if (choice == 1) {
                printIngredients(ingredientDao.findAll());
            } else if (choice == 2) {
                addIngredient();
            } else if (choice == 3) {
                toggleIngredient();
            } else if (choice == 4) {
                showRecentOrders();
            } else {
                return;
            }
        }
    }

    private Optional<User> login(Role role) throws SQLException {
        System.out.println("تسجيل دخول " + role.getDisplayName());
        String username = input.readText("اسم المستخدم: ");
        String password = input.readText("كلمة المرور: ");
        Optional<User> user = authService.login(username, password, role);
        if (!user.isPresent()) {
            System.out.println("بيانات الدخول غير صحيحة أو الصلاحية غير كافية.");
        }
        return user;
    }

    private void addIngredient() throws SQLException {
        String name = input.readText("اسم المكون: ");
        IngredientCategory category = chooseCategory(false);
        String serving = input.readText("وصف الحصة (مثال 150 جرام): ");
        BigDecimal price = input.readMoney("السعر: ");
        double calories = input.readDouble("السعرات: ", 0, 5000);
        double protein = input.readDouble("البروتين بالجرام: ", 0, 500);
        double carbs = input.readDouble("الكربوهيدرات بالجرام: ", 0, 500);
        double fat = input.readDouble("الدهون بالجرام: ", 0, 500);
        boolean lactose = input.readYesNo("هل يحتوي على لاكتوز؟");
        boolean gluten = input.readYesNo("هل يحتوي على غلوتين؟");
        boolean sugar = input.readYesNo("هل هو عالي السكر؟");
        boolean sodium = input.readYesNo("هل هو عالي الصوديوم؟");
        boolean highFat = input.readYesNo("هل هو عالي الدهون؟");

        Ingredient ingredient = new Ingredient(
                0,
                name,
                category,
                serving,
                price,
                calories,
                protein,
                carbs,
                fat,
                lactose,
                gluten,
                sugar,
                sodium,
                highFat,
                true);

        int id = ingredientDao.insert(ingredient);
        System.out.println("تمت إضافة المكون برقم #" + id + ".");
    }

    private void toggleIngredient() throws SQLException {
        List<Ingredient> ingredients = ingredientDao.findAll();
        printIngredients(ingredients);
        int id = input.readInt("رقم المكون أو 0 للرجوع: ", 0, Integer.MAX_VALUE);
        if (id == 0) {
            return;
        }

        Optional<Ingredient> ingredient = ingredientDao.findById(id);
        if (!ingredient.isPresent()) {
            System.out.println("المكون غير موجود.");
            return;
        }

        boolean newStatus = !ingredient.get().isAvailable();
        ingredientDao.updateAvailability(id, newStatus);
        System.out.println("تم تحديث حالة المكون إلى: " + (newStatus ? "متاح" : "موقوف"));
    }

    private void showRecentOrders() throws SQLException {
        List<OrderTicket> tickets = orderDao.findRecentOrders(20);
        if (tickets.isEmpty()) {
            System.out.println("لا توجد طلبات.");
            return;
        }

        printTicketSummary(tickets);
        int id = input.readInt("أدخل رقم طلب لعرض التفاصيل أو 0 للرجوع: ", 0, Integer.MAX_VALUE);
        if (id == 0) {
            return;
        }

        Optional<OrderTicket> ticket = orderDao.findTicket(id);
        if (ticket.isPresent()) {
            printTicket(ticket.get());
        } else {
            System.out.println("الطلب غير موجود.");
        }
    }

    private void printReadyMeals(List<ReadyMeal> meals) {
        System.out.println();
        System.out.println("الوجبات الجاهزة:");
        for (ReadyMeal meal : meals) {
            System.out.printf(
                    Locale.US,
                    "#%d | %s | %.1f سعرة | P %.1fg / C %.1fg / F %.1fg | %s د.ل%n",
                    meal.getId(),
                    meal.getName(),
                    meal.getCalories(),
                    meal.getProteinGrams(),
                    meal.getCarbsGrams(),
                    meal.getFatGrams(),
                    meal.getPrice().toPlainString());
            System.out.println("   " + meal.getDescription());
        }
    }

    private void printIngredients(List<Ingredient> ingredients) {
        System.out.println();
        System.out.println("المكونات:");
        for (Ingredient ingredient : ingredients) {
            System.out.printf(
                    Locale.US,
                    "#%d | %s | %s | %s | %.1f سعرة | P %.1fg / C %.1fg / F %.1fg | %s د.ل | %s%n",
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getCategory().getDisplayName(),
                    ingredient.getServingLabel(),
                    ingredient.getCalories(),
                    ingredient.getProteinGrams(),
                    ingredient.getCarbsGrams(),
                    ingredient.getFatGrams(),
                    ingredient.getPrice().toPlainString(),
                    ingredient.isAvailable() ? "متاح" : "موقوف");
        }
    }

    private void printMealSummary(MealSummary summary) {
        System.out.println();
        System.out.println("ملخص الوجبة الحالي:");
        System.out.printf(
                Locale.US,
                "السعر: %s د.ل | السعرات: %.1f | بروتين: %.1fg | كربوهيدرات: %.1fg | دهون: %.1fg%n",
                summary.getPrice().toPlainString(),
                summary.getCalories(),
                summary.getProteinGrams(),
                summary.getCarbsGrams(),
                summary.getFatGrams());
    }

    private void printTicketSummary(List<OrderTicket> tickets) {
        for (OrderTicket ticket : tickets) {
            System.out.printf(
                    Locale.US,
                    "#%d | طاولة %d | %s | %s | %.1f سعرة | %s د.ل%n",
                    ticket.getId(),
                    ticket.getTableNumber(),
                    ticket.getOrderType().getDisplayName(),
                    ticket.getStatus().getDisplayName(),
                    ticket.getCalories(),
                    ticket.getSubtotal().toPlainString());
        }
    }

    private void printTicket(OrderTicket ticket) {
        System.out.println();
        System.out.println("تفاصيل الطلب #" + ticket.getId());
        System.out.println("الطاولة: " + ticket.getTableNumber());
        System.out.println("النوع: " + ticket.getOrderType().getDisplayName());
        System.out.println("الحالة: " + ticket.getStatus().getDisplayName());
        System.out.println("الإجمالي: " + ticket.getSubtotal().toPlainString() + " د.ل");
        System.out.printf(
                Locale.US,
                "القيم الغذائية: %.1f سعرة | P %.1fg / C %.1fg / F %.1fg%n",
                ticket.getCalories(),
                ticket.getProteinGrams(),
                ticket.getCarbsGrams(),
                ticket.getFatGrams());

        System.out.println("العناصر:");
        ticket.getLines().forEach(System.out::println);
        if (ticket.getHealthNotes() != null && !ticket.getHealthNotes().trim().isEmpty()) {
            System.out.println("ملاحظات صحية:");
            System.out.println(ticket.getHealthNotes());
        }
    }
}
