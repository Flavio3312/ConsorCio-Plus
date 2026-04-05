package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.UnidadFuncional;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para Unidad Funcional. */
public interface IUnidadFuncionalDAO {
    List<UnidadFuncional>     findByConsorcio(int idConsorcio)             throws SQLException;
    Optional<UnidadFuncional> findById(int id)                             throws SQLException;
    void                      save(UnidadFuncional uf)                     throws SQLException;
    void                      update(UnidadFuncional uf)                   throws SQLException;
    void                      softDelete(int id)                           throws SQLException;
    BigDecimal                sumPorcentualesByConsorcio(int idConsorcio)  throws SQLException;
}
