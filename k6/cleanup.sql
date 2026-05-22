-- 스트레스 테스트 데이터 정리 (재실행 전 초기화)
-- 실행: docker exec -i <db-container> psql -U <user> -d <db> < k6/cleanup.sql

DELETE FROM deliveries       WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM order_item_options WHERE order_item_id IN (
    SELECT oi.id FROM order_items oi
    JOIN orders o ON oi.order_id = o.id
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM order_items      WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM payments         WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM orders           WHERE restaurant_id IN (
    SELECT id FROM restaurants WHERE name = '스트레스테스트식당'
);
DELETE FROM menu_option_items WHERE menu_option_id IN (
    SELECT mo.id FROM menu_options mo
    JOIN menus m ON mo.menu_id = m.id
    JOIN restaurants r ON m.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM menu_options     WHERE menu_id IN (
    SELECT m.id FROM menus m
    JOIN restaurants r ON m.restaurant_id = r.id
    WHERE r.name = '스트레스테스트식당'
);
DELETE FROM menus            WHERE restaurant_id IN (
    SELECT id FROM restaurants WHERE name = '스트레스테스트식당'
);
DELETE FROM restaurants      WHERE name = '스트레스테스트식당';
DELETE FROM notifications    WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE 'stress_%@test.com'
);
DELETE FROM users            WHERE email LIKE 'stress_%@test.com';

-- ── 주문 생성 부하 테스트 데이터 정리 ──────────────────────────────────────
DELETE FROM deliveries       WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM order_item_options WHERE order_item_id IN (
    SELECT oi.id FROM order_items oi
    JOIN orders o ON oi.order_id = o.id
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM order_items      WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM payments         WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN restaurants r ON o.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM orders           WHERE restaurant_id IN (
    SELECT id FROM restaurants WHERE name = '부하테스트식당'
);
DELETE FROM menu_option_items WHERE menu_option_id IN (
    SELECT mo.id FROM menu_options mo
    JOIN menus m ON mo.menu_id = m.id
    JOIN restaurants r ON m.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM menu_options     WHERE menu_id IN (
    SELECT m.id FROM menus m
    JOIN restaurants r ON m.restaurant_id = r.id
    WHERE r.name = '부하테스트식당'
);
DELETE FROM menus            WHERE restaurant_id IN (
    SELECT id FROM restaurants WHERE name = '부하테스트식당'
);
DELETE FROM restaurants      WHERE name = '부하테스트식당';
DELETE FROM notifications    WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE 'load_%@test.com'
);
DELETE FROM users            WHERE email LIKE 'load_%@test.com';
