INSERT INTO category (id, name, type) VALUES (1, 'Продукты', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (2, 'Коммунальные услуги', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (3, 'Транспорт', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (4, 'Рестораны', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (5, 'Развлечение', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (6, 'Шопинг', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (7, 'Путешествия', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (8, 'Обучение', 'EXPENSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (15, 'Перевод', 'EXPENSE') ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, name, type) VALUES (9, 'Зарплата', 'INCOME') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (10, 'Аванс', 'INCOME') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (11, 'Инвестиции', 'INCOME') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (12, 'Подарок', 'INCOME') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (13, 'Возврат долга', 'INCOME') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, type) VALUES (14, 'Начальный баланс', 'INCOME') ON CONFLICT (id) DO NOTHING;