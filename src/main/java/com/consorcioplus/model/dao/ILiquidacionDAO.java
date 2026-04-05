package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para Liquidacion y sus detalles. */
public interface ILiquidacionDAO {
    Optional<Liquidacion>    findByConsorcioAndPeriodo(int idConsorcio, LocalDate periodo)  throws SQLException;
    List<Liquidacion>        findByConsorcio(int idConsorcio)                               throws SQLException;
    Liquidacion              save(Liquidacion liq)                                          throws SQLException;
    void                     saveDetalle(LiquidacionDetalle detalle)                        throws SQLException;
    void                     cerrarLiquidacion(int idLiquidacion)                          throws SQLException;
    List<LiquidacionDetalle> findDetallesByLiquidacion(int idLiquidacion)                  throws SQLException;
    Optional<LiquidacionDetalle> findDetalleByUnidadAndPeriodo(int idUnidad, LocalDate periodo) throws SQLException;
}
