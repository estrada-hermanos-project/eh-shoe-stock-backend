-- =====================================================================
--  V4__shoe_stock_add_min_stock.sql
--  Agrega shoe_stock.min_stock: cantidad minima de existencias de una
--  variante. Permite notificar al administrador cuando el stock llega
--  al minimo. Default 0 para no romper las filas existentes.
--
--  Idempotente: solo agrega la columna si aun no existe.
-- =====================================================================

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'shoe_stock'
      AND COLUMN_NAME  = 'min_stock'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE shoe_stock ADD COLUMN min_stock INT NOT NULL DEFAULT 0 AFTER stock',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
