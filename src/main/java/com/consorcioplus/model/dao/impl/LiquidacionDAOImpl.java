package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.ILiquidacionDAO;
import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC de ILiquidacionDAO. */
public class LiquidacionDAOImpl implements ILiquidacionDAO {

    @Override
    public Optional<Liquidacion> findByConsorcioAndPeriodo(int idConsorcio, LocalDate periodo) throws SQLException {
        String sql = "SELECT l.id, l.id_consorcio, l.periodo, l.total_ordinario, "
                   + "       l.total_extraordinario, l.fecha_cierre, l.cerrada, "
                   + "       c.nombre AS nombre_consorcio "
                   + "FROM liquidacion l JOIN consorcio c ON c.id = l.id_consorcio "
                   + "WHERE l.id_consorcio = ? AND l.periodo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            stmt.setDate(2, Date.valueOf(periodo));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapLiqRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Liquidacion> findByConsorcio(int idConsorcio) throws SQLException {
        String sql = "SELECT l.id, l.id_consorcio, l.periodo, l.total_ordinario, "
                   + "       l.total_extraordinario, l.fecha_cierre, l.cerrada, "
                   + "       c.nombre AS nombre_consorcio "
                   + "FROM liquidacion l JOIN consorcio c ON c.id = l.id_consorcio "
                   + "WHERE l.id_consorcio = ? ORDER BY l.periodo DESC";
        List<Liquidacion> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapLiqRow(rs));
            }
        }
        return result;
    }

    @Override
    public Liquidacion save(Liquidacion liq) throws SQLException {
        String sql = "INSERT INTO liquidacion (id_consorcio, periodo, total_ordinario, total_extraordinario) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, liq.getIdConsorcio());
            stmt.setDate(2, Date.valueOf(liq.getPeriodo()));
            stmt.setBigDecimal(3, liq.getTotalOrdinario());
            stmt.setBigDecimal(4, liq.getTotalExtraordinario());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) liq.setId(keys.getInt(1));
            }
        }
        return liq;
    }

    @Override
    public void saveDetalle(LiquidacionDetalle d) throws SQLException {
        String sql = "INSERT INTO liquidacion_detalle "
                   + "(id_liquidacion, id_unidad, expensa_ordinaria, expensa_extraordinaria, "
                   + " mora_aplicada, total_a_pagar, saldo_deudor) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, d.getIdLiquidacion());
            stmt.setInt(2, d.getIdUnidad());
            stmt.setBigDecimal(3, d.getExpensaOrdinaria());
            stmt.setBigDecimal(4, d.getExpensaExtraordinaria());
            stmt.setBigDecimal(5, d.getMoraAplicada());
            stmt.setBigDecimal(6, d.getTotalAPagar());
            stmt.setBigDecimal(7, d.getSaldoDeudor());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void cerrarLiquidacion(int idLiquidacion) throws SQLException {
        String sql = "UPDATE liquidacion SET cerrada = TRUE, fecha_cierre = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLiquidacion);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<LiquidacionDetalle> findDetallesByLiquidacion(int idLiquidacion) throws SQLException {
        String sql = "SELECT ld.id, ld.id_liquidacion, ld.id_unidad, "
                   + "       ld.expensa_ordinaria, ld.expensa_extraordinaria, "
                   + "       ld.mora_aplicada, ld.total_a_pagar, ld.saldo_deudor, "
                   + "       uf.numero AS numero_unidad, uf.piso AS piso_unidad, uf.porcentual "
                   + "FROM liquidacion_detalle ld "
                   + "JOIN unidad_funcional uf ON uf.id = ld.id_unidad "
                   + "WHERE ld.id_liquidacion = ? "
                   + "ORDER BY uf.piso, uf.numero";
        List<LiquidacionDetalle> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLiquidacion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapDetalleRow(rs));
            }
        }
        return result;
    }

    @Override
    public Optional<LiquidacionDetalle> findDetalleByUnidadAndPeriodo(int idUnidad, LocalDate periodo) throws SQLException {
        String sql = "SELECT ld.id, ld.id_liquidacion, ld.id_unidad, "
                   + "       ld.expensa_ordinaria, ld.expensa_extraordinaria, "
                   + "       ld.mora_aplicada, ld.total_a_pagar, ld.saldo_deudor, "
                   + "       uf.numero AS numero_unidad, uf.piso AS piso_unidad, uf.porcentual "
                   + "FROM liquidacion_detalle ld "
                   + "JOIN liquidacion l ON l.id = ld.id_liquidacion "
                   + "JOIN unidad_funcional uf ON uf.id = ld.id_unidad "
                   + "WHERE ld.id_unidad = ? AND l.periodo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUnidad);
            stmt.setDate(2, Date.valueOf(periodo));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapDetalleRow(rs));
            }
        }
        return Optional.empty();
    }

    private Liquidacion mapLiqRow(ResultSet rs) throws SQLException {
        Liquidacion l = new Liquidacion();
        l.setId(rs.getInt("id"));
        l.setIdConsorcio(rs.getInt("id_consorcio"));
        l.setNombreConsorcio(rs.getString("nombre_consorcio"));
        l.setPeriodo(rs.getDate("periodo").toLocalDate());
        l.setTotalOrdinario(rs.getBigDecimal("total_ordinario"));
        l.setTotalExtraordinario(rs.getBigDecimal("total_extraordinario"));
        Timestamp fc = rs.getTimestamp("fecha_cierre");
        if (fc != null) l.setFechaCierre(fc.toLocalDateTime());
        l.setCerrada(rs.getBoolean("cerrada"));
        return l;
    }

    private LiquidacionDetalle mapDetalleRow(ResultSet rs) throws SQLException {
        LiquidacionDetalle d = new LiquidacionDetalle();
        d.setId(rs.getInt("id"));
        d.setIdLiquidacion(rs.getInt("id_liquidacion"));
        d.setIdUnidad(rs.getInt("id_unidad"));
        d.setNumeroUnidad(rs.getString("numero_unidad"));
        d.setPisoUnidad(rs.getString("piso_unidad"));
        d.setPorcentual(rs.getBigDecimal("porcentual"));
        d.setExpensaOrdinaria(rs.getBigDecimal("expensa_ordinaria"));
        d.setExpensaExtraordinaria(rs.getBigDecimal("expensa_extraordinaria"));
        d.setMoraAplicada(rs.getBigDecimal("mora_aplicada"));
        d.setTotalAPagar(rs.getBigDecimal("total_a_pagar"));
        d.setSaldoDeudor(rs.getBigDecimal("saldo_deudor"));
        return d;
    }
}
