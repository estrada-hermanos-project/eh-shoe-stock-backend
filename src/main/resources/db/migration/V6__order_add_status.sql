-- =====================================================================
--  V6__order_add_status.sql
--  Agrega `order`.status: estado del pedido (PENDIENTE | RECIBIDA).
--  VARCHAR en BD; el conjunto de valores se valida en el backend con
--  OrderStatusEnum. Default PENDIENTE para no romper filas existentes.
--
--  Idempotente: solo agrega la columna si aun no existe.
-- =====================================================================

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'order'
      AND COLUMN_NAME  = 'status'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''PENDIENTE'' AFTER supplier',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
