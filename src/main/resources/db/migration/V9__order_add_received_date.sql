-- =====================================================================
--  V9__order_add_received_date.sql
--  Agrega `order`.received_date: dia en que el pedido paso a RECIBIDA.
--  NULL mientras sigue PENDIENTE. No toca order_delivery_date.
--
--  Idempotente: solo agrega la columna si aun no existe.
-- =====================================================================

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'order'
      AND COLUMN_NAME  = 'received_date'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN received_date DATE NULL AFTER order_delivery_date',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
