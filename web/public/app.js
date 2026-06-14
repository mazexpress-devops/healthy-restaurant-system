let state = {
  categories: {},
  conditions: {},
  goals: {},
  ingredients: [],
  meals: [],
  orders: [],
  allOrders: [],
  staff: [],
  stats: {}
};

const $ = (id) => document.getElementById(id);

document.querySelectorAll(".tab").forEach((button) => {
  button.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((item) => item.classList.remove("active"));
    document.querySelectorAll(".panel").forEach((item) => item.classList.remove("active"));
    button.classList.add("active");
    $(button.dataset.tab).classList.add("active");
  });
});

$("refreshBtn").addEventListener("click", load);
$("customOrderBtn").addEventListener("click", placeCustomOrder);
$("addIngredientBtn").addEventListener("click", addIngredient);
$("addMealBtn").addEventListener("click", addMeal);
$("addStaffBtn").addEventListener("click", addStaff);
$("clearCustomBtn").addEventListener("click", clearCustomMeal);
$("readyTable").addEventListener("input", renderTableOrders);

["tableNumber", "age", "weightKg", "heightCm", "goal"].forEach((id) => {
  $(id).addEventListener("input", updateSummary);
});

load();

async function load() {
  const response = await fetch("/api/bootstrap");
  state = await response.json();
  $("connectionState").textContent = "متصل: MongoDB / healthy_restaurant";
  fillSelects();
  renderMeals();
  renderIngredients();
  renderOrders();
  renderAdmin();
  renderTableOrders();
  updateSummary();
}

function fillSelects() {
  if (!$("goal").dataset.ready) {
    $("goal").innerHTML = Object.entries(state.goals)
      .map(([value, label]) => `<option value="${value}">${label}</option>`)
      .join("");
    $("goal").dataset.ready = "true";
  }

  $("newCategory").innerHTML = Object.entries(state.categories)
    .map(([value, label]) => `<option value="${value}">${label}</option>`)
    .join("");

  if (!$("conditions").dataset.ready) {
    $("conditions").innerHTML = Object.entries(state.conditions)
      .map(([value, label]) => `<label><input type="checkbox" value="${value}" class="condition"> ${label}</label>`)
      .join("");
    document.querySelectorAll(".condition").forEach((item) => item.addEventListener("change", updateSummary));
    $("conditions").dataset.ready = "true";
  }
}

function renderMeals() {
  $("meals").innerHTML = state.meals.map((meal) => `
    <article class="card meal-card">
      <h3>${escapeHtml(meal.name)}</h3>
      <p class="meta">${escapeHtml(meal.description || "وجبة جاهزة من إدارة النظام.")}</p>
      <p class="meta">${meal.calories} سعرة | بروتين ${meal.protein}g | كارب ${meal.carbs}g | دهون ${meal.fat}g</p>
      <strong>${meal.price} د.ل</strong>
      ${renderMealOrderButton(meal)}
    </article>
  `).join("");
}

function renderIngredients() {
  const groups = groupBy(state.ingredients.filter((item) => item.available), "category");
  $("ingredients").innerHTML = Object.entries(groups).map(([category, items]) => `
    <article class="card">
      <h3>${state.categories[category] || category}</h3>
      ${items.map((item) => `
        <label class="ingredient-row">
          <input type="checkbox" class="ingredient" value="${item._id}" onchange="handleIngredientChange(this)">
          <span>
            <strong>${escapeHtml(item.name)}</strong>
            <span class="meta"> - ${escapeHtml(item.servingLabel)} - ${item.calories} سعرة - ${item.price} د.ل</span>
            <span class="flags">${flagText(item)}</span>
          </span>
        </label>
      `).join("")}
    </article>
  `).join("");

  $("mealIngredientList").innerHTML = state.ingredients
    .filter((item) => item.available)
    .map((item) => `
      <label class="compact-check">
        <input type="checkbox" class="mealIngredient" value="${item._id}">
        ${escapeHtml(item.name)} <span>${state.categories[item.category] || item.category}</span>
      </label>
    `).join("");
}

function renderOrders() {
  $("openCount").textContent = `${state.orders.length} طلب مفتوح`;
  if (!state.orders.length) {
    $("orders").innerHTML = `<p class="meta">لا توجد طلبات مفتوحة حاليا.</p>`;
    return;
  }
  $("orders").innerHTML = state.orders.map(renderOrderCard).join("");
}

function renderAdmin() {
  renderStats();
  renderAdminIngredients();
  renderAdminMeals();
  renderStaff();
  renderAdminOrders();
}

