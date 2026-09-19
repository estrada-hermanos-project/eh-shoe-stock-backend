-- =====================================================================
--  V7__shoe_add_created_at.sql
--  Agrega shoe.created_at: fecha de alta del estilo, para el reporte
--  de estilos nuevos del periodo (G-02). NULL en filas existentes
--  para no marcarlas como nuevas.
--
--  Idempotente: solo agrega la columna si aun no existe.
-- =====================================================================

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'shoe'
      AND COLUMN_NAME  = 'created_at'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE shoe ADD COLUMN created_at DATE NULL AFTER supplier',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
