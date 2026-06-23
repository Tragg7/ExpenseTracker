CREATE SCHEMA IF NOT EXISTS expenses_tracker;
SET search_path TO expenses_tracker;

-- -----------------------------------------------------
-- Table: category
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS category (
  id INT PRIMARY KEY,
  name VARCHAR(255),
  type VARCHAR(20)
);

-- -----------------------------------------------------
-- Table: client
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS client (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255),
  first_name VARCHAR(255),
  last_name VARCHAR(255)
);

-- -----------------------------------------------------
-- Table: account
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS account (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(20) NOT NULL,
  balance NUMERIC(12,2) DEFAULT 0,
  currency VARCHAR(10) DEFAULT 'RUB',
  is_active BOOLEAN DEFAULT TRUE,
  description VARCHAR(400),
  client_id INT,
  CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES client(id)
);

-- -----------------------------------------------------
-- Table: expense (ОБНОВЛЕНА - добавлены original_amount и original_currency)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS expense (
  id SERIAL PRIMARY KEY,
  amount NUMERIC(10,2),
  date_time TIMESTAMP,
  description VARCHAR(400),
  category_id INT,
  client_id INT,
  account_id INT,
  original_amount NUMERIC(10,2),
  original_currency VARCHAR(10) DEFAULT 'RUB',
  CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_expense_client FOREIGN KEY (client_id) REFERENCES client(id),
  CONSTRAINT fk_expense_account FOREIGN KEY (account_id) REFERENCES account(id)
);

-- -----------------------------------------------------
-- Table: income (ОБНОВЛЕНА - добавлены original_amount и original_currency)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS income (
  id SERIAL PRIMARY KEY,
  amount NUMERIC(10,2),
  date_time TIMESTAMP,
  description VARCHAR(400),
  category_id INT,
  client_id INT,
  account_id INT,
  original_amount NUMERIC(10,2),
  original_currency VARCHAR(10) DEFAULT 'RUB',
  CONSTRAINT fk_income_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_income_client FOREIGN KEY (client_id) REFERENCES client(id),
  CONSTRAINT fk_income_account FOREIGN KEY (account_id) REFERENCES account(id)
);

-- -----------------------------------------------------
-- Table: role
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
  id INT PRIMARY KEY,
  name VARCHAR(255)
);

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  password VARCHAR(255),
  user_name VARCHAR(255),
  client_id INT UNIQUE,
  enabled BOOLEAN,
  CONSTRAINT fk_users_client FOREIGN KEY (client_id) REFERENCES client(id)
);

-- -----------------------------------------------------
-- Table: users_roles
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users_roles (
  user_id INT,
  role_id INT,
  CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES role(id)
);

ALTER SEQUENCE expenses_tracker.client_id_seq RESTART WITH 9;
ALTER SEQUENCE expenses_tracker.expense_id_seq RESTART WITH 16;
ALTER SEQUENCE expenses_tracker.users_id_seq RESTART WITH 9;

INSERT INTO category (id, name, type) VALUES (14, 'Начальный баланс', 'INCOME') ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, type) VALUES (15, 'Перевод', 'INCOME') ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, type) VALUES (16, 'Перевод', 'EXPENSE') ON CONFLICT (id) DO NOTHING;

ALTER TABLE income ALTER COLUMN is_initial_balance SET DEFAULT FALSE;
ALTER TABLE income ALTER COLUMN is_initial_balance SET NOT NULL;