package com.consorcioplus.view.reportes;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.LiquidacionController;
import com.consorcioplus.controller.ReclamoController;
import com.consorcioplus.controller.ReporteController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.model.entity.Reclamo;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportesPanel — panel Swing con tres reportes del sistema.
 *
 * Auto-contenido: el usuario elige el consorcio y la liquidación desde el panel.
 *
 * Reporte 1: Estado de Deuda        → datos reales de BD, ordenados con QuickSort
 * Reporte 2: Historial de Reclamos  → datos reales de BD, ordenados con InsertionSort
 * Reporte 3: Balance Financiero     → agregación con Stream API + reduce()
 *
 * Conceptos Java del Módulo 3:
 *  - JTabbedPane con tres solapas
 *  - ArrayList ordenable en memoria con QuickSort e InsertionSort
 *  - Stream API para agregación financiera
 */
public class ReportesPanel extends JPanel {

    private final ReporteController     controller    = new ReporteController();
    private final LiquidacionController liqCtrl       = new LiquidacionController();
    private final ReclamoController     reclamoCtrl   = new ReclamoController();
    private final ConsorcioController   consorcioCtrl = new ConsorcioController();

    // Selección
    private JComboBox<Consorcio>   cbConsorcio;
    private JComboBox<Liquidacion> cbLiquidacion;

    // ─── Tab 1: Estado de Deuda ──────────────────────────────
    private DefaultTableModel modeloDeuda;
    private JLabel lblDeudaTotal;
    private JLabel lblAlgoritmoDeuda;

    // ─── Tab 2: Reclamos ─────────────────────────────────────
    private DefaultTableModel modeloReclamos;

    // ─── Tab 3: Balance ──────────────────────────────────────
    private JTextArea txtBalance;

