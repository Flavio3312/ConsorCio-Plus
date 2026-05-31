package com.consorcioplus.controller;

import com.consorcioplus.model.dao.ILiquidacionDAO;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.model.entity.Reclamo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReporteController — genera los reportes del sistema ConsorCio+.
 *
 * Conceptos Java del Módulo 3:
 *  - ArrayList: colecciones ordenables en memoria para los reportes.
 *  - BubbleSort:    educativo — UFs por deuda (listas pequeñas)   O(n²)
 *  - InsertionSort: eficiente para casi-ordenadas               O(n) / O(n²)
 *  - QuickSort:     eficiente para grandes volúmenes            O(n log n)
 *  - Búsqueda lineal:  O(n) — reclamo por número de referencia
 *  - Búsqueda binaria: O(log n) — sobre lista pre-ordenada
 */
public class ReporteController {

    // =====================================================================
    // REPORTE 1 — Estado de Deuda
    // Retorna las UFs con saldo deudor > 0, ordenadas de mayor a menor deuda
    // =====================================================================

    /**
     * Ordena la lista de detalles de mayor a menor saldo_deudor usando QuickSort.
     * Complejidad promedio: O(n log n) — adecuado para producción.
     *
     * @param detalles lista de LiquidacionDetalle a ordenar (se ordena in-place)
     */
    public void ordenarPorDeudaQuickSort(List<LiquidacionDetalle> detalles) {
        if (detalles == null || detalles.size() <= 1) return;
        quickSort(detalles, 0, detalles.size() - 1);
    }

    private void quickSort(List<LiquidacionDetalle> lista, int inicio, int fin) {
        if (inicio < fin) {
            int pivot = particionar(lista, inicio, fin);
            quickSort(lista, inicio, pivot - 1);
            quickSort(lista, pivot + 1, fin);
        }
    }

    private int particionar(List<LiquidacionDetalle> lista, int inicio, int fin) {
        BigDecimal pivot = lista.get(fin).getSaldoDeudor();
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            // Orden descendente: mayor deuda primero
            if (lista.get(j).getSaldoDeudor().compareTo(pivot) >= 0) {
                i++;
                LiquidacionDetalle temp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, temp);
            }
        }
        LiquidacionDetalle temp = lista.get(i + 1);
        lista.set(i + 1, lista.get(fin));
        lista.set(fin, temp);
        return i + 1;
    }

    // =====================================================================
    // REPORTE 2 — Historial de Reclamos
    // Ordena reclamos del más reciente al más antiguo usando InsertionSort
    // =====================================================================

    /**
     * Ordena la lista de reclamos por fechaAlta descendente (más reciente primero).
     * InsertionSort es eficiente cuando la lista ya viene casi ordenada de la BD.
     * Complejidad: O(n) mejor caso, O(n²) peor caso.
     *
     * @param reclamos lista de Reclamo a ordenar (se ordena in-place)
     */
    public void ordenarReclamosPorFechaInsertionSort(List<Reclamo> reclamos) {
        for (int i = 1; i < reclamos.size(); i++) {
            Reclamo clave = reclamos.get(i);
            int j = i - 1;
            // Compara fechas: desplaza hacia la derecha si la fecha es anterior
            while (j >= 0 && reclamos.get(j).getFechaAlta()
                    .isBefore(clave.getFechaAlta())) {
                reclamos.set(j + 1, reclamos.get(j));
                j--;
            }
            reclamos.set(j + 1, clave);
        }
    }

    // =====================================================================
    // REPORTE 3 — Top Deudores (educativo)
    // BubbleSort sobre lista de detalles por saldo deudor
    // =====================================================================

    /**
     * Ordena los detalles de mayor a menor saldo_deudor usando BubbleSort.
     * Complejidad: O(n²) — se usa con fines educativos para listas pequeñas.
     *
     * @param detalles lista de LiquidacionDetalle a ordenar (in-place)
     */
    public void ordenarPorDeudaBubbleSort(List<LiquidacionDetalle> detalles) {
        int n = detalles.size();
        for (int i = 0; i < n - 1; i++) {
            boolean intercambio = false;
            for (int j = 0; j < n - 1 - i; j++) {
                // Orden descendente: si el actual < siguiente, intercambia
                if (detalles.get(j).getSaldoDeudor()
                        .compareTo(detalles.get(j + 1).getSaldoDeudor()) < 0) {
                    LiquidacionDetalle temp = detalles.get(j);
                    detalles.set(j, detalles.get(j + 1));
                    detalles.set(j + 1, temp);
                    intercambio = true;
                }
            }
            // Optimización: si no hubo intercambios, la lista ya está ordenada
            if (!intercambio) break;
        }
    }

    // =====================================================================
    // BÚSQUEDA LINEAL — O(n)
    // Busca un reclamo por número único (sin requerir orden previo)
    // =====================================================================

    /**
     * Búsqueda lineal de un reclamo por su número único.
     * Complejidad: O(n) — recorre la lista secuencialmente.
     *
     * @param reclamos   lista de reclamos donde buscar
     * @param nroReclamo número de reclamo objetivo (ej: "REC-2026-0042")
     * @return el Reclamo encontrado, o null si no existe
     */
    public Reclamo busquedaLinealReclamo(List<Reclamo> reclamos, String nroReclamo) {
        for (Reclamo r : reclamos) {
            if (r.getNroReclamo().equalsIgnoreCase(nroReclamo)) {
                return r; // encontrado
            }
        }
        return null; // no encontrado
    }

    // =====================================================================
    // BÚSQUEDA BINARIA — O(log n)
    // Busca un detalle de liquidación por número de unidad en lista ORDENADA
    // =====================================================================

    /**
     * Búsqueda binaria de un LiquidacionDetalle por número de unidad.
     * PRECONDICIÓN: la lista debe estar ordenada por numeroUnidad (ascendente).
     * Complejidad: O(log n) — mucho más eficiente que lineal para listas grandes.
     *
     * @param detalles      lista ORDENADA de LiquidacionDetalle
     * @param numeroUnidad  número de UF objetivo (ej: "4B")
     * @return el LiquidacionDetalle encontrado, o null si no existe
     */
    public LiquidacionDetalle busquedaBinariaDetalleByUnidad(
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
    // REPORTE BALANCE FINANCIERO — Agregación con Stream API
    // =====================================================================

    /**
     * Calcula el total general de saldo deudor de todos los detalles.
     * Usa Stream API y reduce para agregar BigDecimals.
     *
     * @param detalles lista de detalles de liquidación
     * @return suma total de saldo_deudor
     */
    public BigDecimal calcularDeudaTotal(List<LiquidacionDetalle> detalles) {
        return detalles.stream()
                .map(LiquidacionDetalle::getSaldoDeudor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Filtra los detalles con saldo deudor mayor a cero (UFs morosas).
     *
     * @param detalles lista de todos los detalles
     * @return nueva lista con solo las UFs con deuda pendiente
     */
    public List<LiquidacionDetalle> filtrarDeudores(List<LiquidacionDetalle> detalles) {
        List<LiquidacionDetalle> deudores = new ArrayList<>();
        for (LiquidacionDetalle d : detalles) {
            if (d.getSaldoDeudor().compareTo(BigDecimal.ZERO) > 0) {
                deudores.add(d);
            }
        }
        return deudores;
    }
}
