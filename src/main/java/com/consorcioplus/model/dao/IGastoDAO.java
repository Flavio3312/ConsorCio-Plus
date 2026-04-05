package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Gasto;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Contrato de acceso a datos para Gasto. */
public interface IGastoDAO {
    List<Gasto> findByConsorcioAndPeriodo(int idConsorcio, LocalDate periodo) throws SQLException;
    void        save(Gasto g)                                                  throws SQLException;
    void        delete(int id)                                                 throws SQLException;
    boolean     existePeriodoCerrado(int idConsorcio, LocalDate periodo)       throws SQLException;
}
