package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IPagoDAO;
import com.consorcioplus.model.entity.Pago;
import com.consorcioplus.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PagoDAOImpl — implementación JDBC de IPagoDAO.
 *
 * Conceptos Java del Módulo 3:
 *  - JDBC con transacción explícita: el pago y la actualización del saldo
 *    se realizan en una sola transacción (commit / rollback).
 *  - ArrayList: colección dinámica para retornar los pagos de un detalle.
 *  - PreparedStatement: consultas parametrizadas (previene SQL injection).
 */
public class PagoDAOImpl implements IPagoDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Inserta el pago y descuenta el montoPagado del saldo_deudor del detalle.
     * Ambas operaciones forman una única transacción atómica.
     */
    @Override
    public void insertar(Pago pago) throws SQLException {
        Connection conn = getConn();
        boolean autoCommitOriginal = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // 1. Insertar el pago
            String sqlInsert = "INSERT INTO pago "
                    + "(id_liq_detalle, fecha_pago, monto_pagado, nro_recibo, id_usuario) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(
                    sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pago.getIdLiqDetalle());
                ps.setTimestamp(2, Timestamp.valueOf(pago.getFechaPago()));
                ps.setBigDecimal(3, pago.getMontoPagado());
                ps.setString(4, pago.getNroRecibo());
                ps.setInt(5, pago.getIdUsuario());
                ps.executeUpdate();
                // Asignar el id generado al objeto
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) pago.setId(rs.getInt(1));
                }
            }

            // 2. Actualizar saldo_deudor en liquidacion_detalle
            String sqlUpdate = "UPDATE liquidacion_detalle "
                    + "SET saldo_deudor = saldo_deudor - ? "
                    + "WHERE id = ? AND saldo_deudor >= ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setBigDecimal(1, pago.getMontoPagado());
                ps.setInt(2, pago.getIdLiqDetalle());
                ps.setBigDecimal(3, pago.getMontoPagado());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    // El monto pagado supera el saldo — no se permite sobrepago
                    throw new SQLException(
                            "El monto pagado (" + pago.getMontoPagado()
                                    + ") supera el saldo deudor del detalle "
                                    + pago.getIdLiqDetalle() + ".");
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }
    }

    @Override
    public List<Pago> findByLiquidacionDetalle(int idLiqDetalle) throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT id, id_liq_detalle, fecha_pago, monto_pagado, "
                + "nro_recibo, id_usuario "
                + "FROM pago WHERE id_liq_detalle = ? ORDER BY fecha_pago ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idLiqDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapRow(rs));
                }
            }
        }
        return pagos;
    }

    @Override
    public BigDecimal calcularTotalPagado(int idLiqDetalle) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto_pagado), 0) FROM pago WHERE id_liq_detalle = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idLiqDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Pago findByNroRecibo(String nroRecibo) throws SQLException {
        String sql = "SELECT id, id_liq_detalle, fecha_pago, monto_pagado, "
                + "nro_recibo, id_usuario FROM pago WHERE UPPER(nro_recibo) = UPPER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nroRecibo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------
    // Mapeo de ResultSet → Pago (evita duplicación de código)
    // -------------------------------------------------------
    private Pago mapRow(ResultSet rs) throws SQLException {
        int           id           = rs.getInt("id");
        int           idLiqDet     = rs.getInt("id_liq_detalle");
        LocalDateTime fechaPago    = rs.getTimestamp("fecha_pago").toLocalDateTime();
        BigDecimal    monto        = rs.getBigDecimal("monto_pagado");
        String        nroRecibo    = rs.getString("nro_recibo");
        int           idUsuario    = rs.getInt("id_usuario");
        return new Pago(id, idLiqDet, fechaPago, monto, nroRecibo, idUsuario);
    }
}
