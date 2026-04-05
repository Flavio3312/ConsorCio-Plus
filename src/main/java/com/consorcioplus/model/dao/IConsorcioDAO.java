package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Consorcio;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para la entidad Consorcio. */
public interface IConsorcioDAO {
    List<Consorcio>     findAllActivos()       throws SQLException;
    Optional<Consorcio> findById(int id)       throws SQLException;
    void                save(Consorcio c)      throws SQLException;
    void                update(Consorcio c)    throws SQLException;
    void                softDelete(int id)     throws SQLException;
}
