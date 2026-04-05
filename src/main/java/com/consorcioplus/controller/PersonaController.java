package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IPersonaDAO;
import com.consorcioplus.model.dao.impl.PersonaDAOImpl;
import com.consorcioplus.model.entity.Persona;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Controller para Persona (Propietarios e Inquilinos). */
public class PersonaController {

    private final IPersonaDAO personaDAO;

    public PersonaController() {
        this.personaDAO = new PersonaDAOImpl();
    }

    public List<Persona> listarActivos() throws SQLException {
        return personaDAO.findAllActivos();
    }

    public Optional<Persona> buscarPorId(int id) throws SQLException {
        return personaDAO.findById(id);
    }

    public void crear(String tipo, String nombre, String apellido,
                      String dni, String telefono, String email) throws SQLException {
        validar(tipo, nombre, apellido);
        personaDAO.save(new Persona(tipo, nombre.trim(), apellido.trim(),
            dni, telefono, email));
    }

    public void actualizar(int id, String tipo, String nombre, String apellido,
                           String dni, String telefono, String email) throws SQLException {
        validar(tipo, nombre, apellido);
        Persona p = new Persona(tipo, nombre.trim(), apellido.trim(), dni, telefono, email);
        p.setId(id);
        personaDAO.update(p);
    }

    public void darDeBaja(int id) throws SQLException {
        personaDAO.softDelete(id);
    }

    public void vincularConUnidad(int idPersona, int idUnidad) throws SQLException {
        personaDAO.vincularConUnidad(idPersona, idUnidad, LocalDate.now());
    }

    private void validar(String tipo, String nombre, String apellido) {
        if (tipo == null || (!tipo.equals("PROPIETARIO") && !tipo.equals("INQUILINO")))
            throw new IllegalArgumentException("El tipo debe ser PROPIETARIO o INQUILINO.");
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (apellido == null || apellido.trim().isEmpty())
            throw new IllegalArgumentException("El apellido es obligatorio.");
    }
}

