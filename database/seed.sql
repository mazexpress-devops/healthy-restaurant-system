SET NAMES utf8mb4;

USE healthy_restaurant;

INSERT INTO users (username, password_hash, full_name, role, active)
VALUES
  ('admin', 'admin123', 'منال الفيتوري', 3, TRUE),
  ('chef', 'chef123', 'إيناس عبد المنعم', 2, TRUE)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  role = VALUES(role),
  active = TRUE;

INSERT INTO dining_tables (table_number, account_name, password_hash, status, active)
VALUES
  (1, 'table1', '1', 'OPEN', TRUE),
  (2, 'table2', '2', 'OPEN', TRUE),
  (3, 'table3', '3', 'OPEN', TRUE),
  (4, 'table4', '4', 'OPEN', TRUE),
  (5, 'table5', '5', 'OPEN', TRUE),
  (6, 'table6', '6', 'OPEN', TRUE),
  (7, 'table7', '7', 'OPEN', TRUE),
  (8, 'table8', '8', 'OPEN', TRUE),
  (9, 'table9', '9', 'OPEN', TRUE),
  (10, 'table10', '10', 'OPEN', TRUE)
ON DUPLICATE KEY UPDATE
  account_name = VALUES(account_name),
  password_hash = VALUES(password_hash),
  status = 'OPEN',
  active = TRUE;

UPDATE ready_meals SET available = FALSE;
UPDATE ingredients SET available = FALSE;
DELETE FROM ready_meal_ingredients;

INSERT INTO ingredients
  (name, category, serving_label, price, calories, protein_g, carbs_g, fat_g,
   contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat, available)
