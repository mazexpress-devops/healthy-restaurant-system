import express from "express";
import { MongoClient, ObjectId } from "mongodb";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const port = Number(process.env.PORT || 3000);
const mongoUri = process.env.MONGO_URI || "mongodb://127.0.0.1:27017";
const dbName = process.env.MONGO_DB || "healthy_restaurant";

const categories = {
  PROTEIN: "بروتين",
  CARB: "نشويات",
  FAT: "دهون صحية",
  ADDON: "إضافات",
  SAUCE: "صلصات"
};

const conditions = {
  DIABETES: "سكري",
  LACTOSE_INTOLERANCE: "حساسية لاكتوز",
  GLUTEN_INTOLERANCE: "حساسية غلوتين",
  HYPERTENSION: "ضغط",
  FATTY_LIVER: "دهون على الكبد"
};

const goalLabels = {
  MAINTAIN: "تثبيت الوزن",
  CLEAN_BULK: "زيادة نظيفة",
  BULK: "زيادة عشوائية",
  CUT: "تنشيف"
};

const goalAdjustments = {
  MAINTAIN: 0,
  CLEAN_BULK: 250,
  BULK: 450,
  CUT: -400
};

const seedIngredients = [
  { name: "صدر دجاج مشوي", category: "PROTEIN", servingLabel: "150 جرام", price: 18, calories: 248, protein: 46, carbs: 0, fat: 5, flags: {} },
  { name: "سلمون مشوي", category: "PROTEIN", servingLabel: "150 جرام", price: 28, calories: 310, protein: 34, carbs: 0, fat: 18, flags: { highFat: true } },
  { name: "توفو نباتي", category: "PROTEIN", servingLabel: "120 جرام", price: 14, calories: 145, protein: 16, carbs: 4, fat: 8, flags: {} },
  { name: "أرز بني", category: "CARB", servingLabel: "150 جرام", price: 7, calories: 165, protein: 4, carbs: 35, fat: 1, flags: {} },
  { name: "كينوا", category: "CARB", servingLabel: "150 جرام", price: 10, calories: 180, protein: 6, carbs: 32, fat: 3, flags: {} },
  { name: "خبز شوفان", category: "CARB", servingLabel: "قطعتان", price: 6, calories: 160, protein: 6, carbs: 28, fat: 3, flags: { containsGluten: true } },
  { name: "بطاطا حلوة", category: "CARB", servingLabel: "150 جرام", price: 6, calories: 135, protein: 2, carbs: 31, fat: 0, flags: {} },
  { name: "زيت زيتون", category: "FAT", servingLabel: "ملعقة كبيرة", price: 4, calories: 119, protein: 0, carbs: 0, fat: 14, flags: { highFat: true } },
  { name: "أفوكادو", category: "FAT", servingLabel: "نصف حبة", price: 9, calories: 160, protein: 2, carbs: 9, fat: 15, flags: { highFat: true } },
  { name: "جبن قليل الدسم", category: "ADDON", servingLabel: "40 جرام", price: 5, calories: 90, protein: 8, carbs: 2, fat: 5, flags: { containsLactose: true, highSodium: true } },
  { name: "خضار مطهوة بالبخار", category: "ADDON", servingLabel: "كوب", price: 5, calories: 55, protein: 3, carbs: 11, fat: 0, flags: {} },
  { name: "ذرة حلوة", category: "ADDON", servingLabel: "نصف كوب", price: 4, calories: 75, protein: 2, carbs: 17, fat: 1, flags: { highSugar: true } },
  { name: "صلصة زبادي", category: "SAUCE", servingLabel: "30 جرام", price: 3, calories: 45, protein: 3, carbs: 4, fat: 2, flags: { containsLactose: true } },
  { name: "صلصة صويا قليلة الملح", category: "SAUCE", servingLabel: "20 مل", price: 3, calories: 15, protein: 1, carbs: 2, fat: 0, flags: { highSodium: true } }
];

