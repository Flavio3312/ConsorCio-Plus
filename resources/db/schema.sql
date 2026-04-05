-- ============================================================
-- ConsorCio+ — Schema v1.0
-- Base de datos para Administración de Consorcios
-- Ejecutar en MySQL 8.x (XAMPP / phpMyAdmin)
-- ============================================================

CREATE DATABASE IF NOT EXISTS consorcioplus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE consorcioplus;

-- ------------------------------------------------------------
-- USUARIOS DEL SISTEMA
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL COMMENT 'SHA-256 hex',
    perfil   ENUM('ADMINISTRADOR', 'OPERADOR') NOT NULL,
    activo   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT NOW()
);

-- Usuario inicial: admin / admin123
INSERT INTO usuario (username, password_hash, perfil)
VALUES ('admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'ADMINISTRADOR');

-- ------------------------------------------------------------
-- CONSORCIOS / EDIFICIOS
-- ------------------------------------------------------------
CREATE TABLE consorcio (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    direccion   VARCHAR(200) NOT NULL,
    cuit        VARCHAR(13)  UNIQUE,
    total_pisos INT          NOT NULL DEFAULT 1,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_consorcio_activo ON consorcio(activo);

-- ------------------------------------------------------------
-- UNIDADES FUNCIONALES
-- ------------------------------------------------------------
CREATE TABLE unidad_funcional (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    numero       VARCHAR(10)    NOT NULL,
    piso         VARCHAR(5),
    porcentual   DECIMAL(7, 4)  NOT NULL
        COMMENT '% de participación: ej. 5.2500',
    id_consorcio INT            NOT NULL,
    activo       BOOLEAN        NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uk_uf_consorcio_numero (id_consorcio, numero)
);
CREATE INDEX idx_uf_consorcio ON unidad_funcional(id_consorcio);

-- ------------------------------------------------------------
-- PERSONAS (PROPIETARIOS E INQUILINOS)
-- ------------------------------------------------------------
CREATE TABLE persona (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    tipo     ENUM('PROPIETARIO', 'INQUILINO') NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni      VARCHAR(15),
    telefono VARCHAR(20),
    email    VARCHAR(100),
    activo   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- VINCULACIÓN PERSONA <-> UNIDAD FUNCIONAL
-- ------------------------------------------------------------
CREATE TABLE persona_unidad (
    id          INT  AUTO_INCREMENT PRIMARY KEY,
    id_persona  INT  NOT NULL,
    id_unidad   INT  NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE,
    FOREIGN KEY (id_persona) REFERENCES persona(id),
    FOREIGN KEY (id_unidad)  REFERENCES unidad_funcional(id)
);
CREATE INDEX idx_pu_unidad  ON persona_unidad(id_unidad);
CREATE INDEX idx_pu_persona ON persona_unidad(id_persona);

-- ------------------------------------------------------------
-- PROVEEDORES
-- ------------------------------------------------------------
CREATE TABLE proveedor (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cuit   VARCHAR(13)
);

-- ------------------------------------------------------------
-- GASTOS DEL CONSORCIO
-- ------------------------------------------------------------
CREATE TABLE gasto (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    id_consorcio INT            NOT NULL,
    periodo      DATE           NOT NULL
        COMMENT 'Primer día del mes: YYYY-MM-01',
    categoria    ENUM('ORDINARIO', 'EXTRAORDINARIO') NOT NULL,
    descripcion  VARCHAR(200)   NOT NULL,
    monto        DECIMAL(12, 2) NOT NULL CHECK (monto > 0),
    nro_factura  VARCHAR(50),
    id_proveedor INT,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id)
);
CREATE INDEX idx_gasto_consorcio_periodo ON gasto(id_consorcio, periodo);

-- ------------------------------------------------------------
-- LIQUIDACIONES MENSUALES
-- ------------------------------------------------------------
CREATE TABLE liquidacion (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    id_consorcio         INT            NOT NULL,
    periodo              DATE           NOT NULL,
    total_ordinario      DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_extraordinario DECIMAL(12, 2) NOT NULL DEFAULT 0,
    fecha_cierre         DATETIME,
    cerrada              BOOLEAN        NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id),
    UNIQUE KEY uk_liq_consorcio_periodo (id_consorcio, periodo)
);
CREATE INDEX idx_liq_consorcio ON liquidacion(id_consorcio);

-- ------------------------------------------------------------
-- DETALLE DE LIQUIDACIÓN POR UNIDAD FUNCIONAL
-- ------------------------------------------------------------
CREATE TABLE liquidacion_detalle (
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    id_liquidacion         INT            NOT NULL,
    id_unidad              INT            NOT NULL,
    expensa_ordinaria      DECIMAL(12, 2) NOT NULL DEFAULT 0,
    expensa_extraordinaria DECIMAL(12, 2) NOT NULL DEFAULT 0,
    mora_aplicada          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_a_pagar          DECIMAL(12, 2) NOT NULL DEFAULT 0,
    saldo_deudor           DECIMAL(12, 2) NOT NULL DEFAULT 0,
    FOREIGN KEY (id_liquidacion) REFERENCES liquidacion(id),
    FOREIGN KEY (id_unidad)      REFERENCES unidad_funcional(id)
);
CREATE INDEX idx_liqdet_liquidacion ON liquidacion_detalle(id_liquidacion);
CREATE INDEX idx_liqdet_unidad      ON liquidacion_detalle(id_unidad);

-- ------------------------------------------------------------
-- PAGOS DE EXPENSAS
-- ------------------------------------------------------------
CREATE TABLE pago (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    id_liq_detalle INT            NOT NULL,
    fecha_pago     DATETIME       NOT NULL DEFAULT NOW(),
    monto_pagado   DECIMAL(12, 2) NOT NULL CHECK (monto_pagado > 0),
    nro_recibo     VARCHAR(30)    NOT NULL UNIQUE,
    id_usuario     INT            NOT NULL,
    FOREIGN KEY (id_liq_detalle) REFERENCES liquidacion_detalle(id),
    FOREIGN KEY (id_usuario)     REFERENCES usuario(id)
);
CREATE INDEX idx_pago_detalle ON pago(id_liq_detalle);

-- ------------------------------------------------------------
-- RECLAMOS DE MANTENIMIENTO
-- ------------------------------------------------------------
CREATE TABLE reclamo (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_unidad        INT      NOT NULL,
    descripcion      TEXT     NOT NULL,
    estado           ENUM('PENDIENTE', 'EN_CURSO', 'RESUELTO', 'DESCARTADO')
                     NOT NULL DEFAULT 'PENDIENTE',
    fecha_alta       DATETIME NOT NULL DEFAULT NOW(),
    fecha_resolucion DATETIME,
    id_usuario_alta  INT      NOT NULL,
    FOREIGN KEY (id_unidad)      REFERENCES unidad_funcional(id),
    FOREIGN KEY (id_usuario_alta) REFERENCES usuario(id)
);
CREATE INDEX idx_reclamo_unidad ON reclamo(id_unidad);
CREATE INDEX idx_reclamo_estado ON reclamo(estado);
