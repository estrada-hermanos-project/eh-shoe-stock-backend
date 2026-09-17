-- =====================================================================
--  V5__order_detail_add_amount.sql
--  Agrega order_detail.amount: cantidad de pares solicitados en la
--  linea del pedido. Default 1 para no romper filas existentes.
--
--  Idempotente: solo agrega la columna si aun no existe.
-- =====================================================================

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'order_detail'
      AND COLUMN_NAME  = 'amount'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE order_detail ADD COLUMN amount INT NOT NULL DEFAULT 1 AFTER shoe_stock_id',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