const extraIngredients = [
  { name: "لحم بقري قليل الدهن", category: "PROTEIN", servingLabel: "140 جرام", price: 24, calories: 270, protein: 38, carbs: 0, fat: 12, flags: {} },
  { name: "بيض مسلوق", category: "PROTEIN", servingLabel: "بيضتان", price: 6, calories: 156, protein: 13, carbs: 1, fat: 11, flags: { highFat: true } },
  { name: "عدس مطهو", category: "PROTEIN", servingLabel: "كوب", price: 7, calories: 230, protein: 18, carbs: 40, fat: 1, flags: {} },
  { name: "مكرونة قمح كامل", category: "CARB", servingLabel: "150 جرام", price: 8, calories: 190, protein: 7, carbs: 39, fat: 2, flags: { containsGluten: true } },
  { name: "سلطة خضراء", category: "ADDON", servingLabel: "طبق صغير", price: 5, calories: 35, protein: 2, carbs: 7, fat: 0, flags: {} },
  { name: "مكسرات مشكلة", category: "FAT", servingLabel: "30 جرام", price: 8, calories: 180, protein: 6, carbs: 6, fat: 16, flags: { highFat: true } },
  { name: "صلصة ليمون وأعشاب", category: "SAUCE", servingLabel: "30 مل", price: 3, calories: 25, protein: 0, carbs: 2, fat: 1, flags: {} },
  { name: "عسل طبيعي", category: "SAUCE", servingLabel: "ملعقة صغيرة", price: 3, calories: 64, protein: 0, carbs: 17, fat: 0, flags: { highSugar: true } }
];

const seedMeals = [
  { name: "طبق دجاج وكينوا", description: "صدر دجاج مشوي مع كينوا وخضار مطهوة بالبخار.", price: 32, ingredientNames: ["صدر دجاج مشوي", "كينوا", "خضار مطهوة بالبخار"] },
  { name: "طبق سلمون وأرز بني", description: "سلمون مشوي مع أرز بني وخضار.", price: 40, ingredientNames: ["سلمون مشوي", "أرز بني", "خضار مطهوة بالبخار"] },
  { name: "طبق نباتي صحي", description: "توفو نباتي مع بطاطا حلوة وخضار.", price: 27, ingredientNames: ["توفو نباتي", "بطاطا حلوة", "خضار مطهوة بالبخار"] }
];

const extraMeals = [
  { name: "طبق تنشيف عالي البروتين", description: "صدر دجاج مشوي مع سلطة خضراء وصلصة ليمون وأعشاب.", price: 30, ingredientNames: ["صدر دجاج مشوي", "سلطة خضراء", "صلصة ليمون وأعشاب"] },
  { name: "باول زيادة نظيفة", description: "لحم بقري قليل الدهن مع أرز بني وأفوكادو.", price: 42, ingredientNames: ["لحم بقري قليل الدهن", "أرز بني", "أفوكادو"] },
  { name: "طبق نباتي غني بالبروتين", description: "عدس مطهو مع كينوا وسلطة خضراء.", price: 25, ingredientNames: ["عدس مطهو", "كينوا", "سلطة خضراء"] }
];

const client = new MongoClient(mongoUri);
await client.connect();
const db = client.db(dbName);
const ingredientsCol = db.collection("ingredients");
const mealsCol = db.collection("ready_meals");
const ordersCol = db.collection("orders");
const countersCol = db.collection("counters");
const usersCol = db.collection("users");

const seedUsers = [
  { username: "admin", fullName: "مدير النظام", role: "ADMIN", active: true },
  { username: "chef", fullName: "شيف المطبخ", role: "CHEF", active: true }
];

await seedDatabase();

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

app.get("/api/bootstrap", async (_req, res) => {
  res.json({
    categories,
    conditions,
    goals: goalLabels,
    ingredients: await listIngredients(false),
    meals: await listMeals(),
    orders: await listOrders(true),
    allOrders: await listOrders(false, 80),
    staff: await listStaff(),
    stats: await buildStats()
  });
});

