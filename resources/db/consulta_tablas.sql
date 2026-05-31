
-- ==========================================
-- 2. PRESENTACIÓN DE CONSULTAS SQL (SELECT)
-- ==========================================

-- Consulta A: Obtener el detalle de unidades funcionales y sus porcentuales de un consorcio específico
SELECT c.nombre AS Consorcio, uf.numero AS Unidad, uf.piso AS Piso, uf.porcentual AS Porcentual
FROM unidad_funcional uf
INNER JOIN consorcio c ON uf.id_consorcio = c.id
WHERE c.id = 1 AND uf.activo = TRUE
ORDER BY uf.numero;

-- Consulta B: Obtener el listado de gastos registrados en un período específico para un edificio
SELECT g.periodo, g.categoria, g.descripcion, g.monto, p.nombre AS Proveedor
FROM gasto g
LEFT JOIN proveedor p ON g.id_proveedor = p.id
WHERE g.id_consorcio = 1 AND g.periodo = '2024-05-01'
ORDER BY g.categoria, g.monto DESC;

-- Consulta C: Generar el informe de deuda (quiénes deben expensas y cuánto) cruzando persona, unidad y liquidación
SELECT uf.numero AS Unidad, 
       CONCAT(p.nombre, ' ', p.apellido) AS Responsable, 
       p.tipo AS Tipo,
       ld.saldo_deudor AS Saldo_Pendiente
FROM liquidacion_detalle ld
INNER JOIN unidad_funcional uf ON ld.id_unidad = uf.id
INNER JOIN persona_unidad pu ON uf.id = pu.id_unidad
INNER JOIN persona p ON pu.id_persona = p.id
WHERE ld.id_liquidacion = 1 AND ld.saldo_deudor > 0 AND pu.fecha_hasta IS NULL;


-- ==========================================
-- 3. ACTUALIZACIÓN DE REGISTROS (UPDATE)
-- ==========================================

-- Simulación de Pago: Actualizamos el saldo deudor de la UF 1 (Pago total de $52,500)
UPDATE liquidacion_detalle
SET saldo_deudor = saldo_deudor - 52500.00
WHERE id = 1;

-- Insertar el registro de ese pago
INSERT INTO pago (id_liq_detalle, monto_pagado, nro_recibo, id_usuario)
VALUES (1, 52500.00, 'RC-0001-00001', 1);

-- Modificar el estado de un consorcio para inactivarlo (Baja Lógica)
UPDATE consorcio 
SET activo = FALSE 
WHERE id = 1;