function renderStats() {
  const stats = state.stats || {};
  $("stats").innerHTML = `
    <article class="stat"><strong>${stats.totalOrders || 0}</strong><span>إجمالي الطلبات</span></article>
    <article class="stat"><strong>${stats.openOrders || 0}</strong><span>طلبات مفتوحة</span></article>
    <article class="stat"><strong>${stats.completedOrders || 0}</strong><span>طلبات مكتملة</span></article>
    <article class="stat"><strong>${stats.revenue || 0}</strong><span>إجمالي المبيعات د.ل</span></article>
    <article class="stat"><strong>${stats.averageCalories || 0}</strong><span>متوسط السعرات</span></article>
  `;
}

function renderAdminIngredients() {
  $("adminIngredients").innerHTML = state.ingredients.map((item) => `
    <article class="card small-card">
      <h3>${escapeHtml(item.name)}</h3>
      <p class="meta">${state.categories[item.category]} | ${item.calories} سعرة | ${item.price} د.ل | ${item.available ? "متاح" : "موقوف"}</p>
      <button onclick="toggleIngredient('${item._id}', ${!item.available})">${item.available ? "إيقاف" : "تفعيل"}</button>
    </article>
  `).join("");
}

function renderAdminMeals() {
  $("adminMeals").innerHTML = state.meals.map((meal) => `
    <article class="card small-card">
      <h3>${escapeHtml(meal.name)}</h3>
      <p class="meta">${meal.calories} سعرة | ${meal.price} د.ل | ${meal.available === false ? "موقوفة" : "متاحة"}</p>
      <button onclick="toggleMeal('${meal._id}', ${meal.available === false})">${meal.available === false ? "تفعيل" : "إيقاف"}</button>
    </article>
  `).join("");
}

function renderStaff() {
  $("staffList").innerHTML = state.staff.map((user) => `
    <article class="card small-card">
      <h3>${escapeHtml(user.fullName)}</h3>
      <p class="meta">${escapeHtml(user.username)} | ${user.role === "ADMIN" ? "مدير النظام" : "الشيف"} | ${user.active ? "نشط" : "موقوف"}</p>
      <button onclick="toggleStaff('${user._id}', ${!user.active})">${user.active ? "إيقاف" : "تفعيل"}</button>
    </article>
  `).join("");
}

function renderAdminOrders() {
  $("adminOrders").innerHTML = (state.allOrders || []).slice(0, 20).map(renderOrderCard).join("");
}

function renderOrderCard(order) {
  return `
    <article class="card order-card">
      <div class="order-head">
        <h3>#${order.orderNo} - طاولة ${order.tableNumber}</h3>
        <span class="badge">${statusLabel(order.status)}</span>
      </div>
      <p class="meta">${typeLabel(order.orderType)} | ${order.calories} سعرة | ${order.subtotal} د.ل</p>
      <div class="order-lines">
        ${(order.lines || []).map((line) => `
          <p><strong>${escapeHtml(line.name)}</strong></p>
          <ul>
            ${(line.ingredients || []).map((item) => `<li>${escapeHtml(item.name)} - ${item.categoryLabel || item.category} - ${item.calories} سعرة</li>`).join("")}
          </ul>
        `).join("")}
      </div>
      <pre class="meta">${escapeHtml(order.healthNotes || "")}</pre>
      <div class="actions">
        <button onclick="setStatus('${order._id}', 'PREPARING')">قيد التحضير</button>
        <button onclick="setStatus('${order._id}', 'READY')">جاهز</button>
        <button onclick="setStatus('${order._id}', 'COMPLETED')">مكتمل</button>
        <button onclick="printInvoice('${order._id}')">طباعة الفاتورة</button>
        <button class="danger" onclick="setStatus('${order._id}', 'CANCELLED')">إلغاء</button>
      </div>
    </article>
  `;
}

function renderTableOrders() {
  const tableNumber = Number($("readyTable").value || 1);
  const orders = (state.allOrders || [])
    .filter((order) => order.tableNumber === tableNumber && ["NEW", "PREPARING", "READY"].includes(order.status))
    .slice(0, 5);

  if (!orders.length) {
    $("tableOrders").innerHTML = `<span>لا توجد إشعارات للطاولة ${tableNumber}.</span>`;
    return;
  }

  $("tableOrders").innerHTML = orders.map((order) => `
    <span class="notice">طلب #${order.orderNo}: ${statusLabel(order.status)}${order.status === "READY" ? " - يمكن الاستلام الآن" : ""}</span>
  `).join("");
}

