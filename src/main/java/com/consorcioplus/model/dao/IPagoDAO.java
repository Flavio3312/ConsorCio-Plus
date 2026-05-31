package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Pago;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * IPagoDAO — interfaz (contrato) para el acceso a datos de Pago.
 *
 * Concepto Java del Módulo 3:
 *  - Interface: define el contrato sin revelar la implementación (JDBC).
 *  - Bajo acoplamiento: el Controller depende de esta interfaz, no de la implementación.
 */
public interface IPagoDAO {

    /**
     * Inserta un nuevo pago y actualiza el saldo_deudor del detalle de liquidación.
     * Operación transaccional: ambos cambios se confirman juntos o se revierten.
     *
     * @param pago el objeto Pago a persistir (se le asignará el id generado)
     * @throws SQLException si ocurre un error de base de datos
     */
    void insertar(Pago pago) throws SQLException;

    /**
     * Retorna todos los pagos asociados a un detalle de liquidación específico.
     *
     * @param idLiqDetalle identificador del detalle de liquidación
     * @return lista de pagos (puede estar vacía, nunca null)
     */
    List<Pago> findByLiquidacionDetalle(int idLiqDetalle) throws SQLException;

    /**
     * Calcula el total pagado acumulado para un detalle de liquidación.
     * Equivale a la suma de todos los montoPagado para ese idLiqDetalle.
     *
     * @param idLiqDetalle identificador del detalle de liquidación
     * @return total acumulado (BigDecimal.ZERO si no hay pagos)
     */
    BigDecimal calcularTotalPagado(int idLiqDetalle) throws SQLException;

    /**
     * Busca un pago por su número de recibo único.
     *
     * @param nroRecibo número de recibo (case-insensitive)
     * @return el Pago encontrado, o null si no existe
     */
    Pago findByNroRecibo(String nroRecibo) throws SQLException;
}
