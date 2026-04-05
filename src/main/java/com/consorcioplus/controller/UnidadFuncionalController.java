package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IUnidadFuncionalDAO;
import com.consorcioplus.model.dao.impl.UnidadFuncionalDAOImpl;
import com.consorcioplus.model.entity.UnidadFuncional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller para la gestión de Unidades Funcionales.
 * Aplica la regla de dominio RD01: suma de porcentuales ≤ 100%.
 */
public class UnidadFuncionalController {

    private static final BigDecimal CIEN = new BigDecimal("100.0000");

    private final IUnidadFuncionalDAO unidadDAO;

    public UnidadFuncionalController() {
        this.unidadDAO = new UnidadFuncionalDAOImpl();
    }

    public List<UnidadFuncional> listarPorConsorcio(int idConsorcio) throws SQLException {
        return unidadDAO.findByConsorcio(idConsorcio);
    }

    public Optional<UnidadFuncional> buscarPorId(int id) throws SQLException {
        return unidadDAO.findById(id);
    }

    /** Suma actual de porcentuales del consorcio para validación en tiempo real. */
    public BigDecimal obtenerSumaPorcentuales(int idConsorcio) throws SQLException {
        return unidadDAO.sumPorcentualesByConsorcio(idConsorcio);
    }

    /**
     * Crea una nueva Unidad Funcional.
     * Valida que la suma de porcentuales no supere 100.
     */
    public void crear(String numero, String piso, BigDecimal porcentual, int idConsorcio)
            throws SQLException {
        validarCampos(numero, porcentual);
        BigDecimal sumaActual = unidadDAO.sumPorcentualesByConsorcio(idConsorcio);
        if (sumaActual.add(porcentual).compareTo(CIEN) > 0) {
            throw new IllegalArgumentException(
                "El porcentual ingresado supera el 100%. Suma actual: "
                + sumaActual.toPlainString() + "%. Disponible: "
                + CIEN.subtract(sumaActual).toPlainString() + "%."
            );
        }
        UnidadFuncional uf = new UnidadFuncional(numero.trim(), piso, porcentual, idConsorcio);
        unidadDAO.save(uf);
    }

    /**
     * Actualiza una Unidad Funcional existente.
     * Valida porcentuales excluyendo la propia unidad de la suma.
     */
    public void actualizar(int id, String numero, String piso,
                           BigDecimal porcentual, int idConsorcio) throws SQLException {
        validarCampos(numero, porcentual);
        BigDecimal sumaActual = unidadDAO.sumPorcentualesByConsorcio(idConsorcio);
        // La UF original ya está incluida; restamos su porcentual actual
        Optional<UnidadFuncional> original = unidadDAO.findById(id);
        BigDecimal sumaExcluyendoEsta = original
            .map(uf -> sumaActual.subtract(uf.getPorcentual()))
            .orElse(sumaActual);
        if (sumaExcluyendoEsta.add(porcentual).compareTo(CIEN) > 0) {
            throw new IllegalArgumentException(
                "El porcentual actualizado supera el 100%. Disponible: "
                + CIEN.subtract(sumaExcluyendoEsta).toPlainString() + "%."
            );
        }
        UnidadFuncional uf = new UnidadFuncional(numero.trim(), piso, porcentual, idConsorcio);
        uf.setId(id);
        unidadDAO.update(uf);
    }

    public void darDeBaja(int id) throws SQLException {
        unidadDAO.softDelete(id);
    }

    private void validarCampos(String numero, BigDecimal porcentual) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número de UF es obligatorio.");
        if (porcentual == null || porcentual.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El porcentual debe ser mayor a 0.");
        if (porcentual.compareTo(CIEN) > 0)
            throw new IllegalArgumentException("El porcentual no puede superar el 100%.");
    }
}