app.post("/api/orders/ready", async (req, res) => {
  const tableNumber = Number(req.body.tableNumber);
  const meal = await mealsCol.findOne({ _id: oid(req.body.mealId), available: true });
  const profile = normalizeProfile({ ...(req.body.profile || {}), tableNumber });
  if (!tableNumber || tableNumber < 1 || !meal) {
    return res.status(400).json({ error: "بيانات الطلب غير صحيحة." });
  }

  const selectedIngredients = await ingredientsCol.find({ _id: { $in: meal.ingredientIds || [] } }).toArray();
  const blocked = selectedIngredients.flatMap((ingredient) => validateIngredient(ingredient, profile));
  if (blocked.length > 0) {
    return res.status(400).json({
      error: "هذه الوجبة تحتوي على مكونات ممنوعة للحالة الصحية المحددة.",
      blocked
    });
  }

  const summary = summarize(selectedIngredients, meal.price);
  const order = await createOrder({
    tableNumber,
    orderType: "READY_MEAL",
    status: "NEW",
    subtotal: meal.price,
    calories: summary.calories,
    protein: summary.protein,
    carbs: summary.carbs,
    fat: summary.fat,
    healthNotes: "طلب وجبة جاهزة من قائمة الطعام.",
    lines: [{ name: meal.name, ingredients: selectedIngredients.map(toLineIngredient) }]
  });

  res.json({ ok: true, order });
});

app.post("/api/orders/custom", async (req, res) => {
  const profile = normalizeProfile(req.body.profile || {});
  const ingredientIds = (req.body.ingredientIds || []).map(oid).filter(Boolean);
  const selectedIngredients = await ingredientsCol.find({ _id: { $in: ingredientIds }, available: true }).toArray();
  if (!profile.tableNumber || selectedIngredients.length === 0) {
    return res.status(400).json({ error: "أدخل بيانات الزبون واختر مكونا واحدا على الأقل." });
  }

  const blocked = selectedIngredients.flatMap((ingredient) => validateIngredient(ingredient, profile));
  if (blocked.length > 0) {
    return res.status(400).json({ error: "يوجد مكون غير مناسب صحيا.", blocked });
  }

  const summary = summarize(selectedIngredients);
  const feedback = buildFeedback(profile, summary);
  const order = await createOrder({
    tableNumber: profile.tableNumber,
    orderType: "CUSTOM_MEAL",
    status: "NEW",
    subtotal: summary.price,
    calories: summary.calories,
    protein: summary.protein,
    carbs: summary.carbs,
    fat: summary.fat,
    profile,
    healthNotes: buildHealthNotes(profile, selectedIngredients, summary, feedback),
    lines: [{ name: "وجبة مخصصة", ingredients: selectedIngredients.map(toLineIngredient) }]
  });

  res.json({ ok: true, order, feedback });
});

app.patch("/api/orders/:id/status", async (req, res) => {
  const status = String(req.body.status || "").toUpperCase();
  if (!["NEW", "PREPARING", "READY", "COMPLETED", "CANCELLED"].includes(status)) {
    return res.status(400).json({ error: "حالة الطلب غير صحيحة." });
  }
  const result = await ordersCol.findOneAndUpdate(
    { _id: oid(req.params.id) },
    { $set: { status, updatedAt: new Date() } },
    { returnDocument: "after" }
  );
  const order = result?.value || result;
  if (!order) {
    return res.status(404).json({ error: "الطلب غير موجود." });
  }
  res.json({ ok: true, order });
});

app.post("/api/ingredients", async (req, res) => {
  const ingredient = normalizeIngredient(req.body);
  if (!ingredient.name || !categories[ingredient.category]) {
    return res.status(400).json({ error: "بيانات المكون غير صحيحة." });
  }
  await ingredientsCol.updateOne({ name: ingredient.name }, { $set: ingredient }, { upsert: true });
  res.json({ ok: true, ingredients: await listIngredients(false) });
});

app.patch("/api/ingredients/:id/availability", async (req, res) => {
  await ingredientsCol.updateOne(
    { _id: oid(req.params.id) },
    { $set: { available: Boolean(req.body.available), updatedAt: new Date() } }
  );
  res.json({ ok: true, ingredients: await listIngredients(false) });
});

