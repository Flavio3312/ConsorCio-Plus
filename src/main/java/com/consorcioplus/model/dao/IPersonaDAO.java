package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Persona;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para Persona (Propietario / Inquilino). */
public interface IPersonaDAO {
    List<Persona>     findAllActivos()    throws SQLException;
    Optional<Persona> findById(int id)    throws SQLException;
    void              save(Persona p)     throws SQLException;
    void              update(Persona p)   throws SQLException;
    void              softDelete(int id)  throws SQLException;
    void              vincularConUnidad(int idPersona, int idUnidad,
                                       java.time.LocalDate desde) throws SQLException;
}
