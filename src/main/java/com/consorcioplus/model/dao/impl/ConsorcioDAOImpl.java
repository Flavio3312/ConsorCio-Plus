package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IConsorcioDAO;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de IConsorcioDAO.
 * Usa PreparedStatement para evitar SQL Injection.
 * Nunca usa SELECT * — solo columnas necesarias.
 */
public class ConsorcioDAOImpl implements IConsorcioDAO {

    @Override
    public List<Consorcio> findAllActivos() throws SQLException {
        String sql = "SELECT id, nombre, direccion, cuit, total_pisos, activo "
                   + "FROM consorcio WHERE activo = TRUE ORDER BY nombre";
        List<Consorcio> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public Optional<Consorcio> findById(int id) throws SQLException {
        String sql = "SELECT id, nombre, direccion, cuit, total_pisos, activo "
                   + "FROM consorcio WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Consorcio c) throws SQLException {
        String sql = "INSERT INTO consorcio (nombre, direccion, cuit, total_pisos) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getDireccion());
            stmt.setString(3, c.getCuit());
            stmt.setInt(4, c.getTotalPisos());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void update(Consorcio c) throws SQLException {
        String sql = "UPDATE consorcio SET nombre=?, direccion=?, cuit=?, total_pisos=? "
                   + "WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getDireccion());
            stmt.setString(3, c.getCuit());
            stmt.setInt(4, c.getTotalPisos());
            stmt.setInt(5, c.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE consorcio SET activo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /** Mapea una fila del ResultSet a un objeto Consorcio. */
    private Consorcio mapRow(ResultSet rs) throws SQLException {
        Consorcio c = new Consorcio();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDireccion(rs.getString("direccion"));
        c.setCuit(rs.getString("cuit"));
        c.setTotalPisos(rs.getInt("total_pisos"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
