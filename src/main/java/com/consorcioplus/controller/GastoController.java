package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IGastoDAO;
import com.consorcioplus.model.dao.impl.GastoDAOImpl;
import com.consorcioplus.model.entity.Gasto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller para la gestión de Gastos mensuales.
 * Aplica la regla de dominio RFS16: período cerrado = sin gastos nuevos.
 */
public class GastoController {

    private final IGastoDAO gastoDAO;

    public GastoController() {
        this.gastoDAO = new GastoDAOImpl();
    }

    public List<Gasto> listarPorConsorcioYPeriodo(int idConsorcio, LocalDate periodo)
            throws SQLException {
        return gastoDAO.findByConsorcioAndPeriodo(idConsorcio, periodo);
    }

    /**
     * Registra un nuevo gasto.
     * Rechaza el registro si el período ya fue liquidado (RFS16).
     */
    public void registrar(int idConsorcio, LocalDate periodo, String categoria,
                          String descripcion, BigDecimal monto,
                          String nroFactura, Integer idProveedor) throws SQLException {
        validarGasto(categoria, descripcion, monto);
        if (gastoDAO.existePeriodoCerrado(idConsorcio, periodo)) {
            throw new IllegalStateException(
                "El período " + periodo.getMonthValue() + "/" + periodo.getYear()
                + " ya fue liquidado y está cerrado. No se pueden agregar gastos."
            );
        }
        Gasto g = new Gasto();
        g.setIdConsorcio(idConsorcio);
        g.setPeriodo(periodo.withDayOfMonth(1)); // Normalizar al 1er día del mes
        g.setCategoria(categoria);
        g.setDescripcion(descripcion.trim());
        g.setMonto(monto);
        g.setNroFactura(nroFactura != null ? nroFactura.trim() : null);
        g.setIdProveedor(idProveedor);
        gastoDAO.save(g);
    }

    public void eliminar(int idGasto) throws SQLException {
        gastoDAO.delete(idGasto);
    }

    public boolean periodoCerrado(int idConsorcio, LocalDate periodo) throws SQLException {
        return gastoDAO.existePeriodoCerrado(idConsorcio, periodo);
    }

    private void validarGasto(String categoria, String descripcion, BigDecimal monto) {
        if (categoria == null || (!categoria.equals("ORDINARIO") && !categoria.equals("EXTRAORDINARIO")))
            throw new IllegalArgumentException("Categoría inválida. Debe ser ORDINARIO o EXTRAORDINARIO.");
        if (descripcion == null || descripcion.trim().isEmpty())
            throw new IllegalArgumentException("La descripción del gasto es obligatoria.");
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
    }
}

