package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IGastoDAO;
import com.consorcioplus.model.dao.ILiquidacionDAO;
import com.consorcioplus.model.dao.IUnidadFuncionalDAO;
import com.consorcioplus.model.dao.impl.GastoDAOImpl;
import com.consorcioplus.model.dao.impl.LiquidacionDAOImpl;
import com.consorcioplus.model.dao.impl.UnidadFuncionalDAOImpl;
import com.consorcioplus.model.entity.Gasto;
import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.model.entity.UnidadFuncional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller que implementa la lógica central de liquidación de expensas.
 *
 * Reglas de negocio aplicadas:
 * - RD01: La suma de porcentuales debe ser exactamente 100%.
 * - RD02: Ordinarios al habitante, Extraordinarios al propietario.
 * - RFS15: Deuda_UF = GastoTotal × (Porcentual / 100)
 * - RFS13: Mora de 3% sobre saldo deudor del mes anterior.
 * - RFS16: Cierre del período tras liquidar.
 */
public class LiquidacionController {

    /** Porcentaje de mora mensual (3%). */
    private static final BigDecimal PORCENTAJE_MORA = new BigDecimal("0.03");
    private static final BigDecimal CIEN            = new BigDecimal("100");

    private final ILiquidacionDAO    liquidacionDAO;
    private final IGastoDAO          gastoDAO;
    private final IUnidadFuncionalDAO unidadDAO;

    public LiquidacionController() {
        this.liquidacionDAO = new LiquidacionDAOImpl();
        this.gastoDAO       = new GastoDAOImpl();
        this.unidadDAO      = new UnidadFuncionalDAOImpl();
    }

    /**
     * Ejecuta la liquidación mensual completa para un consorcio y período.
     *
     * @param idConsorcio consorcio a liquidar
     * @param periodo     primer día del mes (YYYY-MM-01)
     * @return La liquidación con su detalle calculado
     * @throws SQLException            si falla la BD
     * @throws IllegalStateException   si el período ya fue liquidado
     * @throws IllegalArgumentException si los porcentuales no suman 100%
     */
    public Liquidacion ejecutarLiquidacion(int idConsorcio, LocalDate periodo) throws SQLException {
        LocalDate primerDia = periodo.withDayOfMonth(1);

        // 1. Verificar que el período no esté ya cerrado
        Optional<Liquidacion> existente = liquidacionDAO.findByConsorcioAndPeriodo(idConsorcio, primerDia);
        if (existente.isPresent() && existente.get().isCerrada()) {
            throw new IllegalStateException(
                "El período " + primerDia.getMonthValue() + "/" + primerDia.getYear()
                + " ya fue liquidado y está cerrado.");
        }

        // 2. Obtener Unidades Funcionales activas
        List<UnidadFuncional> unidades = unidadDAO.findByConsorcio(idConsorcio);
        if (unidades.isEmpty()) {
            throw new IllegalArgumentException("El consorcio no tiene Unidades Funcionales activas.");
        }

        // 3. Validar RD01: porcentuales suman 100%
        BigDecimal sumaPorcentuales = unidades.stream()
            .map(UnidadFuncional::getPorcentual)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumaPorcentuales.compareTo(CIEN) != 0) {
            throw new IllegalArgumentException(
                "La suma de porcentuales es " + sumaPorcentuales.toPlainString()
                + "%. Debe ser exactamente 100% (falta: "
                + CIEN.subtract(sumaPorcentuales).toPlainString() + "%)."
            );
        }

        // 4. Obtener gastos del período
        List<Gasto> gastos = gastoDAO.findByConsorcioAndPeriodo(idConsorcio, primerDia);
        BigDecimal totalOrdinario = gastos.stream()
            .filter(g -> "ORDINARIO".equals(g.getCategoria()))
            .map(Gasto::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExtraordinario = gastos.stream()
            .filter(g -> "EXTRAORDINARIO".equals(g.getCategoria()))
            .map(Gasto::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Crear cabecera de liquidación
        Liquidacion liq = new Liquidacion();
        liq.setIdConsorcio(idConsorcio);
        liq.setPeriodo(primerDia);
        liq.setTotalOrdinario(totalOrdinario);
        liq.setTotalExtraordinario(totalExtraordinario);
        liquidacionDAO.save(liq);

        // 6. Calcular detalle por UF (RFS15 + RD02 + RFS13)
        LocalDate periodoAnterior = primerDia.minusMonths(1);
        List<LiquidacionDetalle> detalles = new ArrayList<>();
        for (UnidadFuncional uf : unidades) {
            LiquidacionDetalle detalle = calcularDetalleUF(
                uf, liq.getId(), totalOrdinario, totalExtraordinario,
                idConsorcio, periodoAnterior
            );
            liquidacionDAO.saveDetalle(detalle);
            detalles.add(detalle);
        }

        // 7. Cerrar el período (RFS16)
        liquidacionDAO.cerrarLiquidacion(liq.getId());
        liq.setCerrada(true);

        return liq;
    }

    /**
     * Calcula el detalle de liquidación para una Unidad Funcional.
     * Aplica mora del 3% si la UF tenía saldo deudor el mes anterior (RFS13).
     */
    private LiquidacionDetalle calcularDetalleUF(
            UnidadFuncional uf, int idLiquidacion,
            BigDecimal totalOrdinario, BigDecimal totalExtraordinario,
            int idConsorcio, LocalDate periodoAnterior) throws SQLException {

        BigDecimal factor = uf.getPorcentual().divide(CIEN, 10, RoundingMode.HALF_UP);

        BigDecimal expensaOrd   = totalOrdinario.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expensaExtra = totalExtraordinario.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        // Mora sobre saldo deudor del período anterior (RFS13)
        BigDecimal mora = BigDecimal.ZERO;
        Optional<LiquidacionDetalle> detalleAnterior =
            liquidacionDAO.findDetalleByUnidadAndPeriodo(uf.getId(), periodoAnterior);
        if (detalleAnterior.isPresent()) {
            BigDecimal saldoAnterior = detalleAnterior.get().getSaldoDeudor();
            if (saldoAnterior.compareTo(BigDecimal.ZERO) > 0) {
                mora = saldoAnterior.multiply(PORCENTAJE_MORA).setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal total = expensaOrd.add(expensaExtra).add(mora);

        LiquidacionDetalle d = new LiquidacionDetalle();
        d.setIdLiquidacion(idLiquidacion);
        d.setIdUnidad(uf.getId());
        d.setNumeroUnidad(uf.getNumero());
        d.setPisoUnidad(uf.getPiso());
        d.setPorcentual(uf.getPorcentual());
        d.setExpensaOrdinaria(expensaOrd);
        d.setExpensaExtraordinaria(expensaExtra);
        d.setMoraAplicada(mora);
        d.setTotalAPagar(total);
        d.setSaldoDeudor(total); // Inicia igual al total; se reduce con cada pago
        return d;
    }

    public List<Liquidacion> listarPorConsorcio(int idConsorcio) throws SQLException {
        return liquidacionDAO.findByConsorcio(idConsorcio);
    }

    public List<LiquidacionDetalle> listarDetalles(int idLiquidacion) throws SQLException {
        return liquidacionDAO.findDetallesByLiquidacion(idLiquidacion);
    }
}