function updateSummary() {
  const profile = readProfile();
  enforceHealthBlocks(profile);
  renderMeals();
  const selected = selectedIngredients();
  const summary = summarize(selected);
  const target = estimateMealTarget(profile);
  const feedback = buildFeedback(profile, summary);
  const alerts = selected.flatMap((item) => validate(item, profile));

  $("targetBox").innerHTML = `
    الاحتياج اليومي التقريبي: ${round(target * 3)} سعرة<br>
    هدف الوجبة: ${round(target)} سعرة
  `;

  $("summary").innerHTML = `
    <strong>الحساب اللحظي</strong><br>
    السعر: ${round(summary.price)} د.ل<br>
    السعرات: ${round(summary.calories)} | بروتين: ${round(summary.protein)}g | كارب: ${round(summary.carbs)}g | دهون: ${round(summary.fat)}g
  `;

  const messages = [
    ...feedback.map((message) => ({ message, bad: false })),
    ...alerts.map((message) => ({ message, bad: true }))
  ];

  $("alerts").innerHTML = messages.length
    ? messages.map((item) => `<div class="alert ${item.bad ? "bad" : ""}">${item.message}</div>`).join("")
    : `<div class="alert">اختر المكونات لتظهر التنبيهات الذكية.</div>`;
}

async function placeReadyOrder(mealId) {
  const meal = state.meals.find((item) => item._id === mealId);
  const blocked = meal ? validateMeal(meal, readReadyProfile()) : [];
  if (blocked.length > 0) {
    toast("تم منع الطلب لأن الوجبة لا تناسب الحالة الصحية المحددة.", true);
    $("alerts").innerHTML = blocked.map((item) => `<div class="alert bad">${item}</div>`).join("");
    return;
  }

  await post("/api/orders/ready", {
    mealId,
    tableNumber: Number($("readyTable").value || 1),
    profile: readReadyProfile()
  }, "تم إرسال الوجبة الجاهزة للمطبخ.");
}

async function placeCustomOrder() {
  const blocked = selectedIngredients().flatMap((item) => validate(item, readProfile()));
  if (blocked.length > 0) {
    toast("تم منع الطلب بسبب مكونات مخالفة للحالة الصحية.", true);
    $("alerts").innerHTML = blocked.map((item) => `<div class="alert bad">${item}</div>`).join("");
    return;
  }

  const ingredientIds = selectedIngredients().map((item) => item._id);
  await post("/api/orders/custom", {
    profile: readProfile(),
    ingredientIds
  }, "تم إرسال الوجبة المخصصة للمطبخ.");
}

