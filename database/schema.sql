SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS healthy_restaurant
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE healthy_restaurant;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(60) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  role ENUM('ADMIN', 'CHEF') NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ingredients (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  category ENUM('PROTEIN', 'CARB', 'FAT', 'ADDON', 'SAUCE') NOT NULL,
  serving_label VARCHAR(80) NOT NULL DEFAULT 'حصة واحدة',
  price DECIMAL(10,2) NOT NULL,
  calories DECIMAL(8,2) NOT NULL,
  protein_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  contains_lactose BOOLEAN NOT NULL DEFAULT FALSE,
  contains_gluten BOOLEAN NOT NULL DEFAULT FALSE,
  high_sugar BOOLEAN NOT NULL DEFAULT FALSE,
  high_sodium BOOLEAN NOT NULL DEFAULT FALSE,
  high_fat BOOLEAN NOT NULL DEFAULT FALSE,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_ingredient_price CHECK (price >= 0),
  CONSTRAINT chk_ingredient_calories CHECK (calories >= 0),
  CONSTRAINT chk_ingredient_macros CHECK (protein_g >= 0 AND carbs_g >= 0 AND fat_g >= 0),
  UNIQUE KEY uk_ingredients_name (name),
  INDEX idx_ingredients_category (category)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ready_meals (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(140) NOT NULL,
  description VARCHAR(500) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  calories DECIMAL(8,2) NOT NULL,
  protein_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_ready_meal_price CHECK (price >= 0),
  CONSTRAINT chk_ready_meal_calories CHECK (calories >= 0),
  UNIQUE KEY uk_ready_meals_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ready_meal_ingredients (
  ready_meal_id INT NOT NULL,
  ingredient_id INT NOT NULL,
  PRIMARY KEY (ready_meal_id, ingredient_id),
  CONSTRAINT fk_ready_meal_ingredient_meal
    FOREIGN KEY (ready_meal_id) REFERENCES ready_meals(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_ready_meal_ingredient_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
    ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS dining_tables (
  id INT AUTO_INCREMENT PRIMARY KEY,
  table_number INT NOT NULL,
  account_name VARCHAR(60) NOT NULL,
  password_hash CHAR(64) NOT NULL,
  status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_dining_table_number CHECK (table_number > 0),
  UNIQUE KEY uk_dining_tables_number (table_number),
  UNIQUE KEY uk_dining_tables_account (account_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  table_number INT NOT NULL,
  age INT NULL,
  weight_kg DECIMAL(6,2) NULL,
  height_cm DECIMAL(6,2) NULL,
  goal ENUM('MAINTAIN', 'CLEAN_BULK', 'BULK', 'CUT') NULL,
  notes VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_customer_table CHECK (table_number > 0),
  CONSTRAINT chk_customer_age CHECK (age IS NULL OR age BETWEEN 5 AND 120),
  CONSTRAINT chk_customer_weight CHECK (weight_kg IS NULL OR weight_kg > 0),
  CONSTRAINT chk_customer_height CHECK (height_cm IS NULL OR height_cm > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customer_conditions (
  customer_id INT NOT NULL,
  condition_code ENUM('DIABETES', 'LACTOSE_INTOLERANCE', 'GLUTEN_INTOLERANCE', 'HYPERTENSION', 'FATTY_LIVER') NOT NULL,
  PRIMARY KEY (customer_id, condition_code),
  CONSTRAINT fk_customer_condition_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NULL,
  table_number INT NOT NULL,
  order_type ENUM('READY_MEAL', 'CUSTOM_MEAL') NOT NULL,
  status ENUM('NEW', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'NEW',
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
  calories DECIMAL(8,2) NOT NULL DEFAULT 0,
  protein_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  health_notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE SET NULL,
  CONSTRAINT chk_order_table CHECK (table_number > 0),
  CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
  INDEX idx_orders_status (status),
  INDEX idx_orders_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  ready_meal_id INT NULL,
  name VARCHAR(160) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  unit_price DECIMAL(10,2) NOT NULL,
  calories DECIMAL(8,2) NOT NULL DEFAULT 0,
  protein_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_order_item_ready_meal
    FOREIGN KEY (ready_meal_id) REFERENCES ready_meals(id)
    ON DELETE SET NULL,
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
  CONSTRAINT chk_order_item_price CHECK (unit_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_item_ingredients (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_item_id INT NOT NULL,
  ingredient_id INT NULL,
  ingredient_name VARCHAR(140) NOT NULL,
  category ENUM('PROTEIN', 'CARB', 'FAT', 'ADDON', 'SAUCE') NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  calories DECIMAL(8,2) NOT NULL DEFAULT 0,
  protein_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(8,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_order_item_ingredient_item
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_order_item_ingredient_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
    ON DELETE SET NULL,
  UNIQUE KEY uk_order_item_ingredient_once (order_item_id, ingredient_id)
) ENGINE=InnoDB;
