# ConsorCio+ — Manual de Usuario y Documentación Técnica (Módulo 3)

Este documento detalla las últimas actualizaciones realizadas en el sistema (Módulo 3) y proporciona instrucciones avanzadas para el uso de la aplicación.

---

## 🛠️ 1. Cambios Técnicos y Actualizaciones (UI a Base de Datos)

Se ha realizado una revisión integral y sincronización desde la interfaz gráfica hasta la base de datos para asegurar el correcto funcionamiento de los nuevos módulos correspondientes al TP3.

### 🖥️ Interfaz Gráfica (UI)
* **`MainFrame.java`**: Se agregaron los botones de acceso directo en el menú lateral para los tres nuevos módulos (`Cobros`, `Reclamos`, `Reportes`), junto con un separador visual para diferenciarlos de los módulos base.
* **`CobrosPanel.java`**: Se rediseñó para ser **auto-contenido**. Ahora incluye selectores en cascada (Consorcio → Liquidación → UF) en lugar de depender de la navegación desde el panel de Liquidaciones.
* **`ReclamosPanel.java`**: Se rediseñó para incluir un selector de Consorcio y un menú desplegable con las Unidades Funcionales (UFs) reales, evitando que el usuario deba memorizar e ingresar manualmente el ID de la UF.
* **`ReportesPanel.java`**: Se conectó a los datos reales de la base de datos (antes usaba datos "mockeados" de demostración). Ahora integra selectores de Consorcio y Liquidación, y los tres reportes (Deuda, Reclamos y Balance) se generan dinámicamente utilizando `QuickSort`, `InsertionSort` y `Stream API`.

### ⚙️ Lógica de Negocio (Controllers & Util)
* **`SessionManager.java` & Controllers**: Se corrigieron errores de compilación donde se invocaban métodos de instancia (`getUsuarioActual()`) como si fueran estáticos.
* **`LiquidacionDAOImpl.java`**: Se modificó la consulta `INSERT` en `saveDetalle()` para que guarde las columnas desnormalizadas (`numero_unidad`, `piso_unidad`, `porcentual`) y así preservar la "foto" histórica al momento de liquidar. También se actualizaron las consultas `SELECT` utilizando `COALESCE` para garantizar compatibilidad hacia atrás con liquidaciones antiguas.
* **`ColaReclamos.java`**: Se agregaron las importaciones faltantes para procesar correctamente la cola FIFO.

### 🗄️ Base de Datos (Tablas y Scripts)
* **Tabla `reclamo`**: Se detectó la falta de la columna `nro_reclamo` que utiliza el Módulo 3. Se agregó la columna `nro_reclamo VARCHAR(40) UNIQUE` y se creó un índice en el campo `estado` para optimizar las consultas de la cola FIFO.
* **Tabla `liquidacion_detalle`**: Se agregaron las columnas `numero_unidad`, `piso_unidad` y `porcentual` para almacenar los datos históricos al momento del cierre de la liquidación, evitando discrepancias si una UF cambia de porcentual en el futuro.
* **Tabla `usuario`**: Se agregó la columna `created_at` correspondiente a la versión 2.0 del esquema.
* **Sincronización de Scripts**: Se actualizaron `Creacion_Tablas.sql` (en TP1) y `schema.sql` (en TP3) para reflejar exactamente la misma estructura. Además, se creó un script de migración (`migracion_v2_TP3.sql`) que fue ejecutado en vivo en la base de datos local (puerto 3307).

---

## 📖 2. Manual de Uso e Instrucciones Avanzadas

### 📋 Módulo: Liquidaciones (Pre-requisito)
Para poder operar con los reportes financieros y los cobros, primero debes tener una liquidación ejecutada.
1. Ve al panel **Liquidaciones** en el menú izquierdo.
2. Selecciona un consorcio del combo superior y el mes a liquidar.
3. Presiona **▶ Ejecutar Liquidación**. 
4. *Regla de negocio (RD01)*: Si los porcentuales de las unidades de ese consorcio no suman exactamente 100%, el sistema te impedirá liquidar. Deberás corregirlos primero en el panel de **Unidades Funcionales**.

### 💳 Módulo: Cobros (Pagos Parciales y Totales)
Este módulo permite registrar pagos de expensas (Cobros) afectando el saldo deudor de cada unidad.
1. Ve al panel **Cobros**.
2. **Selecciona el Consorcio** y luego la **Liquidación** que ejecutaste previamente. Aparecerá la grilla con todas las UFs que tienen deuda.
3. Haz clic sobre una UF en la grilla. Se habilitará el panel izquierdo.
4. **Instrucción Avanzada (Cobro Parcial):** Ingresa un monto *menor* al total adeudado. Presiona **💳 Registrar Pago (auto)**. Verás que el recibo se genera solo (`REC-YYYYMMDD-XXXXX`), la deuda en la tabla disminuye y el pago aparece en el historial inferior.
5. **Instrucción Avanzada (Cobro Manual):** Si necesitas registrar un pago antiguo o de transferencia bancaria, ingresa el monto, escribe un número de comprobante en "Nro. Recibo (opcional)" y usa el botón **🖨 Con Nro. Recibo manual**.

### 🔧 Módulo: Reclamos (Atención FIFO)
Gestiona incidencias de mantenimiento utilizando una estructura de Cola (Queue).
1. Ve al panel **Reclamos**. Selecciona un consorcio para ver sus reclamos.
2. Haz clic en **➕ Nuevo Reclamo**. Elige la UF que reporta el problema y detalla la descripción. El reclamo ingresará con estado `PENDIENTE` (amarillo).
3. **Instrucción Avanzada (Cola FIFO):** Presiona **▶ Atender Siguiente (FIFO)**. El sistema tomará automáticamente el reclamo `PENDIENTE` *más antiguo* y lo pasará a `EN_CURSO` (celeste). Esto garantiza equidad (el primero en llegar es el primero en atenderse).
4. Para cerrar un reclamo, selecciónalo en la tabla y presiona **✅ Resolver** o **❌ Descartar**.

### 📊 Módulo: Reportes (Algoritmos)
Muestra visualizaciones de datos aplicando algoritmos de ordenamiento y Stream API.
1. Ve al panel **Reportes**. Selecciona un Consorcio y una Liquidación, y presiona **🔄 Actualizar**.
2. **Pestaña Estado de Deuda:** Muestra las UFs ordenadas desde la que debe *más* dinero a la que debe *menos*. Utiliza internamente el algoritmo **QuickSort** (O(n log n)).
3. **Pestaña Reclamos Activos:** Muestra los reclamos ordenados desde el más reciente al más antiguo. Utiliza internamente el algoritmo **InsertionSort**, ideal para listas casi ordenadas o de inserción continua.
4. **Pestaña Balance Financiero:** Muestra un resumen general calculando porcentajes de morosidad y totales financieros utilizando **Java Stream API** (operación `reduce()`).
