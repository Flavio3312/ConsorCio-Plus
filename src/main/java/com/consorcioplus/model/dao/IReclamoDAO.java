package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Reclamo;
import com.consorcioplus.model.entity.Reclamo.EstadoReclamo;
import java.sql.SQLException;
import java.util.List;

/**
 * IReclamoDAO — interfaz (contrato) para el acceso a datos de Reclamo.
 *
 * Concepto Java del Módulo 3:
 *  - Interface: abstracción que desacopla la lógica de negocio de la BD.
 *  - El Controller solo conoce esta interfaz, no PagoDAOImpl.
 */
public interface IReclamoDAO {

    /**
     * Inserta un nuevo reclamo en la base de datos.
     * Asigna el id generado automáticamente al objeto pasado por parámetro.
     *
     * @param reclamo el Reclamo a persistir
     * @throws SQLException si ocurre un error de base de datos
     */
    void insertar(Reclamo reclamo) throws SQLException;

    /**
     * Actualiza el estado de un reclamo existente.
     * Si el nuevo estado es RESUELTO, también persiste la fecha de resolución.
     *
     * @param idReclamo    identificador del reclamo
     * @param nuevoEstado  el nuevo estado (EN_CURSO, RESUELTO, DESCARTADO)
     * @throws SQLException si ocurre un error de base de datos
     */
    void actualizarEstado(int idReclamo, EstadoReclamo nuevoEstado) throws SQLException;

    /**
     * Retorna todos los reclamos de un consorcio ordenados por fecha_alta ascendente (FIFO).
     *
     * @param idConsorcio identificador del consorcio
     * @return lista de reclamos (puede estar vacía, nunca null)
     */
    List<Reclamo> findByConsorcio(int idConsorcio) throws SQLException;

    /**
     * Retorna solo los reclamos activos (PENDIENTE o EN_CURSO) de un consorcio.
     *
     * @param idConsorcio identificador del consorcio
     * @return lista de reclamos activos, ordenados por fecha_alta (FIFO)
     */
    List<Reclamo> findActivosByConsorcio(int idConsorcio) throws SQLException;

    /**
     * Busca un reclamo por su número único.
     *
     * @param nroReclamo el número de reclamo (ej: "REC-2026-0042")
     * @return el Reclamo, o null si no existe
     */
    Reclamo findByNroReclamo(String nroReclamo) throws SQLException;
}