app.post("/api/meals", async (req, res) => {
  const ingredientIds = (req.body.ingredientIds || []).map(oid).filter(Boolean);
  const selectedIngredients = await ingredientsCol.find({ _id: { $in: ingredientIds } }).toArray();
  const name = String(req.body.name || "").trim();
  if (!name || selectedIngredients.length === 0) {
    return res.status(400).json({ error: "أدخل اسم الوجبة واختر مكونا واحدا على الأقل." });
  }

  const summary = summarize(selectedIngredients, req.body.price ? Number(req.body.price) : null);
  await mealsCol.updateOne(
    { name },
    {
      $set: {
        name,
        description: String(req.body.description || "").trim(),
        price: summary.price,
        calories: summary.calories,
        protein: summary.protein,
        carbs: summary.carbs,
        fat: summary.fat,
        ingredientIds: selectedIngredients.map((item) => item._id),
        available: req.body.available !== false,
        updatedAt: new Date()
      },
      $setOnInsert: { createdAt: new Date() }
    },
    { upsert: true }
  );

  res.json({ ok: true, meals: await listMeals(false) });
});

app.patch("/api/meals/:id/availability", async (req, res) => {
  await mealsCol.updateOne(
    { _id: oid(req.params.id) },
    { $set: { available: Boolean(req.body.available), updatedAt: new Date() } }
  );
  res.json({ ok: true, meals: await listMeals(false) });
});

app.post("/api/staff", async (req, res) => {
  const username = String(req.body.username || "").trim();
  const fullName = String(req.body.fullName || "").trim();
  const role = String(req.body.role || "CHEF").toUpperCase();
  if (!username || !fullName || !["ADMIN", "CHEF"].includes(role)) {
    return res.status(400).json({ error: "بيانات حساب الطاقم غير صحيحة." });
  }

  await usersCol.updateOne(
    { username },
    {
      $set: { username, fullName, role, active: req.body.active !== false, updatedAt: new Date() },
      $setOnInsert: { createdAt: new Date() }
    },
    { upsert: true }
  );

  res.json({ ok: true, staff: await listStaff() });
});

app.patch("/api/staff/:id/availability", async (req, res) => {
  await usersCol.updateOne(
    { _id: oid(req.params.id) },
    { $set: { active: Boolean(req.body.active), updatedAt: new Date() } }
  );
  res.json({ ok: true, staff: await listStaff() });
});

app.get("/api/orders", async (_req, res) => {
  res.json({ orders: await listOrders(true) });
});

app.get("/api/orders/all", async (_req, res) => {
  res.json({ orders: await listOrders(false, 100), stats: await buildStats() });
});

app.get("/api/tables/:tableNumber/orders", async (req, res) => {
  const tableNumber = Number(req.params.tableNumber);
  const orders = await ordersCol
    .find({ tableNumber, status: { $in: ["NEW", "PREPARING", "READY"] } })
    .sort({ createdAt: -1 })
    .limit(20)
    .toArray();
  res.json({ orders });
});

app.listen(port, "127.0.0.1", () => {
  console.log(`Healthy Restaurant Mongo runner: http://127.0.0.1:${port}`);
  console.log(`MongoDB: ${mongoUri}/${dbName}`);
});

async function seedDatabase() {
  await ingredientsCol.createIndex({ name: 1 }, { unique: true });
  await mealsCol.createIndex({ name: 1 }, { unique: true });
  await ordersCol.createIndex({ orderNo: -1 });
  await usersCol.createIndex({ username: 1 }, { unique: true });

  for (const raw of [...seedIngredients, ...extraIngredients]) {
    await ingredientsCol.updateOne(
      { name: raw.name },
      { $setOnInsert: normalizeIngredient(raw) },
      { upsert: true }
    );
  }

  for (const meal of [...seedMeals, ...extraMeals]) {
    const ingredients = await ingredientsCol.find({ name: { $in: meal.ingredientNames } }).toArray();
    const summary = summarize(ingredients, meal.price);
    await mealsCol.updateOne(
      { name: meal.name },
      {
        $set: {
          name: meal.name,
          description: meal.description,
          price: meal.price,
          calories: summary.calories,
          protein: summary.protein,
          carbs: summary.carbs,
          fat: summary.fat,
          ingredientIds: ingredients.map((item) => item._id),
          available: true,
          updatedAt: new Date()
        },
        $setOnInsert: { createdAt: new Date() }
      },
      { upsert: true }
    );
  }

  for (const user of seedUsers) {
    await usersCol.updateOne(
      { username: user.username },
      { $setOnInsert: { ...user, createdAt: new Date(), updatedAt: new Date() } },
      { upsert: true }
    );
  }

  const latestOrder = await ordersCol.find({ orderNo: { $type: "number" } }).sort({ orderNo: -1 }).limit(1).next();
  const orderCount = await ordersCol.countDocuments({});
  const safeCounter = Math.max(latestOrder?.orderNo || 0, orderCount);
  if (safeCounter > 0) {
    await countersCol.updateOne(
      { _id: "orders" },
      { $max: { value: safeCounter } },
      { upsert: true }
    );
  }
}

