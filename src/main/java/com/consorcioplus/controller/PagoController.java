package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IPagoDAO;
import com.consorcioplus.model.dao.impl.PagoDAOImpl;
import com.consorcioplus.model.entity.Pago;
import com.consorcioplus.util.AuditoriaStack;
import com.consorcioplus.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PagoController — lógica de negocio para el registro de pagos de expensas.
 *
 * Conceptos Java del Módulo 3:
 *  - Sobrecarga de métodos (Overloading): tres versiones de registrarPago()
 *    con distintos conjuntos de parámetros, por conveniencia del llamador.
 *  - AuditoriaStack: cada pago exitoso queda registrado en el historial LIFO.
 *  - Principio SRP: este controller solo gestiona pagos.
 */
public class PagoController {

    private final IPagoDAO        pagoDAO;
    private final AuditoriaStack  auditoria;

    public PagoController() {
        this.pagoDAO   = new PagoDAOImpl();
        this.auditoria = AuditoriaStack.getInstance();
    }

    // -------------------------------------------------------
    // SOBRECARGA 1: pago completo con generación automática de
    // número de recibo y fecha = hoy
    // -------------------------------------------------------
    /**
     * Registra un pago generando automáticamente el número de recibo y
     * usando la fecha actual como fecha de pago.
     *
     * @param idLiqDetalle id del detalle de liquidación a pagar
     * @param monto        monto a pagar (debe ser > 0 y <= saldo_deudor)
     * @return el objeto Pago persistido con su nroRecibo asignado
     */
    public Pago registrarPago(int idLiqDetalle, BigDecimal monto) throws SQLException {
        return registrarPago(idLiqDetalle, monto, generarNroRecibo());
    }

    // -------------------------------------------------------
    // SOBRECARGA 2: pago con número de recibo provisto externamente
    // -------------------------------------------------------
    /**
     * Registra un pago con un número de recibo provisto externamente
     * (por ejemplo, para reimprimir un recibo o migrar datos históricos).
     *
     * @param idLiqDetalle id del detalle de liquidación
     * @param monto        monto a pagar
     * @param nroRecibo    número de recibo externo
     * @return el objeto Pago persistido
     */
    public Pago registrarPago(int idLiqDetalle, BigDecimal monto, String nroRecibo)
            throws SQLException {
        return registrarPago(idLiqDetalle, monto, nroRecibo, LocalDate.now());
    }

    // -------------------------------------------------------
    // SOBRECARGA 3 (método base): todos los parámetros explícitos
    // -------------------------------------------------------
    /**
     * Método base — registra el pago con control total de todos los parámetros.
     * Las sobrecargas anteriores delegan aquí.
     *
     * @param idLiqDetalle id del detalle de liquidación
     * @param monto        monto a pagar
     * @param nroRecibo    número de recibo
     * @param fechaPago    fecha del cobro
     * @return el objeto Pago persistido
     */
    public Pago registrarPago(int idLiqDetalle, BigDecimal monto,
                               String nroRecibo, LocalDate fechaPago) throws SQLException {
        // Validaciones de negocio
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (nroRecibo == null || nroRecibo.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de recibo no puede estar vacío.");
        }

        int idUsuario = SessionManager.getInstance().getUsuarioActual() != null
                ? SessionManager.getInstance().getUsuarioActual().getId()
                : 0;

        Pago pago = new Pago(idLiqDetalle, monto, nroRecibo, idUsuario);
        pagoDAO.insertar(pago);

        // Registrar en el historial de auditoría (Stack LIFO)
        auditoria.registrar("PAGO_RECIBIDO",
                "Recibo " + nroRecibo + " — $" + monto
                        + " — DetalleLiq#" + idLiqDetalle);

        return pago;
    }

    /**
     * Obtiene todos los pagos registrados para un detalle de liquidación.
     * Permite verificar el historial de pagos parciales.
     */
    public List<Pago> obtenerPagosPorDetalle(int idLiqDetalle) throws SQLException {
        return pagoDAO.findByLiquidacionDetalle(idLiqDetalle);
    }

    /**
     * Calcula el total acumulado pagado para un detalle de liquidación.
     */
    public BigDecimal calcularTotalPagado(int idLiqDetalle) throws SQLException {
        return pagoDAO.calcularTotalPagado(idLiqDetalle);
    }

    /**
     * Busca un pago por número de recibo (para reimprimir o consultar).
     */
    public Pago buscarPorRecibo(String nroRecibo) throws SQLException {
        return pagoDAO.findByNroRecibo(nroRecibo);
    }

    // -------------------------------------------------------
    // Generación de número de recibo único
    // En producción se obtiene de una secuencia de BD (MAX+1)
    // -------------------------------------------------------
    private String generarNroRecibo() {
        String anio = String.valueOf(LocalDate.now().getYear());
        String ts   = String.valueOf(System.currentTimeMillis() % 100000);
        return String.format("REC-%s-%05d", anio, Long.parseLong(ts));
    }
}
