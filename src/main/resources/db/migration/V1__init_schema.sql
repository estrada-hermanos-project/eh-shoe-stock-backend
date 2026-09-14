-- =====================================================================
--  V1__init_schema.sql
--  Esquema inicial del sistema de inventario "Estrada Hermanos"
--  SGBD: MySQL 8.x | Motor: InnoDB | Charset: utf8mb4
--  NOTA: la tabla `order` es palabra reservada -> se escribe entre
--  acentos graves (`). El esquema/base de datos lo fija el datasource,
--  por lo que esta migracion no ejecuta USE ni CREATE DATABASE.
-- =====================================================================

-- =====================================================================
--  MODULO USUARIOS
-- =====================================================================

-- Administradores / personas fisicas del sistema
CREATE TABLE user_admin (
    dpi         VARCHAR(13)   NOT NULL,                 -- PK (DPI de Guatemala: 13 digitos)
    full_name   VARCHAR(100)  NOT NULL,
    phone       VARCHAR(20)   NOT NULL,
    email       VARCHAR(100)  NULL,
    CONSTRAINT pk_user_admin PRIMARY KEY (dpi)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- Cuentas de acceso, ligadas a un administrador
CREATE TABLE user_account (
    username        VARCHAR(50)   NOT NULL,             -- PK
    password_hash   VARCHAR(255)  NOT NULL,
    dpi             VARCHAR(13)   NOT NULL,             -- FK -> user_admin.dpi
    CONSTRAINT pk_user_account PRIMARY KEY (username),
    CONSTRAINT fk_user_account_admin
        FOREIGN KEY (dpi) REFERENCES user_admin (dpi)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- Sesiones de usuario
CREATE TABLE user_session (
    id                INT           NOT NULL AUTO_INCREMENT,  -- PK
    session_code      VARCHAR(255)  NOT NULL,
    creation_date     DATETIME      NOT NULL,
    expiration_date   DATETIME      NOT NULL,
    CONSTRAINT pk_user_session PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
--  MODULO PROVEEDORES
-- =====================================================================

CREATE TABLE supplier (
    id          INT           NOT NULL AUTO_INCREMENT,   -- PK
    full_name   VARCHAR(100)  NOT NULL,
    phone       VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_supplier PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
--  MODULO PRODUCTOS (ZAPATOS)
-- =====================================================================

-- Catalogo de tipos: Dama, Caballero, Nino, Nina
CREATE TABLE shoe (
    code          VARCHAR(50)   NOT NULL,               -- PK
    type          ENUM('Dama', 'Caballero', 'Niño', 'Niña') NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    description   VARCHAR(255)  NULL,
    supplier      INT           NOT NULL,               -- FK -> supplier.id
    CONSTRAINT pk_shoe PRIMARY KEY (code),
    CONSTRAINT fk_shoe_supplier
        FOREIGN KEY (supplier) REFERENCES supplier (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- Existencias por color y talla
CREATE TABLE shoe_stock (
    id          INT           NOT NULL AUTO_INCREMENT,   -- PK
    shoe_id     VARCHAR(50)   NOT NULL,                  -- FK -> shoe.code
    color       VARCHAR(30)   NOT NULL,
    size        INT           NOT NULL,                  -- tallas solo enteras (p. ej. 35, 36, 37)
    stock       INT           NOT NULL DEFAULT 0,
    CONSTRAINT pk_shoe_stock PRIMARY KEY (id),
    CONSTRAINT fk_shoe_stock_shoe
        FOREIGN KEY (shoe_id) REFERENCES shoe (code)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
--  MODULO VENTAS
-- =====================================================================

CREATE TABLE sale (
    id              INT      NOT NULL AUTO_INCREMENT,    -- PK
    shoe_stock_id   INT      NOT NULL,                   -- FK -> shoe_stock.id
    amount          INT      NOT NULL,
    size            INT      NOT NULL,
    sale_date       DATE     NOT NULL,
    CONSTRAINT pk_sale PRIMARY KEY (id),
    CONSTRAINT fk_sale_shoe_stock
        FOREIGN KEY (shoe_stock_id) REFERENCES shoe_stock (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
--  MODULO MERCADERIA
-- =====================================================================

-- `order` es palabra reservada -> se escribe entre acentos graves
CREATE TABLE `order` (
    id                    VARCHAR(50)  NOT NULL,         -- PK
    order_delivery_date   DATE         NOT NULL,
    supplier              INT          NOT NULL,         -- FK -> supplier.id
    CONSTRAINT pk_order PRIMARY KEY (id),
    CONSTRAINT fk_order_supplier
        FOREIGN KEY (supplier) REFERENCES supplier (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- Detalle del pedido (clave primaria compuesta id + order_id)
CREATE TABLE order_detail (
    id              INT           NOT NULL AUTO_INCREMENT,  -- parte de la PK
    order_id        VARCHAR(50)   NOT NULL,                 -- parte de la PK y FK -> order.id
    shoe_stock_id   INT           NOT NULL,                 -- FK -> shoe_stock.id
    CONSTRAINT pk_order_detail PRIMARY KEY (id, order_id),
    CONSTRAINT fk_order_detail_order
        FOREIGN KEY (order_id) REFERENCES `order` (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_order_detail_shoe_stock
        FOREIGN KEY (shoe_stock_id) REFERENCES shoe_stock (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
