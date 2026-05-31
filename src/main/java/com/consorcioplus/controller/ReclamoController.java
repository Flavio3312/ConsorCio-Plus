package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IReclamoDAO;
import com.consorcioplus.model.dao.impl.ReclamoDAOImpl;
import com.consorcioplus.model.entity.Reclamo;
import com.consorcioplus.model.entity.Reclamo.EstadoReclamo;
import com.consorcioplus.util.AuditoriaStack;
import com.consorcioplus.util.ColaReclamos;
import com.consorcioplus.util.SessionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * ReclamoController — lógica de negocio para la gestión de reclamos.
 *
 * Conceptos Java del Módulo 3:
 *  - switch: controla las transiciones válidas entre estados del reclamo.
 *  - ColaReclamos (Queue FIFO): los reclamos nuevos se encolan para atención
 *    ordenada (el más antiguo se atiende primero).
 *  - Excepciones de negocio: TransicionEstadoInvalidaException evita
 *    cambios de estado inválidos.
 */
public class ReclamoController {

    private final IReclamoDAO    reclamoDAO;
    private final ColaReclamos   cola;
    private final AuditoriaStack auditoria;

    public ReclamoController() {
        this.reclamoDAO = new ReclamoDAOImpl();
        this.cola       = ColaReclamos.getInstance();
        this.auditoria  = AuditoriaStack.getInstance();
    }

    // -------------------------------------------------------
    // Alta de reclamo
    // -------------------------------------------------------
    /**
     * Registra un nuevo reclamo en la BD y lo encola para atención FIFO.
     *
     * @param idUnidad    id de la unidad funcional que genera el reclamo
     * @param descripcion detalle del problema reportado
     * @return el Reclamo persistido con su nroReclamo asignado
     */
    public Reclamo altaReclamo(int idUnidad, String descripcion) throws SQLException {
        int idUsuario = SessionManager.getInstance().getUsuarioActual() != null
                ? SessionManager.getInstance().getUsuarioActual().getId()
                : 0;

        Reclamo nuevo = new Reclamo(idUnidad, descripcion, idUsuario);
        reclamoDAO.insertar(nuevo);
        cola.encolar(nuevo); // se agrega al final de la cola FIFO

        auditoria.registrar("ALTA_RECLAMO",
                nuevo.getNroReclamo() + " — UF#" + idUnidad
                        + " — " + descripcion);
        return nuevo;
    }

    // -------------------------------------------------------
    // Transición de estado (switch con validación de negocio)
    // -------------------------------------------------------
    /**
     * Intenta cambiar el estado de un reclamo.
     * El switch valida que la transición sea válida según la máquina de estados:
     *   PENDIENTE → EN_CURSO | DESCARTADO
     *   EN_CURSO  → RESUELTO | DESCARTADO
     *   RESUELTO / DESCARTADO → (estados finales, sin cambio posible)
     *
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarEstado(Reclamo reclamo, EstadoReclamo nuevoEstado)
            throws SQLException {
        switch (reclamo.getEstado()) {
            case PENDIENTE:
                // Desde PENDIENTE solo puede pasar a EN_CURSO o DESCARTADO
                if (nuevoEstado == EstadoReclamo.EN_CURSO
                        || nuevoEstado == EstadoReclamo.DESCARTADO) {
                    aplicarCambio(reclamo, nuevoEstado);
                } else {
                    throw new IllegalStateException(
                            "Transición inválida: PENDIENTE → " + nuevoEstado
                                    + ". Valores permitidos: EN_CURSO, DESCARTADO.");
                }
                break;

            case EN_CURSO:
                // Desde EN_CURSO puede ir a RESUELTO o DESCARTADO
                if (nuevoEstado == EstadoReclamo.RESUELTO
                        || nuevoEstado == EstadoReclamo.DESCARTADO) {
                    aplicarCambio(reclamo, nuevoEstado);
                } else {
                    throw new IllegalStateException(
                            "Transición inválida: EN_CURSO → " + nuevoEstado
                                    + ". Valores permitidos: RESUELTO, DESCARTADO.");
                }
                break;

            case RESUELTO:
            case DESCARTADO:
                throw new IllegalStateException(
                        "El reclamo " + reclamo.getNroReclamo()
                                + " ya está en estado final (" + reclamo.getEstado() + ").");
        }
    }

    /** Atajo para pasar un reclamo PENDIENTE a EN_CURSO. */
    public void pasarAEnCurso(Reclamo reclamo) throws SQLException {
        cambiarEstado(reclamo, EstadoReclamo.EN_CURSO);
    }

    /** Atajo para resolver un reclamo EN_CURSO. */
    public void resolver(Reclamo reclamo) throws SQLException {
        cambiarEstado(reclamo, EstadoReclamo.RESUELTO);
    }

    /** Atajo para descartar un reclamo. */
    public void descartar(Reclamo reclamo) throws SQLException {
        cambiarEstado(reclamo, EstadoReclamo.DESCARTADO);
    }

    // -------------------------------------------------------
    // Consultas
    // -------------------------------------------------------

    /**
     * Retorna todos los reclamos de un consorcio ordenados por fecha (FIFO).
     */
    public List<Reclamo> obtenerTodos(int idConsorcio) throws SQLException {
        return reclamoDAO.findByConsorcio(idConsorcio);
    }

    /**
     * Retorna solo los reclamos activos (PENDIENTE | EN_CURSO).
     */
    public List<Reclamo> obtenerActivos(int idConsorcio) throws SQLException {
        return reclamoDAO.findActivosByConsorcio(idConsorcio);
    }

    /**
     * Procesa el siguiente reclamo de la cola FIFO (lo pasa a EN_CURSO).
     * Retorna null si la cola está vacía.
     */
    public Reclamo atenderSiguiente() throws SQLException {
        Reclamo siguiente = cola.atenderSiguiente();
        if (siguiente != null) {
            pasarAEnCurso(siguiente);
        }
        return siguiente;
    }

    /**
     * Cantidad de reclamos pendientes en la cola de atención.
     */
    public int cantidadEnCola() {
        return cola.cantidadPendientes();
    }

    // -------------------------------------------------------
    // Privado — aplica el cambio de estado en objeto y BD
    // -------------------------------------------------------
    private void aplicarCambio(Reclamo reclamo, EstadoReclamo nuevoEstado)
            throws SQLException {
        reclamo.setEstado(nuevoEstado); // actualiza el objeto en memoria
        reclamoDAO.actualizarEstado(reclamo.getId(), nuevoEstado); // persiste
        auditoria.registrar("ESTADO_RECLAMO",
                reclamo.getNroReclamo() + " → " + nuevoEstado);
    }
}
