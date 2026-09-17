-- =====================================================================
--  V2__user_session_add_account_fk.sql
--  Vincula cada sesion con su cuenta de acceso.
--  Agrega user_session.user_account_id como FK -> user_account.username.
--  Columna NULL: no rompe filas existentes y una sesion puede vincularse
--  despues de crearse. El tipo coincide con user_account.username VARCHAR(50).
--
--  Idempotente: MySQL 8.x no soporta ADD COLUMN/CONSTRAINT IF NOT EXISTS,
--  por lo que se consulta information_schema y solo se aplica si falta.
--  Asi la migracion no falla si el cambio ya se hizo aparte en la base.
-- =====================================================================

-- Columna: agregar solo si no existe
SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'user_session'
      AND COLUMN_NAME  = 'user_account_id'
);

SET @sql := IF(@column_exists = 0,
    'ALTER TABLE user_session ADD COLUMN user_account_id VARCHAR(50) NULL AFTER session_code',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- Llave foranea: agregar solo si no existe
SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA     = DATABASE()
      AND TABLE_NAME       = 'user_session'
      AND CONSTRAINT_NAME  = 'fk_user_session_account'
      AND CONSTRAINT_TYPE  = 'FOREIGN KEY'
);

SET @sql := IF(@fk_exists = 0,
    'ALTER TABLE user_session ADD CONSTRAINT fk_user_session_account FOREIGN KEY (user_account_id) REFERENCES user_account (username) ON UPDATE CASCADE ON DELETE RESTRICT',
    'DO 0');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
