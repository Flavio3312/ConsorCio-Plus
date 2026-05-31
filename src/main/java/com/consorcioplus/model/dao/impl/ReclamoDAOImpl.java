package com.consorcioplus.model.dao.impl;

import com.consorcioplus.model.dao.IReclamoDAO;
import com.consorcioplus.model.entity.Reclamo;
import com.consorcioplus.model.entity.Reclamo.EstadoReclamo;
import com.consorcioplus.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReclamoDAOImpl — implementación JDBC de IReclamoDAO.
 *
 * Conceptos Java del Módulo 3:
 *  - JDBC: PreparedStatement con mapeo de ResultSet a entidades.
 *  - enum ↔ String: conversión entre el enum EstadoReclamo y el VARCHAR de la BD.
 *  - ArrayList: colección de reclamos devuelta como List.
 */
public class ReclamoDAOImpl implements IReclamoDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insertar(Reclamo reclamo) throws SQLException {
        String sql = "INSERT INTO reclamo "
                + "(id_unidad, descripcion, estado, fecha_alta, id_usuario_alta, nro_reclamo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reclamo.getIdUnidad());
            ps.setString(2, reclamo.getDescripcion());
            ps.setString(3, reclamo.getEstado().name()); // enum → String
            ps.setTimestamp(4, Timestamp.valueOf(reclamo.getFechaAlta()));
            ps.setInt(5, reclamo.getIdUsuarioAlta());
            ps.setString(6, reclamo.getNroReclamo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) reclamo.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void actualizarEstado(int idReclamo, EstadoReclamo nuevoEstado)
            throws SQLException {
        String sql;
        if (nuevoEstado == EstadoReclamo.RESUELTO) {
            // Si se resuelve, también se persiste la fecha de resolución
            sql = "UPDATE reclamo SET estado = ?, fecha_resolucion = ? WHERE id = ?";
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, nuevoEstado.name());
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, idReclamo);
                ps.executeUpdate();
            }
        } else {
            sql = "UPDATE reclamo SET estado = ? WHERE id = ?";
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, nuevoEstado.name());
                ps.setInt(2, idReclamo);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public List<Reclamo> findByConsorcio(int idConsorcio) throws SQLException {
        // Une reclamo con unidad_funcional para filtrar por consorcio
        String sql = "SELECT r.* FROM reclamo r "
                + "JOIN unidad_funcional uf ON r.id_unidad = uf.id "
                + "WHERE uf.id_consorcio = ? ORDER BY r.fecha_alta ASC";
        return ejecutarQuery(sql, idConsorcio);
    }

    @Override
    public List<Reclamo> findActivosByConsorcio(int idConsorcio) throws SQLException {
        String sql = "SELECT r.* FROM reclamo r "
                + "JOIN unidad_funcional uf ON r.id_unidad = uf.id "
                + "WHERE uf.id_consorcio = ? "
                + "AND r.estado IN ('PENDIENTE','EN_CURSO') "
                + "ORDER BY r.fecha_alta ASC";
        return ejecutarQuery(sql, idConsorcio);
    }

    @Override
    public Reclamo findByNroReclamo(String nroReclamo) throws SQLException {
        String sql = "SELECT * FROM reclamo WHERE nro_reclamo = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nroReclamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------
    // Métodos auxiliares privados
    // -------------------------------------------------------

    private List<Reclamo> ejecutarQuery(String sql, int param) throws SQLException {
        List<Reclamo> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /** Mapea una fila del ResultSet a un objeto Reclamo. */
    private Reclamo mapRow(ResultSet rs) throws SQLException {
        int           id          = rs.getInt("id");
        int           idUnidad    = rs.getInt("id_unidad");
        String        descripcion = rs.getString("descripcion");
        EstadoReclamo estado      = EstadoReclamo.valueOf(rs.getString("estado")); // String → enum
        LocalDateTime fechaAlta   = rs.getTimestamp("fecha_alta").toLocalDateTime();

        LocalDateTime fechaResol  = null;
        Timestamp tsResol = rs.getTimestamp("fecha_resolucion");
        if (tsResol != null) fechaResol = tsResol.toLocalDateTime();

        int    idUsuario  = rs.getInt("id_usuario_alta");
        String nroReclamo = rs.getString("nro_reclamo");

        return new Reclamo(id, idUnidad, descripcion, estado,
                fechaAlta, fechaResol, idUsuario, nroReclamo);
    }
}
