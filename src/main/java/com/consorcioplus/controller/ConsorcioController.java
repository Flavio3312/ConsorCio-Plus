package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IConsorcioDAO;
import com.consorcioplus.model.dao.impl.ConsorcioDAOImpl;
import com.consorcioplus.model.entity.Consorcio;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller para la gestión de Consorcios.
 * Contiene la lógica de validación antes de delegar al DAO.
 */
public class ConsorcioController {

    private final IConsorcioDAO consorcioDAO;

    public ConsorcioController() {
        this.consorcioDAO = new ConsorcioDAOImpl();
    }

    public List<Consorcio> listarActivos() throws SQLException {
        return consorcioDAO.findAllActivos();
    }

    public Optional<Consorcio> buscarPorId(int id) throws SQLException {
        return consorcioDAO.findById(id);
    }

    /**
     * Crea un nuevo consorcio previa validación de campos obligatorios.
     *
     * @throws IllegalArgumentException si faltan campos requeridos
     */
    public void crear(String nombre, String direccion, String cuit, int pisos)
            throws SQLException {
        validarCamposObligatorios(nombre, direccion);
        if (pisos < 1) throw new IllegalArgumentException("Los pisos deben ser al menos 1.");

        Consorcio c = new Consorcio(
            nombre.trim(), direccion.trim(),
            cuit != null ? cuit.trim() : null,
            pisos
        );
        consorcioDAO.save(c);
    }

    /**
     * Actualiza los datos de un consorcio existente.
     */
    public void actualizar(int id, String nombre, String direccion, String cuit, int pisos)
            throws SQLException {
        validarCamposObligatorios(nombre, direccion);
        if (pisos < 1) throw new IllegalArgumentException("Los pisos deben ser al menos 1.");

        Consorcio c = new Consorcio(nombre.trim(), direccion.trim(),
                                    cuit != null ? cuit.trim() : null, pisos);
        c.setId(id);
        consorcioDAO.update(c);
    }

    /**
     * Realiza la baja lógica de un consorcio (activo = FALSE).
     */
    public void darDeBaja(int id) throws SQLException {
        consorcioDAO.softDelete(id);
    }

    private void validarCamposObligatorios(String nombre, String direccion) {
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del consorcio es obligatorio.");
        if (direccion == null || direccion.trim().isEmpty())
            throw new IllegalArgumentException("La dirección del consorcio es obligatoria.");
    }
}

