package com.consorcioplus.util;

import com.consorcioplus.controller.ReclamoController;
import com.consorcioplus.model.entity.Reclamo;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ColaReclamos — cola FIFO de reclamos entrantes.
 *
 * Conceptos Java del Módulo 3:
 *  - Queue (Cola): estructura FIFO — el primer reclamo en entrar es el primero
 *    en ser atendido, garantizando equidad entre consorcistas.
 *  - LinkedList como implementación de Queue (nodos doblemente enlazados,
 *    eficiente para inserción al final y extracción al frente).
 *  - Patrón Singleton: una única cola compartida por toda la aplicación.
 *
 * Operaciones:
 *  - OFFER (encolar): agrega al final de la cola     → O(1)
 *  - POLL  (extraer): extrae el primero de la cola   → O(1)
 *  - PEEK  (consultar): ve el primero sin extraerlo  → O(1)
 */
public class ColaReclamos {

    // Singleton
    private static ColaReclamos instance;

    // Cola FIFO implementada con LinkedList
    private final Queue<Reclamo> colaEspera = new LinkedList<>();

    private ColaReclamos() {}

    /** Retorna la instancia única de la cola de reclamos. */
    public static synchronized ColaReclamos getInstance() {
        if (instance == null) {
            instance = new ColaReclamos();
        }
        return instance;
    }

    // =====================================================================
    // Operaciones de cola
    // =====================================================================

    /**
     * OFFER — Encola un nuevo reclamo al final de la cola.
     * Los reclamos se atienden en orden de llegada (FIFO).
     *
     * @param reclamo el reclamo a encolar
     * @return true si se encola exitosamente
     */
    public boolean encolar(Reclamo reclamo) {
        if (reclamo == null) return false;
        return colaEspera.offer(reclamo); // offer no lanza excepción si falla
    }

    /**
     * POLL — Extrae y retorna el reclamo más antiguo (el que llegó primero).
     * Retorna null si la cola está vacía.
     */
    public Reclamo atenderSiguiente() {
        return colaEspera.poll(); // null si vacía (no lanza excepción)
    }

    /**
     * PEEK — Consulta el próximo reclamo a atender SIN extraerlo de la cola.
     * Retorna null si la cola está vacía.
     */
    public Reclamo consultarSiguiente() {
        return colaEspera.peek();
    }

    /**
     * Retorna la cantidad de reclamos actualmente en espera de atención.
     */
    public int cantidadPendientes() {
        return colaEspera.size();
    }

    /** Retorna true si la cola no tiene reclamos pendientes. */
    public boolean estaVacia() {
        return colaEspera.isEmpty();
    }

    /**
     * Procesa TODOS los reclamos en cola, pasándolos al ReclamoController
     * para que se transicionen a EN_CURSO.
     *
     * @param controller instancia del ReclamoController para aplicar la lógica
     */
    public void procesarTodos(ReclamoController controller) {
        while (!estaVacia()) {
            Reclamo r = atenderSiguiente();
            if (r != null) {
                try {
                    controller.pasarAEnCurso(r);
                } catch (Exception e) {
                    System.err.println("Error procesando reclamo " + r.getNroReclamo()
                            + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Vacía la cola sin procesar los reclamos.
     * Uso: reinicio de sesión o limpieza de estado.
     */
    public void vaciar() {
        colaEspera.clear();
    }
}
