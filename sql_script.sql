
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
-- Table: expense
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS expense (
  id SERIAL PRIMARY KEY,
  amount INT,
  date_time VARCHAR(255),
  description VARCHAR(400),
  category_id INT,
  client_id INT,
  CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_expense_client FOREIGN KEY (client_id) REFERENCES client(id)
);

-- -----------------------------------------------------
-- Table: role
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
  id INT PRIMARY KEY,
  name VARCHAR(255)
);

-- -----------------------------------------------------
-- Table: users (переименовано из "user" для безопасности)
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

ALTER SEQUENCE expenses_tracker.client_id_seq RESTART WITH 9;
ALTER SEQUENCE expenses_tracker.expense_id_seq RESTART WITH 16;
ALTER SEQUENCE expenses_tracker.users_id_seq RESTART WITH 9;