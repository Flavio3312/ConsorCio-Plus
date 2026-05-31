-- ==============================================================================
-- ConsorCio+ — Script de MIGRACIÓN v1.0 → v2.0 (TP3)
-- Ejecutar sobre la base de datos existente: consorcioplus
-- Motor: MySQL 8.x / MariaDB 10.x — Puerto 3307
--
-- Este script agrega las columnas y tablas necesarias para los módulos
-- de Cobros, Reclamos y Reportes del Módulo 3 (TP3).
-- Es SEGURO ejecutarlo múltiples veces (usa IF NOT EXISTS / column checks).
-- ==============================================================================

USE consorcioplus;

-- ============================================================
-- 1. TABLA reclamo — agregar columna nro_reclamo
-- ============================================================
-- El código del TP3 genera un número de reclamo único (ej: REC-20260530-001)
-- Esta columna no existía en el schema del TP2.

ALTER TABLE reclamo
    ADD COLUMN IF NOT EXISTS nro_reclamo VARCHAR(40) UNIQUE
    COMMENT 'Número de reclamo generado automáticamente (ej: REC-20260530-001)';

-- Si la BD es MySQL < 8 y no soporta IF NOT EXISTS en ALTER, usar:
-- ALTER TABLE reclamo ADD COLUMN nro_reclamo VARCHAR(40) UNIQUE;
-- (comentar la línea de arriba y descomentar esta si da error)

-- ============================================================
-- 2. TABLA liquidacion_detalle — agregar columnas auxiliares
-- ============================================================
-- El código usa numero_unidad y piso_unidad como desnormalización
-- para mostrar datos en pantalla sin hacer JOIN adicionales.

ALTER TABLE liquidacion_detalle
    ADD COLUMN IF NOT EXISTS numero_unidad VARCHAR(10) DEFAULT NULL
    COMMENT 'Desnormalizado de unidad_funcional.numero para reportes';

ALTER TABLE liquidacion_detalle
    ADD COLUMN IF NOT EXISTS piso_unidad VARCHAR(5) DEFAULT NULL
    COMMENT 'Desnormalizado de unidad_funcional.piso para reportes';

ALTER TABLE liquidacion_detalle
    ADD COLUMN IF NOT EXISTS porcentual DECIMAL(7,4) DEFAULT NULL
    COMMENT 'Desnormalizado del porcentual de la UF al momento de liquidar';

-- ============================================================
-- 3. TABLA usuario — agregar columna created_at (schema v2)
-- ============================================================

ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT NOW()
    COMMENT 'Fecha de creación del usuario';

-- ============================================================
-- 4. ÍNDICES adicionales para mejor rendimiento
-- ============================================================

-- Índice en reclamo por estado (filtrado rápido de PENDIENTE / EN_CURSO)
CREATE INDEX IF NOT EXISTS idx_reclamo_estado ON reclamo(estado);

-- Índice en pago por detalle de liquidación
CREATE INDEX IF NOT EXISTS idx_pago_detalle ON pago(id_liq_detalle);

-- ============================================================
-- 5. VERIFICACIÓN — consulta el resultado
-- ============================================================
-- Ejecutar esto para verificar que las columnas se agregaron:
--
-- DESCRIBE reclamo;
-- DESCRIBE liquidacion_detalle;
--
-- Y para ver que hay datos:
-- SELECT COUNT(*) FROM reclamo;
-- SELECT COUNT(*) FROM liquidacion_detalle;

SELECT 'Migración v2.0 aplicada correctamente.' AS resultado;
