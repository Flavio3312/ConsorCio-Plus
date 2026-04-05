package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IUsuarioDAO;
import com.consorcioplus.model.entity.Usuario;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

/** Implementación JDBC de IUsuarioDAO. */
public class UsuarioDAOImpl implements IUsuarioDAO {

    @Override
    public Optional<Usuario> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, perfil, activo "
                   + "FROM usuario WHERE username = ? AND activo = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (username, password_hash, perfil) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, u.getPasswordHash());
            stmt.setString(3, u.getPerfil());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("perfil"),
            rs.getBoolean("activo")
        );
    }
}