    public ReportesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarConsorcios();
    }

    private void construirUI() {

        // ── ENCABEZADO con selector ───────────────────────────────────────
        JPanel pHeader = new JPanel(new BorderLayout(10, 0));
        pHeader.setBackground(AppColors.FONDO_PANEL);
        pHeader.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel titulo = new JLabel("📊  Reportes del Sistema");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);
        pHeader.add(titulo, BorderLayout.WEST);

        JPanel pSelector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pSelector.setBackground(AppColors.FONDO_PANEL);

        pSelector.add(new JLabel("Consorcio:"));
        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.setPreferredSize(new Dimension(200, 30));
        cbConsorcio.addActionListener(e -> onConsorcioSeleccionado());
        pSelector.add(cbConsorcio);

        pSelector.add(new JLabel("Liquidación:"));
        cbLiquidacion = new JComboBox<>();
        cbLiquidacion.setFont(AppColors.FUENTE_NORMAL);
        cbLiquidacion.setPreferredSize(new Dimension(130, 30));
        pSelector.add(cbLiquidacion);

        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setFont(AppColors.FUENTE_BOTON);
        btnActualizar.setBackground(AppColors.AZUL_OSCURO);
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setOpaque(true);
        btnActualizar.addActionListener(e -> actualizarTodos());
        pSelector.add(btnActualizar);

        pHeader.add(pSelector, BorderLayout.EAST);
        add(pHeader, BorderLayout.NORTH);

        // ── PESTAÑAS ─────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppColors.FUENTE_SUBTITULO);
        tabs.addTab("💰 Estado de Deuda",   construirTabDeuda());
        tabs.addTab("🔧 Reclamos Activos",  construirTabReclamos());
        tabs.addTab("📈 Balance Financiero", construirTabBalance());
        add(tabs, BorderLayout.CENTER);
    }

    // =====================================================================
    // Construcción de pestañas
    // =====================================================================

    private JPanel construirTabDeuda() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(AppColors.FONDO_CARD);

        JPanel pInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pInfo.setBackground(AppColors.FONDO_CARD);

        lblDeudaTotal = new JLabel("Deuda total: $0,00");
        lblDeudaTotal.setFont(AppColors.FUENTE_SUBTITULO);
        lblDeudaTotal.setForeground(AppColors.TEXTO_ERROR);

        lblAlgoritmoDeuda = new JLabel("[ Ordenado con: QuickSort O(n log n) — mayor deuda primero ]");
        lblAlgoritmoDeuda.setFont(AppColors.FUENTE_PEQUEÑA);
        lblAlgoritmoDeuda.setForeground(AppColors.TEXTO_GRIS);

        pInfo.add(lblDeudaTotal);
        pInfo.add(lblAlgoritmoDeuda);
        p.add(pInfo, BorderLayout.NORTH);

        String[] cols = {"UF", "Piso", "Expensa Ord.", "Expensa Extra.", "Mora", "Total", "Saldo Deudor"};
        modeloDeuda = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloDeuda);
        configurarTabla(tabla);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    private JPanel construirTabReclamos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(AppColors.FONDO_CARD);

        JLabel lbl = new JLabel("[ Ordenado con: InsertionSort — más reciente primero ]");
        lbl.setFont(AppColors.FUENTE_PEQUEÑA);
        lbl.setForeground(AppColors.TEXTO_GRIS);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Nro. Reclamo", "UF", "Descripción", "Estado", "Fecha Alta", "Días Abierto"};
        modeloReclamos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloReclamos);
        configurarTabla(tabla);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    private JPanel construirTabBalance() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(AppColors.FONDO_CARD);

        JLabel lbl = new JLabel("Resumen financiero de la liquidación seleccionada — Stream API + reduce()");
        lbl.setFont(AppColors.FUENTE_PEQUEÑA);
        lbl.setForeground(AppColors.TEXTO_GRIS);
        p.add(lbl, BorderLayout.NORTH);

        txtBalance = new JTextArea();
        txtBalance.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtBalance.setEditable(false);
        txtBalance.setBackground(AppColors.FONDO_TABLA);
        txtBalance.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(txtBalance), BorderLayout.CENTER);
        return p;
    }

    // =====================================================================
    // Carga de datos
    // =====================================================================

    private void cargarConsorcios() {
        cbConsorcio.removeAllItems();
        cbLiquidacion.removeAllItems();
        try {
            List<Consorcio> lista = consorcioCtrl.listarActivos();
            for (Consorcio c : lista) cbConsorcio.addItem(c);
        } catch (SQLException ex) {
            mostrarError("Error al cargar consorcios: " + ex.getMessage());
        }
    }

    private void onConsorcioSeleccionado() {
        cbLiquidacion.removeAllItems();
        Consorcio c = (Consorcio) cbConsorcio.getSelectedItem();
        if (c == null) return;
        try {
            List<Liquidacion> lista = liqCtrl.listarPorConsorcio(c.getId());
            for (Liquidacion l : lista) cbLiquidacion.addItem(l);
        } catch (SQLException ex) {
            mostrarError("Error al cargar liquidaciones: " + ex.getMessage());
        }
    }

    public void actualizarTodos() {
        Liquidacion liq = (Liquidacion) cbLiquidacion.getSelectedItem();
        Consorcio   con = (Consorcio)   cbConsorcio.getSelectedItem();

        List<LiquidacionDetalle> detalles = new ArrayList<>();
        if (liq != null) {
            try {
                detalles = liqCtrl.listarDetalles(liq.getId());
            } catch (SQLException ex) {
                mostrarError("Error al cargar detalles: " + ex.getMessage());
            }
        }

        List<Reclamo> reclamos = new ArrayList<>();
        if (con != null) {
            try {
                reclamos = reclamoCtrl.obtenerTodos(con.getId());
            } catch (SQLException ex) {
                mostrarError("Error al cargar reclamos: " + ex.getMessage());
            }
        }

        cargarReporteDeuda(detalles);
        cargarReporteReclamos(reclamos);
        cargarBalance(detalles, con);
    }

    private void cargarReporteDeuda(List<LiquidacionDetalle> detalles) {
        modeloDeuda.setRowCount(0);
        controller.ordenarPorDeudaQuickSort(detalles);
        BigDecimal total = controller.calcularDeudaTotal(detalles);
        lblDeudaTotal.setText("Deuda total: $" + total.toPlainString());

        if (detalles.isEmpty()) {
            lblDeudaTotal.setText("Seleccioná un consorcio y una liquidación para ver el reporte.");
            return;
        }
        for (LiquidacionDetalle d : detalles) {
            modeloDeuda.addRow(new Object[]{
                    "UF " + d.getNumeroUnidad(),
                    d.getPisoUnidad(),
                    "$" + d.getExpensaOrdinaria().toPlainString(),
                    "$" + d.getExpensaExtraordinaria().toPlainString(),
                    "$" + d.getMoraAplicada().toPlainString(),
                    "$" + d.getTotalAPagar().toPlainString(),
                    "$" + d.getSaldoDeudor().toPlainString()
            });
        }
    }

    private void cargarReporteReclamos(List<Reclamo> reclamos) {
        modeloReclamos.setRowCount(0);
        controller.ordenarReclamosPorFechaInsertionSort(reclamos);
        for (Reclamo r : reclamos) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                    r.getFechaAlta().toLocalDate(), java.time.LocalDate.now());
            modeloReclamos.addRow(new Object[]{
                    r.getNroReclamo(),
                    "UF#" + r.getIdUnidad(),
                    r.getDescripcion(),
                    r.getEstado().name(),
                    r.getFechaAlta().toLocalDate(),
                    dias + " días"
            });
        }
    }

    private void cargarBalance(List<LiquidacionDetalle> detalles, Consorcio consorcio) {
        BigDecimal totalDeuda        = controller.calcularDeudaTotal(detalles);
        List<LiquidacionDetalle> deudores = controller.filtrarDeudores(detalles);
        BigDecimal totalOrdinario    = detalles.stream()
                .map(LiquidacionDetalle::getExpensaOrdinaria)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExtraordinario = detalles.stream()
                .map(LiquidacionDetalle::getExpensaExtraordinaria)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Liquidacion liq = (Liquidacion) cbLiquidacion.getSelectedItem();
        String periodo = liq != null
                ? liq.getPeriodo().getMonthValue() + "/" + liq.getPeriodo().getYear()
                : "—";
        String nombreConsorcio = consorcio != null ? consorcio.getNombre() : "—";

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════════════\n");
        sb.append("  BALANCE FINANCIERO — ConsorCio+\n");
        sb.append("══════════════════════════════════════════════════\n\n");
        sb.append(String.format("  Consorcio                  : %s%n",    nombreConsorcio));
        sb.append(String.format("  Período                    : %s%n%n",  periodo));
        sb.append(String.format("  Total expensas ordinarias  : $%-12s%n", totalOrdinario.toPlainString()));
        sb.append(String.format("  Total expensas extraord.   : $%-12s%n", totalExtraordinario.toPlainString()));
        sb.append(String.format("  Deuda total pendiente      : $%-12s%n", totalDeuda.toPlainString()));
        sb.append(String.format("  Unidades con deuda         : %-5d%n",   deudores.size()));
        sb.append(String.format("  Total unidades liquidadas  : %-5d%n",   detalles.size()));
        if (!detalles.isEmpty()) {
            int pct = (int) Math.round((deudores.size() * 100.0) / detalles.size());
            sb.append(String.format("  Porcentaje de morosidad    : %d%%%n%n", pct));
        }
        sb.append("  ── Algoritmo: Stream API + reduce() ──\n");
        sb.append("══════════════════════════════════════════════════\n");
        txtBalance.setText(sb.toString());
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    private void configurarTabla(JTable tabla) {
        tabla.setFont(AppColors.FUENTE_TABLA);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tabla.getTableHeader().setBackground(AppColors.AZUL_OSCURO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
