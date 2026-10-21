-- =====================================================================
--  V11__login_code.sql
--  Código de un solo uso para el segundo paso del login.
--  used es BIT porque Hibernate 6 valida el booleano de MySQL como bit.
-- =====================================================================

CREATE TABLE login_code (
    id               INT           NOT NULL AUTO_INCREMENT,
    username         VARCHAR(50)   NOT NULL,
    code             VARCHAR(4)    NOT NULL,
    creation_date    DATETIME      NOT NULL,
    expiration_date  DATETIME      NOT NULL,
    used             BIT(1)        NOT NULL,
    CONSTRAINT pk_login_code PRIMARY KEY (id),
    CONSTRAINT fk_login_code_account
        FOREIGN KEY (username) REFERENCES user_account (username)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_login_code_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
