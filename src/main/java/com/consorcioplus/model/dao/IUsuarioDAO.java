package com.consorcioplus.model.dao;

import com.consorcioplus.model.entity.Usuario;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la entidad Usuario.
 * El uso de interfaces permite desacoplar la lógica de negocio
 * de la implementación JDBC concreta (principio de Inversión de Dependencias).
 */
public interface IUsuarioDAO {
    Optional<Usuario> findByUsername(String username) throws SQLException;
    void save(Usuario usuario) throws SQLException;
}
