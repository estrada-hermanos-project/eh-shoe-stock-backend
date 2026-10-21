-- =====================================================================
--  V10__password_recovery_token.sql
--  Token de un solo uso para recuperar la contraseña.
--  used es BIT porque Hibernate 6 valida el booleano de MySQL como bit.
-- =====================================================================

CREATE TABLE password_recovery_token (
    id               INT           NOT NULL AUTO_INCREMENT,
    token            VARCHAR(64)   NOT NULL,
    username         VARCHAR(50)   NOT NULL,
    creation_date    DATETIME      NOT NULL,
    expiration_date  DATETIME      NOT NULL,
    used             BIT(1)        NOT NULL,
    CONSTRAINT pk_password_recovery_token PRIMARY KEY (id),
    CONSTRAINT uk_password_recovery_token UNIQUE (token),
    CONSTRAINT fk_password_recovery_token_account
        FOREIGN KEY (username) REFERENCES user_account (username)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_password_recovery_token_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