VALUES
  ('صدر دجاج مشوي بالتوابل الليبية', 'PROTEIN', '150 جرام', 18.00, 245, 46, 0, 5, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('تونة مصراتية بالماء', 'PROTEIN', '120 جرام', 16.00, 132, 29, 0, 1, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('لحم إبل مشوي قليل الدهن', 'PROTEIN', '130 جرام', 24.00, 220, 34, 0, 8, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('حمص مسلوق', 'PROTEIN', 'نصف كوب', 7.00, 135, 7, 22, 2, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('بيض مسلوق', 'PROTEIN', 'بيضتان', 6.00, 156, 13, 1, 11, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('رز مبخر', 'CARB', '150 جرام', 6.00, 190, 4, 41, 1, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('كسكسي شعير', 'CARB', '150 جرام', 8.00, 176, 6, 36, 1, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
  ('مكرونة قمح كامل للمبكبكة', 'CARB', '150 جرام', 7.00, 210, 8, 42, 2, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
  ('بطاطا مشوية', 'CARB', '150 جرام', 5.00, 130, 3, 30, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('خبز شعير', 'CARB', 'قطعتان صغيرتان', 5.00, 150, 5, 30, 2, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
  ('زيت زيتون الجبل الأخضر', 'FAT', 'ملعقة كبيرة', 4.00, 119, 0, 0, 14, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('زيتون أخضر طرابلسي', 'FAT', '8 حبات', 4.00, 55, 0, 2, 5, FALSE, FALSE, FALSE, TRUE, TRUE, TRUE),
  ('لوز ليبي محمص', 'FAT', '20 جرام', 7.00, 116, 4, 4, 10, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('سلطة مشوية', 'ADDON', 'كوب', 6.00, 80, 3, 12, 3, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('خيار وطماطم ونعناع', 'ADDON', 'كوب', 4.00, 35, 2, 7, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('فاصوليا خضراء مطبوخة', 'ADDON', 'كوب', 5.00, 44, 2, 10, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('صلصة طماطم حارة', 'SAUCE', '30 جرام', 3.00, 25, 1, 5, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('صلصة ليمون وكمون', 'SAUCE', '25 جرام', 2.00, 12, 0, 2, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('زبادي بالنعناع', 'SAUCE', '40 جرام', 3.00, 45, 3, 4, 2, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE)
ON DUPLICATE KEY UPDATE
  category = VALUES(category),
  serving_label = VALUES(serving_label),
  price = VALUES(price),
  calories = VALUES(calories),
  protein_g = VALUES(protein_g),
  carbs_g = VALUES(carbs_g),
  fat_g = VALUES(fat_g),
  contains_lactose = VALUES(contains_lactose),
  contains_gluten = VALUES(contains_gluten),
  high_sugar = VALUES(high_sugar),
  high_sodium = VALUES(high_sodium),
  high_fat = VALUES(high_fat),
  available = TRUE;

INSERT INTO ready_meals
  (name, description, price, calories, protein_g, carbs_g, fat_g, available)
VALUES
  ('طبق دجاج ليبي صحي', 'صدر دجاج مشوي بالتوابل الليبية مع رز مبخر وسلطة مشوية وصلصة ليمون وكمون.', 31.00, 527, 54, 55, 9, TRUE),
  ('سلطة تونة طرابلسية', 'تونة مصراتية بالماء مع خيار وطماطم ونعناع وزيتون أخضر وصلصة ليمون وكمون.', 26.00, 234, 31, 11, 6, TRUE),
  ('كسكسي شعير بالدجاج والخضار', 'كسكسي شعير مع صدر دجاج مشوي وفاصوليا خضراء وصلصة طماطم حارة.', 34.00, 490, 54, 51, 6, TRUE),
  ('طبق حمص وخضار مشوية', 'حمص مسلوق مع سلطة مشوية وبطاطا مشوية وزيت زيتون الجبل الأخضر.', 22.00, 464, 13, 64, 19, TRUE),
  ('لحم إبل مشوي مع بطاطا', 'لحم إبل مشوي قليل الدهن مع بطاطا مشوية وخيار وطماطم ونعناع.', 34.00, 385, 39, 37, 8, TRUE)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  price = VALUES(price),
  calories = VALUES(calories),
  protein_g = VALUES(protein_g),
  carbs_g = VALUES(carbs_g),
  fat_g = VALUES(fat_g),
  available = TRUE;

INSERT IGNORE INTO ready_meal_ingredients (ready_meal_id, ingredient_id)
SELECT rm.id, ing.id
FROM ready_meals rm
JOIN ingredients ing ON
  (rm.name = 'طبق دجاج ليبي صحي' AND ing.name IN ('صدر دجاج مشوي بالتوابل الليبية', 'رز مبخر', 'سلطة مشوية', 'صلصة ليمون وكمون'))
  OR (rm.name = 'سلطة تونة طرابلسية' AND ing.name IN ('تونة مصراتية بالماء', 'خيار وطماطم ونعناع', 'زيتون أخضر طرابلسي', 'صلصة ليمون وكمون'))
  OR (rm.name = 'كسكسي شعير بالدجاج والخضار' AND ing.name IN ('كسكسي شعير', 'صدر دجاج مشوي بالتوابل الليبية', 'فاصوليا خضراء مطبوخة', 'صلصة طماطم حارة'))
  OR (rm.name = 'طبق حمص وخضار مشوية' AND ing.name IN ('حمص مسلوق', 'سلطة مشوية', 'بطاطا مشوية', 'زيت زيتون الجبل الأخضر'))
  OR (rm.name = 'لحم إبل مشوي مع بطاطا' AND ing.name IN ('لحم إبل مشوي قليل الدهن', 'بطاطا مشوية', 'خيار وطماطم ونعناع'));

INSERT INTO orders
  (table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g, health_notes)
SELECT
  1,
  'READY_MEAL',
  'NEW',
  rm.price,
  rm.calories,
  rm.protein_g,
  rm.carbs_g,
  rm.fat_g,
  'طلب تجريبي ليبي للطاولة table1'
FROM ready_meals rm
WHERE rm.name = 'طبق دجاج ليبي صحي'
  AND NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table1'
  );

INSERT INTO order_items
  (order_id, ready_meal_id, name, quantity, unit_price, calories, protein_g, carbs_g, fat_g)
SELECT
  o.id,
  rm.id,
  rm.name,
  1,
  rm.price,
  rm.calories,
  rm.protein_g,
  rm.carbs_g,
  rm.fat_g
FROM orders o
JOIN ready_meals rm ON rm.name = 'طبق دجاج ليبي صحي'
WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table1'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.id
  );

INSERT IGNORE INTO order_item_ingredients
  (order_item_id, ingredient_id, ingredient_name, category, unit_price, calories, protein_g, carbs_g, fat_g)
SELECT
  oi.id,
  ing.id,
  ing.name,
  ing.category,
  ing.price,
  ing.calories,
  ing.protein_g,
  ing.carbs_g,
  ing.fat_g
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN ready_meal_ingredients rmi ON rmi.ready_meal_id = oi.ready_meal_id
JOIN ingredients ing ON ing.id = rmi.ingredient_id
WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table1'
  AND NOT EXISTS (
    SELECT 1
    FROM order_item_ingredients existing
    WHERE existing.order_item_id = oi.id
      AND existing.ingredient_id = ing.id
  );

INSERT INTO orders
  (table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g, health_notes)
SELECT
  2,
  'READY_MEAL',
  'PREPARING',
  rm.price,
  rm.calories,
  rm.protein_g,
  rm.carbs_g,
  rm.fat_g,
  'طلب تجريبي ليبي للطاولة table2'
FROM ready_meals rm
WHERE rm.name = 'سلطة تونة طرابلسية'
  AND NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table2'
  );

INSERT INTO order_items
  (order_id, ready_meal_id, name, quantity, unit_price, calories, protein_g, carbs_g, fat_g)
SELECT
  o.id,
  rm.id,
  rm.name,
  1,
  rm.price,
  rm.calories,
  rm.protein_g,
  rm.carbs_g,
  rm.fat_g
FROM orders o
JOIN ready_meals rm ON rm.name = 'سلطة تونة طرابلسية'
WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table2'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.id
  );

INSERT IGNORE INTO order_item_ingredients
  (order_item_id, ingredient_id, ingredient_name, category, unit_price, calories, protein_g, carbs_g, fat_g)
SELECT
  oi.id,
  ing.id,
  ing.name,
  ing.category,
  ing.price,
  ing.calories,
  ing.protein_g,
  ing.carbs_g,
  ing.fat_g
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN ready_meal_ingredients rmi ON rmi.ready_meal_id = oi.ready_meal_id
JOIN ingredients ing ON ing.id = rmi.ingredient_id
WHERE o.health_notes = 'طلب تجريبي ليبي للطاولة table2'
  AND NOT EXISTS (
    SELECT 1
    FROM order_item_ingredients existing
    WHERE existing.order_item_id = oi.id
      AND existing.ingredient_id = ing.id
  );
