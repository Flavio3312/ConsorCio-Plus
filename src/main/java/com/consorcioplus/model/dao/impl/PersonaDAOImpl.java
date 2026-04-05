package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IPersonaDAO;
import com.consorcioplus.model.entity.Persona;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC de IPersonaDAO. */
public class PersonaDAOImpl implements IPersonaDAO {

    @Override
    public List<Persona> findAllActivos() throws SQLException {
        String sql = "SELECT id, tipo, nombre, apellido, dni, telefono, email, activo "
                   + "FROM persona WHERE activo = TRUE ORDER BY apellido, nombre";
        List<Persona> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public Optional<Persona> findById(int id) throws SQLException {
        String sql = "SELECT id, tipo, nombre, apellido, dni, telefono, email, activo "
                   + "FROM persona WHERE id = ?";
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
    public void save(Persona p) throws SQLException {
        String sql = "INSERT INTO persona (tipo, nombre, apellido, dni, telefono, email) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getTipo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getApellido());
            stmt.setString(4, p.getDni());
            stmt.setString(5, p.getTelefono());
            stmt.setString(6, p.getEmail());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void update(Persona p) throws SQLException {
        String sql = "UPDATE persona SET tipo=?, nombre=?, apellido=?, dni=?, "
                   + "telefono=?, email=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getTipo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getApellido());
            stmt.setString(4, p.getDni());
            stmt.setString(5, p.getTelefono());
            stmt.setString(6, p.getEmail());
            stmt.setInt(7, p.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE persona SET activo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void vincularConUnidad(int idPersona, int idUnidad, LocalDate desde) throws SQLException {
        String sql = "INSERT INTO persona_unidad (id_persona, id_unidad, fecha_desde) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPersona);
            stmt.setInt(2, idUnidad);
            stmt.setDate(3, Date.valueOf(desde));
            stmt.executeUpdate();
        }
    }

    private Persona mapRow(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getInt("id"));
        p.setTipo(rs.getString("tipo"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setDni(rs.getString("dni"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
