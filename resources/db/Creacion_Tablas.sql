-- ==============================================================================
-- ConsorCio+ — Definición de Base de Datos y Creación de Tablas (TP2)
-- Motor: MySQL 8.x
-- ==============================================================================

-- 1. CREACIÓN DE LA BASE DE DATOS
CREATE DATABASE IF NOT EXISTS consorcioplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE consorcioplus;

-- 2. CREACIÓN DE LAS TABLAS (DIAGRAMA ENTIDAD-RELACIÓN IMPLEMENTADO)

-- Tabla de Usuarios del sistema (Seguridad)
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL COMMENT 'Contraseña encriptada en SHA-256',
    perfil ENUM('ADMINISTRADOR','OPERADOR') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla de Consorcios / Edificios
CREATE TABLE consorcio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    cuit VARCHAR(13) UNIQUE,
    total_pisos INT,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla de Unidades Funcionales (UFs)
CREATE TABLE unidad_funcional (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(10) NOT NULL COMMENT 'Ej: 1A, PB1',
    piso VARCHAR(5),
    porcentual DECIMAL(6,4) NOT NULL COMMENT 'Valor entre 0.0000 y 100.0000',
    id_consorcio INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE (id_consorcio, numero)
);
CREATE INDEX idx_uf_consorcio ON unidad_funcional(id_consorcio);

-- Tabla unificada de Personas (Propietarios e Inquilinos)
CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('PROPIETARIO','INQUILINO') NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(15) UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla intermedia de asignación de Personas a Unidades Funcionales
CREATE TABLE persona_unidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    id_unidad INT NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE,
    FOREIGN KEY (id_persona) REFERENCES persona(id) ON DELETE CASCADE,
    FOREIGN KEY (id_unidad) REFERENCES unidad_funcional(id) ON DELETE CASCADE
);

-- Tabla de Proveedores de servicios
CREATE TABLE proveedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cuit VARCHAR(13)
);

-- Tabla de Gastos del Consorcio (Ordinarios y Extraordinarios)
CREATE TABLE gasto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_consorcio INT NOT NULL,
    periodo DATE NOT NULL COMMENT 'Día 1 del mes (Ej: 2024-05-01)',
    categoria ENUM('ORDINARIO','EXTRAORDINARIO') NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    nro_factura VARCHAR(50),
    id_proveedor INT,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor(id)
);
CREATE INDEX idx_gasto_periodo ON gasto(id_consorcio, periodo);

-- Tabla de Liquidaciones Mensuales (Cabecera)
CREATE TABLE liquidacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_consorcio INT NOT NULL,
    periodo DATE NOT NULL,
    total_ordinario DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_extraordinario DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_cierre DATETIME,
    cerrada BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_consorcio) REFERENCES consorcio(id),
    UNIQUE (id_consorcio, periodo)
);

-- Tabla Detalle de Liquidación (Lo que debe pagar cada UF en ese período)
CREATE TABLE liquidacion_detalle (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_liquidacion INT NOT NULL,
    id_unidad INT NOT NULL,
    expensa_ordinaria DECIMAL(12,2) NOT NULL DEFAULT 0,
    expensa_extraordinaria DECIMAL(12,2) NOT NULL DEFAULT 0,
    mora_aplicada DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_a_pagar DECIMAL(12,2) NOT NULL DEFAULT 0,
    saldo_deudor DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Se actualiza al registrar pagos',
    FOREIGN KEY (id_liquidacion) REFERENCES liquidacion(id) ON DELETE CASCADE,
    FOREIGN KEY (id_unidad) REFERENCES unidad_funcional(id)
);

-- Tabla de Pagos / Cobranzas
CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_liq_detalle INT NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_pagado DECIMAL(12,2) NOT NULL,
    nro_recibo VARCHAR(30) UNIQUE NOT NULL,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_liq_detalle) REFERENCES liquidacion_detalle(id),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id)
);

-- Tabla de Reclamos
CREATE TABLE reclamo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_unidad INT NOT NULL,
    descripcion TEXT NOT NULL,
    estado ENUM('PENDIENTE','EN_CURSO','RESUELTO','DESCARTADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME,
    id_usuario_alta INT NOT NULL,
    nro_reclamo VARCHAR(40) UNIQUE COMMENT 'Número generado automáticamente, ej: REC-20260530-001',
    FOREIGN KEY (id_unidad) REFERENCES unidad_funcional(id),
    FOREIGN KEY (id_usuario_alta) REFERENCES usuario(id)
);
CREATE INDEX idx_reclamo_estado ON reclamo(estado);
