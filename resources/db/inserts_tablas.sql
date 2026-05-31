USE consorcioplus;

-- ==========================================
-- 1. INSERCIÓN DE REGISTROS (INSERT)
-- ==========================================

-- Insertar Usuario Administrador
INSERT INTO usuario (username, password_hash, perfil, activo)
VALUES ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMINISTRADOR', TRUE);

-- Insertar Consorcio
INSERT INTO consorcio (nombre, direccion, cuit, total_pisos, activo)
VALUES ('Edificio Los Álamos', 'Av. del Libertador 1234, CABA', '30-12345678-9', 10, TRUE);

-- Insertar Unidades Funcionales (Total = 100%)
INSERT INTO unidad_funcional (numero, piso, porcentual, id_consorcio, activo) VALUES 
('1A', '1', 35.0000, 1, TRUE),
('1B', '1', 35.0000, 1, TRUE),
('2A', '2', 30.0000, 1, TRUE);

-- Insertar Personas (Propietarios e Inquilinos)
INSERT INTO persona (tipo, nombre, apellido, dni, telefono, email, activo) VALUES 
('PROPIETARIO', 'Juan', 'Pérez', '12345678', '11-5555-1234', 'juan@email.com', TRUE),
('INQUILINO', 'María', 'Gómez', '87654321', '11-4444-5678', 'maria@email.com', TRUE),
('PROPIETARIO', 'Carlos', 'López', '11223344', '11-3333-9012', 'carlos@email.com', TRUE);

-- Vincular Personas con Unidades Funcionales
INSERT INTO persona_unidad (id_persona, id_unidad, fecha_desde) VALUES 
(1, 1, '2020-01-01'), -- Juan es propietario del 1A
(2, 2, '2023-05-01'), -- María alquila el 1B
(3, 3, '2015-08-15'); -- Carlos es propietario del 2A

-- Insertar Proveedor
INSERT INTO proveedor (nombre, cuit) VALUES ('Edesur S.A.', '30-98765432-1');

-- Insertar Gasto Ordinario (Luz Espacios Comunes)
INSERT INTO gasto (id_consorcio, periodo, categoria, descripcion, monto, nro_factura, id_proveedor)
VALUES (1, '2024-05-01', 'ORDINARIO', 'Factura Luz - Espacios Comunes', 150000.00, 'A-0001-00054321', 1);

-- Insertar Liquidación Mensual
INSERT INTO liquidacion (id_consorcio, periodo, total_ordinario, total_extraordinario, fecha_cierre, cerrada)
VALUES (1, '2024-05-01', 150000.00, 0.00, '2024-05-28 10:00:00', TRUE);

-- Insertar Detalle de Liquidación (Prorrateo)
-- 1A (35%) de $150,000 = $52,500
-- 1B (35%) de $150,000 = $52,500
-- 2A (30%) de $150,000 = $45,000
INSERT INTO liquidacion_detalle (id_liquidacion, id_unidad, expensa_ordinaria, expensa_extraordinaria, total_a_pagar, saldo_deudor) VALUES 
(1, 1, 52500.00, 0.00, 52500.00, 52500.00),
(1, 2, 52500.00, 0.00, 52500.00, 52500.00),
(1, 3, 45000.00, 0.00, 45000.00, 45000.00);

