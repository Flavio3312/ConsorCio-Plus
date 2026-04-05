package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IGastoDAO;
import com.consorcioplus.model.entity.Gasto;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC de IGastoDAO. */
public class GastoDAOImpl implements IGastoDAO {

    @Override
    public List<Gasto> findByConsorcioAndPeriodo(int idConsorcio, LocalDate periodo) throws SQLException {
        String sql = "SELECT g.id, g.id_consorcio, g.periodo, g.categoria, g.descripcion, "
                   + "       g.monto, g.nro_factura, g.id_proveedor, "
                   + "       c.nombre AS nombre_consorcio, p.nombre AS nombre_proveedor "
                   + "FROM gasto g "
                   + "JOIN consorcio c ON c.id = g.id_consorcio "
                   + "LEFT JOIN proveedor p ON p.id = g.id_proveedor "
                   + "WHERE g.id_consorcio = ? AND g.periodo = ? "
                   + "ORDER BY g.categoria, g.descripcion";
        List<Gasto> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            stmt.setDate(2, Date.valueOf(periodo));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public void save(Gasto g) throws SQLException {
        String sql = "INSERT INTO gasto (id_consorcio, periodo, categoria, descripcion, "
                   + "monto, nro_factura, id_proveedor) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, g.getIdConsorcio());
            stmt.setDate(2, Date.valueOf(g.getPeriodo()));
            stmt.setString(3, g.getCategoria());
            stmt.setString(4, g.getDescripcion());
            stmt.setBigDecimal(5, g.getMonto());
            stmt.setString(6, g.getNroFactura());
            if (g.getIdProveedor() != null)
                stmt.setInt(7, g.getIdProveedor());
            else
                stmt.setNull(7, Types.INTEGER);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) g.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM gasto WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean existePeriodoCerrado(int idConsorcio, LocalDate periodo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM liquidacion "
                   + "WHERE id_consorcio = ? AND periodo = ? AND cerrada = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            stmt.setDate(2, Date.valueOf(periodo));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Gasto mapRow(ResultSet rs) throws SQLException {
        Gasto g = new Gasto();
        g.setId(rs.getInt("id"));
        g.setIdConsorcio(rs.getInt("id_consorcio"));
        g.setNombreConsorcio(rs.getString("nombre_consorcio"));
        g.setPeriodo(rs.getDate("periodo").toLocalDate());
        g.setCategoria(rs.getString("categoria"));
        g.setDescripcion(rs.getString("descripcion"));
        g.setMonto(rs.getBigDecimal("monto"));
        g.setNroFactura(rs.getString("nro_factura"));
        int idProv = rs.getInt("id_proveedor");
        if (!rs.wasNull()) g.setIdProveedor(idProv);
        g.setNombreProveedor(rs.getString("nombre_proveedor"));
        return g;
    }
}