async function setStatus(id, status) {
  const response = await fetch(`/api/orders/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status })
  });
  if (!response.ok) {
    toast("تعذر تحديث حالة الطلب.", true);
    return;
  }
  toast("تم تحديث حالة الطلب.");
  await load();
}

async function addIngredient() {
  const body = {
    name: $("newName").value,
    category: $("newCategory").value,
    servingLabel: $("newServing").value,
    price: Number($("newPrice").value || 0),
    calories: Number($("newCalories").value || 0),
    protein: Number($("newProtein").value || 0),
    carbs: Number($("newCarbs").value || 0),
    fat: Number($("newFat").value || 0),
    flags: {
      containsLactose: $("flagLactose").checked,
      containsGluten: $("flagGluten").checked,
      highSugar: $("flagSugar").checked,
      highSodium: $("flagSodium").checked,
      highFat: $("flagFat").checked
    }
  };
  await post("/api/ingredients", body, "تم حفظ المكون.");
}

async function addMeal() {
  const ingredientIds = [...document.querySelectorAll(".mealIngredient:checked")].map((item) => item.value);
  await post("/api/meals", {
    name: $("mealName").value,
    description: $("mealDescription").value,
    price: $("mealPrice").value ? Number($("mealPrice").value) : null,
    ingredientIds
  }, "تم حفظ الوجبة الجاهزة.");
}

async function addStaff() {
  await post("/api/staff", {
    username: $("staffUsername").value,
    fullName: $("staffFullName").value,
    role: $("staffRole").value
  }, "تم حفظ حساب الطاقم.");
}

async function toggleIngredient(id, available) {
  await patch(`/api/ingredients/${id}/availability`, { available }, "تم تحديث حالة المكون.");
}

async function toggleMeal(id, available) {
  await patch(`/api/meals/${id}/availability`, { available }, "تم تحديث حالة الوجبة.");
}

async function toggleStaff(id, active) {
  await patch(`/api/staff/${id}/availability`, { active }, "تم تحديث حالة الحساب.");
}

async function post(url, body, successMessage) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  const payload = await response.json();
  if (!response.ok) {
    toast(payload.error || "حدث خطأ.", true);
    if (payload.blocked) {
      $("alerts").innerHTML = payload.blocked.map((item) => `<div class="alert bad">${item}</div>`).join("");
    }
    return;
  }
  toast(successMessage);
  await load();
}

async function patch(url, body, successMessage) {
  const response = await fetch(url, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    toast("حدث خطأ أثناء التحديث.", true);
    return;
  }
  toast(successMessage);
  await load();
}

function clearCustomMeal() {
  document.querySelectorAll(".ingredient:checked").forEach((item) => {
    item.checked = false;
  });
  updateSummary();
}

function readProfile() {
  return {
    tableNumber: Number($("tableNumber").value || 1),
    age: Number($("age").value || 25),
    weightKg: Number($("weightKg").value || 70),
    heightCm: Number($("heightCm").value || 170),
    goal: $("goal").value,
    conditions: [...document.querySelectorAll(".condition:checked")].map((item) => item.value)
  };
}

function selectedIngredients() {
  const ids = new Set([...document.querySelectorAll(".ingredient:checked")].map((item) => item.value));
  return state.ingredients.filter((item) => ids.has(item._id));
}

function readReadyProfile() {
  return {
    ...readProfile(),
    tableNumber: Number($("readyTable").value || 1)
  };
}

function handleIngredientChange(checkbox) {
  const ingredient = state.ingredients.find((item) => item._id === checkbox.value);
  const blocked = ingredient ? validate(ingredient, readProfile()) : [];
  if (checkbox.checked && blocked.length > 0) {
    checkbox.checked = false;
    toast("تم منع هذا المكون لأنه لا يناسب الحالة الصحية المحددة.", true);
    $("alerts").innerHTML = blocked.map((item) => `<div class="alert bad">${item}</div>`).join("");
    updateSummary();
    return;
  }
  updateSummary();
}

function enforceHealthBlocks(profile) {
  let removed = [];
  document.querySelectorAll(".ingredient").forEach((checkbox) => {
    const ingredient = state.ingredients.find((item) => item._id === checkbox.value);
    if (!ingredient) return;

    const blocked = validate(ingredient, profile);
    const label = checkbox.closest(".ingredient-row");
    if (blocked.length > 0) {
      checkbox.disabled = true;
      checkbox.title = blocked.join(" ");
      label?.classList.add("blocked");
      if (checkbox.checked) {
        checkbox.checked = false;
        removed.push(...blocked);
      }
    } else {
      checkbox.disabled = false;
      checkbox.title = "";
      label?.classList.remove("blocked");
    }
  });

  if (removed.length > 0) {
    toast("تم حذف مكونات مخالفة للحالة الصحية.", true);
  }
}

function mealIngredients(meal) {
  const ids = new Set((meal.ingredientIds || []).map(String));
  return state.ingredients.filter((item) => ids.has(String(item._id)));
}

function validateMeal(meal, profile) {
  return mealIngredients(meal).flatMap((item) => validate(item, profile));
}

function renderMealOrderButton(meal) {
  const blocked = validateMeal(meal, readReadyProfile());
  if (blocked.length > 0) {
    return `<button disabled title="${escapeHtml(blocked.join(" "))}">ممنوعة صحيا</button>`;
  }
  return `<button class="primary" onclick="placeReadyOrder('${meal._id}')">تأكيد الطلب</button>`;
}

function summarize(items) {
  return items.reduce((acc, item) => {
    acc.price += Number(item.price || 0);
    acc.calories += Number(item.calories || 0);
    acc.protein += Number(item.protein || 0);
    acc.carbs += Number(item.carbs || 0);
    acc.fat += Number(item.fat || 0);
    return acc;
  }, { price: 0, calories: 0, protein: 0, carbs: 0, fat: 0 });
}

function validate(item, profile) {
  const selected = new Set(profile.conditions);
  const flags = item.flags || {};
  const errors = [];
  if (selected.has("LACTOSE_INTOLERANCE") && flags.containsLactose) errors.push(`${item.name}: يحتوي على لاكتوز، لذلك يمنع اختياره.`);
  if (selected.has("GLUTEN_INTOLERANCE") && flags.containsGluten) errors.push(`${item.name}: يحتوي على غلوتين، لذلك يمنع اختياره.`);
  if (selected.has("DIABETES") && flags.highSugar) errors.push(`${item.name}: عالي السكر، لذلك لا يناسب حالة السكري.`);
  if (selected.has("HYPERTENSION") && flags.highSodium) errors.push(`${item.name}: عالي الصوديوم، لذلك لا يناسب حالة الضغط.`);
  if (selected.has("FATTY_LIVER") && flags.highFat) errors.push(`${item.name}: عالي الدهون، لذلك لا يناسب دهون الكبد.`);
  return errors;
}

function buildFeedback(profile, summary) {
  if (summary.calories === 0) return [];
  const target = estimateMealTarget(profile);
  const feedback = [];
  if (summary.calories > target * 1.15) feedback.push(`السعرات أعلى من هدف الوجبة بحوالي ${round(summary.calories - target)} سعرة.`);
  else if (summary.calories < target * 0.75) feedback.push("السعرات أقل من هدف الوجبة، ويمكن إضافة مصدر بروتين أو نشويات مناسب.");
  else feedback.push("هذه الوجبة مناسبة مبدئيا لهدف السعرات.");
  if (summary.protein < 20) feedback.push("البروتين منخفض نسبيا.");
  if (summary.fat > 25) feedback.push("نسبة الدهون مرتفعة نسبيا.");
  if (summary.carbs > 60 && profile.conditions.includes("DIABETES")) feedback.push("الكربوهيدرات مرتفعة لحالة السكري.");
  return feedback;
}

function estimateMealTarget(profile) {
  const adjustments = { MAINTAIN: 0, CLEAN_BULK: 250, BULK: 450, CUT: -400 };
  const bmr = (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5;
  return Math.max(1200, (bmr * 1.35) + adjustments[profile.goal]) / 3;
}

function printInvoice(id) {
  const order = [...state.orders, ...state.allOrders].find((item) => item._id === id);
  if (!order) return;

  const lines = (order.lines || []).map((line) => `
    <h3>${escapeHtml(line.name)}</h3>
    <ul>${(line.ingredients || []).map((item) => `<li>${escapeHtml(item.name)} - ${item.price || 0} د.ل</li>`).join("")}</ul>
  `).join("");

  const win = window.open("", "_blank");
  win.document.write(`
    <html lang="ar" dir="rtl">
    <head>
      <meta charset="utf-8">
      <title>فاتورة طلب #${order.orderNo}</title>
      <style>
        body { font-family: Tahoma, Arial, sans-serif; padding: 24px; line-height: 1.8; }
        h1 { margin-bottom: 4px; }
        .total { border-top: 1px solid #222; margin-top: 20px; padding-top: 12px; font-weight: bold; }
      </style>
    </head>
    <body>
      <h1>فاتورة مطعم صحي</h1>
      <p>رقم الطلب: #${order.orderNo}</p>
      <p>الطاولة: ${order.tableNumber}</p>
      <p>الحالة: ${statusLabel(order.status)}</p>
      ${lines}
      <p>السعرات: ${order.calories} | بروتين: ${order.protein}g | كارب: ${order.carbs}g | دهون: ${order.fat}g</p>
      <p class="total">الإجمالي: ${order.subtotal} د.ل</p>
      <pre>${escapeHtml(order.healthNotes || "")}</pre>
    </body>
    </html>
  `);
  win.document.close();
  win.print();
}

function flagText(item) {
  const flags = [];
  if (item.flags?.containsLactose) flags.push("لاكتوز");
  if (item.flags?.containsGluten) flags.push("غلوتين");
  if (item.flags?.highSugar) flags.push("سكر");
  if (item.flags?.highSodium) flags.push("صوديوم");
  if (item.flags?.highFat) flags.push("دهون");
  return flags.length ? ` | تنبيه: ${flags.join("، ")}` : "";
}

function groupBy(items, key) {
  return items.reduce((groups, item) => {
    groups[item[key]] = groups[item[key]] || [];
    groups[item[key]].push(item);
    return groups;
  }, {});
}

function typeLabel(type) {
  return type === "CUSTOM_MEAL" ? "وجبة مخصصة" : "وجبة جاهزة";
}

function statusLabel(status) {
  return ({ NEW: "جديد", PREPARING: "قيد التحضير", READY: "جاهز", COMPLETED: "مكتمل", CANCELLED: "ملغي" })[status] || status;
}

function round(value) {
  return Math.round(Number(value || 0) * 10) / 10;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function toast(message, bad = false) {
  const box = $("toast");
  box.textContent = message;
  box.style.background = bad ? "#a33a2e" : "#1f6b45";
  box.style.display = "block";
  clearTimeout(window.toastTimer);
  window.toastTimer = setTimeout(() => { box.style.display = "none"; }, 2600);
}

window.placeReadyOrder = placeReadyOrder;
window.setStatus = setStatus;
window.toggleIngredient = toggleIngredient;
window.toggleMeal = toggleMeal;
window.toggleStaff = toggleStaff;
window.updateSummary = updateSummary;
window.handleIngredientChange = handleIngredientChange;
window.printInvoice = printInvoice;