async function createOrder(order) {
  const orderNo = await nextOrderNo();
  const doc = { ...order, orderNo, createdAt: new Date(), updatedAt: new Date() };
  await ordersCol.insertOne(doc);
  return doc;
}

async function nextOrderNo() {
  await countersCol.updateOne(
    { _id: "orders" },
    { $inc: { value: 1 } },
    { upsert: true }
  );
  const counter = await countersCol.findOne({ _id: "orders" });
  return counter?.value || 1;
}

async function listIngredients(availableOnly = true) {
  const filter = availableOnly ? { available: true } : {};
  return ingredientsCol.find(filter).sort({ category: 1, name: 1 }).toArray();
}

async function listMeals(availableOnly = true) {
  const filter = availableOnly ? { available: true } : {};
  return mealsCol.find(filter).sort({ name: 1 }).toArray();
}

async function listOrders(openOnly = true, limit = 50) {
  const filter = openOnly ? { status: { $in: ["NEW", "PREPARING", "READY"] } } : {};
  return ordersCol.find(filter).sort({ createdAt: -1 }).limit(limit).toArray();
}

async function listStaff() {
  return usersCol.find({}).sort({ role: 1, fullName: 1 }).toArray();
}

async function buildStats() {
  const [summary] = await ordersCol.aggregate([
    {
      $group: {
        _id: null,
        totalOrders: { $sum: 1 },
        openOrders: {
          $sum: { $cond: [{ $in: ["$status", ["NEW", "PREPARING", "READY"]] }, 1, 0] }
        },
        completedOrders: { $sum: { $cond: [{ $eq: ["$status", "COMPLETED"] }, 1, 0] } },
        cancelledOrders: { $sum: { $cond: [{ $eq: ["$status", "CANCELLED"] }, 1, 0] } },
        revenue: {
          $sum: { $cond: [{ $ne: ["$status", "CANCELLED"] }, "$subtotal", 0] }
        },
        averageCalories: { $avg: "$calories" }
      }
    }
  ]).toArray();

  return {
    totalOrders: summary?.totalOrders || 0,
    openOrders: summary?.openOrders || 0,
    completedOrders: summary?.completedOrders || 0,
    cancelledOrders: summary?.cancelledOrders || 0,
    revenue: round(summary?.revenue || 0),
    averageCalories: round(summary?.averageCalories || 0),
    ingredients: await ingredientsCol.countDocuments({}),
    readyMeals: await mealsCol.countDocuments({})
  };
}

function normalizeIngredient(raw) {
  return {
    name: String(raw.name || "").trim(),
    category: String(raw.category || "ADDON").toUpperCase(),
    servingLabel: String(raw.servingLabel || "حصة واحدة").trim(),
    price: Number(raw.price || 0),
    calories: Number(raw.calories || 0),
    protein: Number(raw.protein || 0),
    carbs: Number(raw.carbs || 0),
    fat: Number(raw.fat || 0),
    flags: {
      containsLactose: Boolean(raw.flags?.containsLactose || raw.containsLactose),
      containsGluten: Boolean(raw.flags?.containsGluten || raw.containsGluten),
      highSugar: Boolean(raw.flags?.highSugar || raw.highSugar),
      highSodium: Boolean(raw.flags?.highSodium || raw.highSodium),
      highFat: Boolean(raw.flags?.highFat || raw.highFat)
    },
    available: raw.available !== false,
    updatedAt: new Date(),
    createdAt: raw.createdAt || new Date()
  };
}

