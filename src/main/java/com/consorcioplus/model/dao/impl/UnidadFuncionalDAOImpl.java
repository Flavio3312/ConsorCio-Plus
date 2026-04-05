package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IUnidadFuncionalDAO;
import com.consorcioplus.model.entity.UnidadFuncional;
import com.consorcioplus.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC de IUnidadFuncionalDAO. */
public class UnidadFuncionalDAOImpl implements IUnidadFuncionalDAO {

    @Override
    public List<UnidadFuncional> findByConsorcio(int idConsorcio) throws SQLException {
        String sql = "SELECT uf.id, uf.numero, uf.piso, uf.porcentual, "
                   + "       uf.id_consorcio, uf.activo, c.nombre AS nombre_consorcio "
                   + "FROM unidad_funcional uf "
                   + "JOIN consorcio c ON c.id = uf.id_consorcio "
                   + "WHERE uf.id_consorcio = ? AND uf.activo = TRUE "
                   + "ORDER BY uf.piso, uf.numero";
        List<UnidadFuncional> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public Optional<UnidadFuncional> findById(int id) throws SQLException {
        String sql = "SELECT uf.id, uf.numero, uf.piso, uf.porcentual, "
                   + "       uf.id_consorcio, uf.activo, c.nombre AS nombre_consorcio "
                   + "FROM unidad_funcional uf "
                   + "JOIN consorcio c ON c.id = uf.id_consorcio "
                   + "WHERE uf.id = ?";
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
    public void save(UnidadFuncional uf) throws SQLException {
        String sql = "INSERT INTO unidad_funcional (numero, piso, porcentual, id_consorcio) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, uf.getNumero());
            stmt.setString(2, uf.getPiso());
            stmt.setBigDecimal(3, uf.getPorcentual());
            stmt.setInt(4, uf.getIdConsorcio());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) uf.setId(keys.getInt(1));
            }
        }
    }

    @Override
    public void update(UnidadFuncional uf) throws SQLException {
        String sql = "UPDATE unidad_funcional SET numero=?, piso=?, porcentual=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uf.getNumero());
            stmt.setString(2, uf.getPiso());
            stmt.setBigDecimal(3, uf.getPorcentual());
            stmt.setInt(4, uf.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE unidad_funcional SET activo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public BigDecimal sumPorcentualesByConsorcio(int idConsorcio) throws SQLException {
        String sql = "SELECT COALESCE(SUM(porcentual), 0) AS total "
                   + "FROM unidad_funcional WHERE id_consorcio = ? AND activo = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsorcio);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    private UnidadFuncional mapRow(ResultSet rs) throws SQLException {
        UnidadFuncional uf = new UnidadFuncional();
        uf.setId(rs.getInt("id"));
        uf.setNumero(rs.getString("numero"));
        uf.setPiso(rs.getString("piso"));
        uf.setPorcentual(rs.getBigDecimal("porcentual"));
        uf.setIdConsorcio(rs.getInt("id_consorcio"));
        uf.setNombreConsorcio(rs.getString("nombre_consorcio"));
        uf.setActivo(rs.getBoolean("activo"));
        return uf;
    }
}
