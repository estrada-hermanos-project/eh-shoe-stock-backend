-- =====================================================================
--  V3__shoe_type_to_varchar.sql
--  Convierte shoe.type de ENUM a VARCHAR(20).
--  Los valores permitidos (Dama/Caballero/Niño/Niña) se validan en la
--  capa de aplicacion (ShoeType), por lo que el ENUM a nivel de BD es
--  redundante y ademas rompe la validacion de esquema de Hibernate
--  (String -> VARCHAR vs columna ENUM).
--
--  Idempotente: solo modifica la columna si aun es de tipo ENUM.
-- =====================================================================

SET @is_enum := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'shoe'
      AND COLUMN_NAME  = 'type'
      AND DATA_TYPE    = 'enum'
);

SET @sql := IF(@is_enum > 0,
    'ALTER TABLE shoe MODIFY COLUMN type VARCHAR(20) NOT NULL',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
