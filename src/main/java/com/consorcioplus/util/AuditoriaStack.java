package com.consorcioplus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * AuditoriaStack — historial LIFO de operaciones del sistema.
 *
 * Conceptos Java del Módulo 3:
 *  - Stack (Pila): estructura LIFO — el último elemento agregado es el primero
 *    en ser consultado. Implementado con {@code ArrayDeque} (preferido sobre
 *    {@code java.util.Stack} que hereda de Vector).
 *  - Patrón Singleton: una única instancia compartida por toda la aplicación.
 *  - Clase interna estática inmutable: {@code AccionAuditoria} encapsula cada
 *    registro sin permitir modificaciones posteriores (todos los campos son {@code final}).
 *
 * <p>Capacidad máxima: {@value #MAX_ACCIONES} acciones.
 * Al superarla, la acción más antigua (la del fondo de la pila) se descarta.
 */
public class AuditoriaStack {

    private static final int MAX_ACCIONES = 100;

    // Singleton — instancia única
    private static AuditoriaStack instance;

    // La pila: ArrayDeque como stack (push/pop/peek desde el frente)
    private final Deque<AccionAuditoria> pila = new ArrayDeque<>();

    private AuditoriaStack() {}

    /** Retorna la instancia única del registro de auditoría. */
    public static synchronized AuditoriaStack getInstance() {
        if (instance == null) {
            instance = new AuditoriaStack();
        }
        return instance;
    }

    // =====================================================================
    // Registro inmutable de una acción de auditoría
    // =====================================================================
    /**
     * Registro de una acción: todos los campos son finales (inmutabilidad).
     */
    public static class AccionAuditoria {
        private final String        tipoAccion;   // "ALTA_GASTO", "PAGO_RECIBIDO"...
        private final String        descripcion;
        private final String        usuario;
        private final LocalDateTime timestamp;

        public AccionAuditoria(String tipoAccion, String descripcion, String usuario) {
            this.tipoAccion  = tipoAccion;
            this.descripcion = descripcion;
            this.usuario     = usuario;
            this.timestamp   = LocalDateTime.now();
        }

        public String        getTipoAccion()  { return tipoAccion; }
        public String        getDescripcion() { return descripcion; }
        public String        getUsuario()     { return usuario; }
        public LocalDateTime getTimestamp()   { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s — por %s",
                    timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    tipoAccion, descripcion, usuario);
        }
    }

    // =====================================================================
    // Operaciones de pila
    // =====================================================================

    /**
     * PUSH — Registra una nueva acción en el tope de la pila.
     * Si se alcanza la capacidad máxima, descarta la acción más antigua
     * (la del fondo).
     *
     * @param tipoAccion  código de la acción (ej: "PAGO_RECIBIDO")
     * @param descripcion descripción legible del evento
     */
    public synchronized void registrar(String tipoAccion, String descripcion) {
        String usuario = "SISTEMA";
        if (SessionManager.getInstance().getUsuarioActual() != null) {
            usuario = SessionManager.getInstance().getUsuarioActual().getUsername();
        }
        AccionAuditoria accion = new AccionAuditoria(tipoAccion, descripcion, usuario);
        if (pila.size() >= MAX_ACCIONES) {
            pila.removeLast(); // descarta la acción más antigua
        }
        pila.push(accion); // agrega al tope (LIFO)
    }

    /**
     * PEEK — Retorna la última acción registrada SIN extraerla de la pila.
     *
     * @return la acción más reciente, o null si la pila está vacía
     */
    public AccionAuditoria verUltima() {
        return pila.isEmpty() ? null : pila.peek();
    }

    /**
     * POP — Extrae y retorna la última acción (la elimina de la pila).
     * Puede usarse para implementar la funcionalidad de "deshacer".
     *
     * @return la acción más reciente extraída, o null si la pila está vacía
     */
    public AccionAuditoria deshacer() {
        return pila.isEmpty() ? null : pila.pop();
    }

    /**
     * Retorna las últimas {@code n} acciones en orden cronológico inverso
     * (la más reciente en la posición 0 de la lista).
     *
     * @param n número de acciones a retornar
     * @return lista con las últimas n acciones
     */
    public List<AccionAuditoria> obtenerUltimas(int n) {
        List<AccionAuditoria> resultado = new ArrayList<>();
        int count = 0;
        for (AccionAuditoria a : pila) { // itera desde el tope
            if (count++ >= n) break;
            resultado.add(a);
        }
        return resultado;
    }

    /**
     * Retorna todas las acciones registradas (de la más reciente a la más antigua).
     */
    public List<AccionAuditoria> obtenerTodas() {
        return new ArrayList<>(pila);
    }

    /** Cantidad de acciones registradas actualmente. */
    public int size() { return pila.size(); }

    /** Retorna true si no hay ninguna acción registrada. */
    public boolean isEmpty() { return pila.isEmpty(); }
}