function normalizeProfile(raw) {
  return {
    tableNumber: Number(raw.tableNumber || 0),
    age: Number(raw.age || 25),
    weightKg: Number(raw.weightKg || 70),
    heightCm: Number(raw.heightCm || 170),
    goal: goalLabels[raw.goal] ? raw.goal : "MAINTAIN",
    conditions: Array.isArray(raw.conditions) ? raw.conditions.filter((item) => conditions[item]) : []
  };
}

function summarize(ingredients, fixedPrice = null) {
  return {
    price: fixedPrice ?? round(ingredients.reduce((sum, item) => sum + Number(item.price || 0), 0)),
    calories: round(ingredients.reduce((sum, item) => sum + Number(item.calories || 0), 0)),
    protein: round(ingredients.reduce((sum, item) => sum + Number(item.protein || 0), 0)),
    carbs: round(ingredients.reduce((sum, item) => sum + Number(item.carbs || 0), 0)),
    fat: round(ingredients.reduce((sum, item) => sum + Number(item.fat || 0), 0))
  };
}

function validateIngredient(ingredient, profile) {
  const selected = new Set(profile.conditions || []);
  const flags = ingredient.flags || {};
  const errors = [];
  if (selected.has("LACTOSE_INTOLERANCE") && flags.containsLactose) errors.push(`${ingredient.name}: يحتوي على لاكتوز.`);
  if (selected.has("GLUTEN_INTOLERANCE") && flags.containsGluten) errors.push(`${ingredient.name}: يحتوي على غلوتين.`);
  if (selected.has("DIABETES") && flags.highSugar) errors.push(`${ingredient.name}: عالي السكر.`);
  if (selected.has("HYPERTENSION") && flags.highSodium) errors.push(`${ingredient.name}: عالي الصوديوم.`);
  if (selected.has("FATTY_LIVER") && flags.highFat) errors.push(`${ingredient.name}: عالي الدهون.`);
  return errors;
}

function buildFeedback(profile, summary) {
  const target = estimateMealTarget(profile);
  const feedback = [];
  if (summary.calories > target * 1.15) feedback.push(`السعرات أعلى من هدف الوجبة بحوالي ${round(summary.calories - target)} سعرة.`);
  else if (summary.calories < target * 0.75) feedback.push("السعرات أقل من هدف الوجبة، يمكن إضافة بروتين أو نشويات مناسبة.");
  else feedback.push("السعرات مناسبة تقريبا لهدفك الحالي.");
  if (summary.protein < 20) feedback.push("البروتين منخفض نسبيا.");
  if ((profile.conditions || []).includes("DIABETES") && summary.carbs > 55) feedback.push("الكربوهيدرات مرتفعة نسبيا لحالة السكري.");
  if ((profile.conditions || []).includes("FATTY_LIVER") && summary.fat > 20) feedback.push("الدهون مرتفعة لحالة دهون الكبد.");
  return feedback;
}

function estimateMealTarget(profile) {
  const bmr = (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5;
  return Math.max(1200, (bmr * 1.35) + goalAdjustments[profile.goal]) / 3;
}

function buildHealthNotes(profile, ingredients, summary, feedback) {
  const conditionText = profile.conditions.length
    ? profile.conditions.map((item) => conditions[item]).join("، ")
    : "لا توجد حالات صحية مسجلة";
  return [
    `الهدف: ${goalLabels[profile.goal]}`,
    `الحالات الصحية: ${conditionText}`,
    `المكونات: ${ingredients.map((item) => item.name).join("، ")}`,
    `السعرات: ${summary.calories} | بروتين: ${summary.protein}g | كربوهيدرات: ${summary.carbs}g | دهون: ${summary.fat}g`,
    `التنبيهات: ${feedback.join(" | ")}`
  ].join("\n");
}

function toLineIngredient(item) {
  return {
    name: item.name,
    category: item.category,
    categoryLabel: categories[item.category],
    price: item.price,
    calories: item.calories
  };
}

function oid(value) {
  if (!value || !ObjectId.isValid(value)) return null;
  return new ObjectId(value);
}

function round(value) {
  return Math.round(Number(value || 0) * 10) / 10;
}
