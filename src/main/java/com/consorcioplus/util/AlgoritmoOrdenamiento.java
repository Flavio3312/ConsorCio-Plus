package com.consorcioplus.util;

import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.model.entity.Reclamo;

import java.math.BigDecimal;
import java.util.List;

/**
 * AlgoritmoOrdenamiento — clase utilitaria con los algoritmos de ordenación
 * y búsqueda aplicados en el Módulo 3 de ConsorCio+.
 *
 * Los métodos son ESTÁTICOS para poder usarlos sin instanciar la clase.
 *
 * Conceptos Java del Módulo 3:
 *  - BubbleSort    O(n²)       — educativo, listas pequeñas
 *  - InsertionSort O(n) / O(n²)— eficiente para listas casi ordenadas
 *  - QuickSort     O(n log n)  — eficiente para grandes volúmenes
 *  - Búsqueda lineal  O(n)     — sin orden previo
 *  - Búsqueda binaria O(log n) — requiere lista ordenada
 */
public final class AlgoritmoOrdenamiento {

    private AlgoritmoOrdenamiento() {} // clase utilitaria, no instanciable

    // =====================================================================
    // BUBBLE SORT — Ordenar UFs por Deuda (educativo)
    // Complejidad: O(n²) — no recomendado para producción con >100 elementos
    // =====================================================================

    /**
     * Ordena la lista de detalles de mayor a menor saldo_deudor usando BubbleSort.
     * Contiene una optimización: si en un pasada no hubo intercambios, la lista
     * ya está ordenada y el algoritmo termina antes.
     *
     * @param detalles lista a ordenar (se modifica in-place)
     */
    public static void bubbleSortPorDeuda(List<LiquidacionDetalle> detalles) {
        int n = detalles.size();
        for (int i = 0; i < n - 1; i++) {
            boolean intercambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                // Comparación: queremos mayor deuda primero (orden DESCENDENTE)
                if (detalles.get(j).getSaldoDeudor()
                             .compareTo(detalles.get(j + 1).getSaldoDeudor()) < 0) {
                    swap(detalles, j, j + 1);
                    intercambio = true;
                }
            }
            if (!intercambio) break; // ya ordenada — O(n) mejor caso
        }
    }

    // =====================================================================
    // INSERTION SORT — Ordenar Gastos/Reclamos Cronológicamente
    // Complejidad: O(n) mejor caso (ya ordenada), O(n²) peor caso
    // =====================================================================

    /**
     * Ordena los reclamos del más reciente al más antiguo (descendente por fechaAlta).
     * Eficiente cuando los reclamos vienen casi ordenados de la BD.
     *
     * @param reclamos lista a ordenar (in-place)
     */
    public static void insertionSortReclamosPorFecha(List<Reclamo> reclamos) {
        for (int i = 1; i < reclamos.size(); i++) {
            Reclamo clave = reclamos.get(i);
            int j = i - 1;
            // Desplaza hacia la derecha los reclamos más antiguos que la clave
            while (j >= 0 && reclamos.get(j).getFechaAlta()
                                           .isBefore(clave.getFechaAlta())) {
                reclamos.set(j + 1, reclamos.get(j));
                j--;
            }
            reclamos.set(j + 1, clave);
        }
    }

    // =====================================================================
    // QUICK SORT — Ordenar Reporte de Deudores (producción)
    // Complejidad: O(n log n) promedio, O(n²) peor caso (pivot ya ordenado)
    // =====================================================================

    /**
     * Ordena la lista de detalles de mayor a menor saldo_deudor usando QuickSort.
     * Recomendado para listas de más de 20 elementos.
     *
     * @param detalles lista a ordenar (in-place)
     */
    public static void quickSortPorDeuda(List<LiquidacionDetalle> detalles) {
        if (detalles == null || detalles.size() <= 1) return;
        quickSort(detalles, 0, detalles.size() - 1);
    }

    private static void quickSort(List<LiquidacionDetalle> lista, int inicio, int fin) {
        if (inicio < fin) {
            int pivotIdx = particionar(lista, inicio, fin);
            quickSort(lista, inicio, pivotIdx - 1); // subarreglo izquierdo
            quickSort(lista, pivotIdx + 1, fin);    // subarreglo derecho
        }
    }

    private static int particionar(List<LiquidacionDetalle> lista, int inicio, int fin) {
        BigDecimal pivot = lista.get(fin).getSaldoDeudor(); // pivot = último elemento
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            // Orden DESCENDENTE: mayor deuda primero
            if (lista.get(j).getSaldoDeudor().compareTo(pivot) >= 0) {
                i++;
                swap(lista, i, j);
            }
        }
        swap(lista, i + 1, fin);
        return i + 1;
    }

    // =====================================================================
    // BÚSQUEDA LINEAL — O(n)
    // No requiere que la lista esté ordenada
    // =====================================================================

    /**
     * Busca un reclamo por su número único recorriendo la lista secuencialmente.
     * Complejidad: O(n).
     *
     * @param reclamos   lista de reclamos donde buscar
     * @param nroReclamo número de reclamo objetivo (ej: "REC-2026-0042")
     * @return el Reclamo encontrado, o null si no existe
     */
    public static Reclamo busquedaLinealReclamo(List<Reclamo> reclamos, String nroReclamo) {
        for (Reclamo r : reclamos) {
            if (r.getNroReclamo().equalsIgnoreCase(nroReclamo)) {
                return r; // encontrado
            }
        }
        return null; // no encontrado
    }

    // =====================================================================
    // BÚSQUEDA BINARIA — O(log n)
    // PRECONDICIÓN: la lista DEBE estar ordenada por numero de unidad
    // =====================================================================

    /**
     * Busca un LiquidacionDetalle por número de unidad usando búsqueda binaria.
     * PRECONDICIÓN: la lista debe estar ordenada por {@code numeroUnidad} ascendente.
     * Complejidad: O(log n).
     *
     * @param detalles     lista ORDENADA de LiquidacionDetalle
     * @param numeroUnidad número de UF objetivo (ej: "4B")
     * @return el LiquidacionDetalle encontrado, o null si no existe
     */
    public static LiquidacionDetalle busquedaBinariaDetalleByUnidad(
            List<LiquidacionDetalle> detalles, String numeroUnidad) {
        int izquierda = 0;
        int derecha   = detalles.size() - 1;

        while (izquierda <= derecha) {
            int medio       = izquierda + (derecha - izquierda) / 2;
            int comparacion = detalles.get(medio).getNumeroUnidad()
                    .compareToIgnoreCase(numeroUnidad);

            if (comparacion == 0) {
                return detalles.get(medio); // ENCONTRADO
            } else if (comparacion < 0) {
                izquierda = medio + 1;      // buscar en la mitad derecha
            } else {
                derecha = medio - 1;        // buscar en la mitad izquierda
            }
        }
        return null; // no encontrado
    }

    // =====================================================================
    // Helper privado
    // =====================================================================

    /** Intercambia dos elementos en una lista (operación de swap). */
    private static void swap(List<LiquidacionDetalle> lista, int i, int j) {
        LiquidacionDetalle temp = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, temp);
    }
}
