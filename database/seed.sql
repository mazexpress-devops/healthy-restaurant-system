SET NAMES utf8mb4;

USE healthy_restaurant;

INSERT INTO users (username, password_hash, full_name, role, active)
VALUES
  ('admin', SHA2('admin123', 256), 'مدير النظام', 'ADMIN', TRUE),
  ('chef', SHA2('chef123', 256), 'شيف المطبخ', 'CHEF', TRUE)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  role = VALUES(role),
  active = TRUE;

INSERT INTO ingredients
  (name, category, serving_label, price, calories, protein_g, carbs_g, fat_g,
   contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat, available)
VALUES
  ('صدر دجاج مشوي', 'PROTEIN', '150 جرام', 18.00, 248, 46, 0, 5, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('سلمون مشوي', 'PROTEIN', '150 جرام', 28.00, 310, 34, 0, 18, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('توفو نباتي', 'PROTEIN', '120 جرام', 14.00, 145, 16, 4, 8, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('أرز بني', 'CARB', '150 جرام', 7.00, 165, 4, 35, 1, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('كينوا', 'CARB', '150 جرام', 10.00, 180, 6, 32, 3, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('خبز شوفان', 'CARB', 'قطعتان', 6.00, 160, 6, 28, 3, FALSE, TRUE, FALSE, FALSE, FALSE, TRUE),
  ('بطاطا حلوة', 'CARB', '150 جرام', 6.00, 135, 2, 31, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('زيت زيتون', 'FAT', 'ملعقة كبيرة', 4.00, 119, 0, 0, 14, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('أفوكادو', 'FAT', 'نصف حبة', 9.00, 160, 2, 9, 15, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
  ('جبن قليل الدسم', 'ADDON', '40 جرام', 5.00, 90, 8, 2, 5, TRUE, FALSE, FALSE, TRUE, FALSE, TRUE),
  ('خضار مطهوة بالبخار', 'ADDON', 'كوب', 5.00, 55, 3, 11, 0, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('ذرة حلوة', 'ADDON', 'نصف كوب', 4.00, 75, 2, 17, 1, FALSE, FALSE, TRUE, FALSE, FALSE, TRUE),
  ('صلصة زبادي', 'SAUCE', '30 جرام', 3.00, 45, 3, 4, 2, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE),
  ('صلصة صويا قليلة الملح', 'SAUCE', '20 مل', 3.00, 15, 1, 2, 0, FALSE, FALSE, FALSE, TRUE, FALSE, TRUE)
ON DUPLICATE KEY UPDATE
  name = VALUES(name);

INSERT INTO ready_meals
  (name, description, price, calories, protein_g, carbs_g, fat_g, available)
VALUES
  ('طبق دجاج وكينوا', 'صدر دجاج مشوي مع كينوا وخضار مطهوة بالبخار.', 32.00, 483, 56, 43, 9, TRUE),
  ('طبق سلمون وأرز بني', 'سلمون مشوي مع أرز بني وخضار.', 40.00, 530, 41, 46, 19, TRUE),
  ('طبق نباتي صحي', 'توفو نباتي مع بطاطا حلوة وخضار.', 27.00, 335, 21, 46, 8, TRUE)
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
  (rm.name = 'طبق دجاج وكينوا' AND ing.name IN ('صدر دجاج مشوي', 'كينوا', 'خضار مطهوة بالبخار'))
  OR (rm.name = 'طبق سلمون وأرز بني' AND ing.name IN ('سلمون مشوي', 'أرز بني', 'خضار مطهوة بالبخار'))
  OR (rm.name = 'طبق نباتي صحي' AND ing.name IN ('توفو نباتي', 'بطاطا حلوة', 'خضار مطهوة بالبخار'));
