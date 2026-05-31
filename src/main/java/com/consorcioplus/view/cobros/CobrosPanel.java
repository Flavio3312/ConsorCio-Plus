package com.consorcioplus.view.cobros;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.LiquidacionController;
import com.consorcioplus.controller.PagoController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.model.entity.Pago;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * CobrosPanel — panel Swing para registrar pagos de expensas.
 *
 * Auto-contenido: el usuario selecciona Consorcio → Liquidación → UF
 * directamente desde este panel, sin necesidad de navegar a Liquidaciones.
 *
 * Conceptos Java del Módulo 3:
 *  - Sobrecarga de métodos: registrarPago() con y sin número de recibo
 *  - JTable para historial de pagos parciales (LinkedList en controller)
 *  - JComboBox encadenado (consorcio → liquidación → detalle)
 */
public class CobrosPanel extends JPanel {

    private final PagoController        pagoCtrl        = new PagoController();
    private final LiquidacionController liqCtrl         = new LiquidacionController();
    private final ConsorcioController   consorcioCtrl   = new ConsorcioController();

    // --- Selección encadenada ---
    private JComboBox<Consorcio>    cbConsorcio;
    private JComboBox<Liquidacion>  cbLiquidacion;

    // --- Tabla de UFs con deuda ---
    private JTable            tblDetalles;
    private DefaultTableModel modeloDetalles;
    private List<LiquidacionDetalle> detallesCargados;

    // --- Formulario de pago ---
    private JLabel     lblUnidad;
    private JLabel     lblSaldoPendiente;
    private JTextField txtMonto;
    private JTextField txtNroRecibo;
    private JButton    btnPagarAuto;
    private JButton    btnPagarManual;

    // --- Historial de pagos ---
    private JTable            tblHistorial;
    private DefaultTableModel modeloHistorial;

    // Detalle seleccionado actualmente
    private LiquidacionDetalle detalleActual;

    public CobrosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarConsorcios();
    }

    // =====================================================================
    // Construcción de la UI
    // =====================================================================
    private void construirUI() {

        // ── TÍTULO ────────────────────────────────────────────────────────
        JLabel titulo = new JLabel("💳  Registro de Cobros");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // ── PANEL IZQUIERDO: selector + formulario ─────────────────────────
        JPanel pIzq = new JPanel(new BorderLayout(0, 10));
        pIzq.setBackground(AppColors.FONDO_PANEL);
        pIzq.setPreferredSize(new Dimension(320, 0));

        // -- Selector encadenado --
        JPanel pSelector = new JPanel(new GridBagLayout());
        pSelector.setBackground(AppColors.FONDO_CARD);
        pSelector.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDE_PANEL),
                "Selección", 0, 0, AppColors.FUENTE_SUBTITULO, AppColors.AZUL_OSCURO));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 10, 6, 10);
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        g.gridx = 0; g.gridy = 0;
        pSelector.add(etiqueta("Consorcio:"), g);
        g.gridy = 1;
        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.addActionListener(e -> onConsorcioSeleccionado());
        pSelector.add(cbConsorcio, g);

        g.gridy = 2;
        pSelector.add(etiqueta("Liquidación:"), g);
        g.gridy = 3;
        cbLiquidacion = new JComboBox<>();
        cbLiquidacion.setFont(AppColors.FUENTE_NORMAL);
        cbLiquidacion.addActionListener(e -> onLiquidacionSeleccionada());
        pSelector.add(cbLiquidacion, g);

        pIzq.add(pSelector, BorderLayout.NORTH);

        // -- Formulario de pago --
        JPanel pFormulario = new JPanel(new GridBagLayout());
        pFormulario.setBackground(AppColors.FONDO_CARD);
        pFormulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDE_PANEL),
                "Datos del pago", 0, 0, AppColors.FUENTE_SUBTITULO, AppColors.AZUL_OSCURO));

        GridBagConstraints gf = new GridBagConstraints();
        gf.insets  = new Insets(7, 10, 7, 10);
        gf.anchor  = GridBagConstraints.WEST;
        gf.fill    = GridBagConstraints.HORIZONTAL;
        gf.weightx = 1.0;

        gf.gridx = 0; gf.gridy = 0;
        pFormulario.add(etiqueta("Unidad Funcional:"), gf);
        gf.gridy = 1;
        lblUnidad = new JLabel("— seleccione una UF —");
        lblUnidad.setFont(AppColors.FUENTE_NORMAL);
        lblUnidad.setForeground(AppColors.TEXTO_GRIS);
        pFormulario.add(lblUnidad, gf);

        gf.gridy = 2;
        pFormulario.add(etiqueta("Saldo Pendiente:"), gf);
        gf.gridy = 3;
        lblSaldoPendiente = new JLabel("$0,00");
        lblSaldoPendiente.setFont(AppColors.FUENTE_SUBTITULO);
        lblSaldoPendiente.setForeground(AppColors.TEXTO_ERROR);
        pFormulario.add(lblSaldoPendiente, gf);

        gf.gridy = 4;
        pFormulario.add(etiqueta("Monto a pagar ($):"), gf);
        gf.gridy = 5;
        txtMonto = new JTextField(12);
        txtMonto.setFont(AppColors.FUENTE_NORMAL);
        pFormulario.add(txtMonto, gf);

        gf.gridy = 6;
        pFormulario.add(etiqueta("Nro. Recibo (opcional):"), gf);
        gf.gridy = 7;
        txtNroRecibo = new JTextField(12);
        txtNroRecibo.setFont(AppColors.FUENTE_NORMAL);
        txtNroRecibo.setToolTipText("Dejar vacío para generar automáticamente");
        pFormulario.add(txtNroRecibo, gf);

        gf.gridy = 8; gf.fill = GridBagConstraints.NONE; gf.anchor = GridBagConstraints.CENTER;
        JPanel pBotones = new JPanel(new GridLayout(2, 1, 0, 6));
        pBotones.setBackground(AppColors.FONDO_CARD);

        btnPagarAuto   = crearBoton("💳 Registrar Pago (auto)", AppColors.AZUL_MEDIO);
        btnPagarManual = crearBoton("🖨  Con Nro. Recibo manual", AppColors.AZUL_OSCURO);
        btnPagarAuto.setToolTipText("El número de recibo se genera automáticamente");
        btnPagarManual.setToolTipText("Completar el campo Nro. Recibo antes de registrar");
        btnPagarAuto.addActionListener(e -> registrarPagoAuto());
        btnPagarManual.addActionListener(e -> registrarPagoManual());

        pBotones.add(btnPagarAuto);
        pBotones.add(btnPagarManual);
        pFormulario.add(pBotones, gf);

        pIzq.add(pFormulario, BorderLayout.CENTER);
        add(pIzq, BorderLayout.WEST);

        // ── PANEL CENTRAL: tabla de UFs + historial ───────────────────────
        JPanel pCentro = new JPanel(new BorderLayout(0, 10));
        pCentro.setBackground(AppColors.FONDO_PANEL);

        // Tabla de UFs con deuda
        String[] colsDet = {"UF", "Piso", "Total", "Saldo Deudor"};
        modeloDetalles = new DefaultTableModel(colsDet, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetalles = new JTable(modeloDetalles);
        tblDetalles.setFont(AppColors.FUENTE_TABLA);
        tblDetalles.setRowHeight(26);
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDetalles.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tblDetalles.getTableHeader().setBackground(AppColors.AZUL_OSCURO);
        tblDetalles.getTableHeader().setForeground(Color.WHITE);
        tblDetalles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onDetalleSeleccionado();
        });

        JScrollPane scrollDet = new JScrollPane(tblDetalles);
        scrollDet.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDE_PANEL),
                "Unidades Funcionales — seleccioná una para cobrar",
                0, 0, AppColors.FUENTE_SUBTITULO, AppColors.AZUL_OSCURO));

        // Historial de pagos
        String[] colsHist = {"Nro. Recibo", "Fecha", "Monto Pagado"};
        modeloHistorial = new DefaultTableModel(colsHist, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHistorial = new JTable(modeloHistorial);
        tblHistorial.setFont(AppColors.FUENTE_TABLA);
        tblHistorial.setRowHeight(24);
        tblHistorial.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tblHistorial.getTableHeader().setBackground(AppColors.AZUL_OSCURO);
        tblHistorial.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollHist = new JScrollPane(tblHistorial);
        scrollHist.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AppColors.BORDE_PANEL),
                "Historial de Pagos Parciales",
                0, 0, AppColors.FUENTE_SUBTITULO, AppColors.AZUL_OSCURO));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDet, scrollHist);
        split.setResizeWeight(0.55);
        split.setDividerLocation(220);
        pCentro.add(split, BorderLayout.CENTER);

        add(pCentro, BorderLayout.CENTER);
    }

    // =====================================================================
    // Lógica de carga encadenada
    // =====================================================================

    private void cargarConsorcios() {
        cbConsorcio.removeAllItems();
        cbLiquidacion.removeAllItems();
        modeloDetalles.setRowCount(0);
        try {
            List<Consorcio> lista = consorcioCtrl.listarActivos();
            if (lista.isEmpty()) {
                mostrarError("No hay consorcios activos. Cree uno primero.");
                return;
            }
            for (Consorcio c : lista) cbConsorcio.addItem(c);
        } catch (SQLException ex) {
            mostrarError("Error al cargar consorcios: " + ex.getMessage());
        }
    }

    private void onConsorcioSeleccionado() {
        cbLiquidacion.removeAllItems();
        modeloDetalles.setRowCount(0);
        limpiarFormulario();
        Consorcio c = (Consorcio) cbConsorcio.getSelectedItem();
        if (c == null) return;
        try {
            List<Liquidacion> lista = liqCtrl.listarPorConsorcio(c.getId());
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Este consorcio no tiene liquidaciones ejecutadas todavía.\n"
                        + "Vaya a Liquidaciones y ejecute una primero.",
                        "Sin liquidaciones", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (Liquidacion l : lista) cbLiquidacion.addItem(l);
        } catch (SQLException ex) {
            mostrarError("Error al cargar liquidaciones: " + ex.getMessage());
        }
    }

    private void onLiquidacionSeleccionada() {
        modeloDetalles.setRowCount(0);
        detallesCargados = null;
        limpiarFormulario();
        Liquidacion liq = (Liquidacion) cbLiquidacion.getSelectedItem();
        if (liq == null) return;
        try {
            detallesCargados = liqCtrl.listarDetalles(liq.getId());
            for (LiquidacionDetalle d : detallesCargados) {
                modeloDetalles.addRow(new Object[]{
                        "UF " + d.getNumeroUnidad(),
                        d.getPisoUnidad(),
                        "$" + d.getTotalAPagar().toPlainString(),
                        "$" + d.getSaldoDeudor().toPlainString()
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar detalle: " + ex.getMessage());
        }
    }

    private void onDetalleSeleccionado() {
        int fila = tblDetalles.getSelectedRow();
        if (fila < 0 || detallesCargados == null || fila >= detallesCargados.size()) return;
        detalleActual = detallesCargados.get(fila);
        lblUnidad.setText("UF " + detalleActual.getNumeroUnidad() + " — Piso " + detalleActual.getPisoUnidad());
        lblUnidad.setForeground(AppColors.AZUL_OSCURO);
        lblSaldoPendiente.setText("$" + detalleActual.getSaldoDeudor().toPlainString());
        txtMonto.setText("");
        txtNroRecibo.setText("");
        cargarHistorial();
    }

    // =====================================================================
    // Registro de pagos (sobrecarga)
    // =====================================================================

    private void registrarPagoAuto() {
        if (detalleActual == null) { mostrarError("Seleccione una Unidad Funcional de la tabla."); return; }
        BigDecimal monto = parseMonto();
        if (monto == null) return;
        try {
            Pago pago = pagoCtrl.registrarPago(detalleActual.getId(), monto);
            JOptionPane.showMessageDialog(this,
                    "✅ Pago registrado.\nRecibo: " + pago.getNroRecibo(),
                    "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);
            actualizarSaldo();
            cargarHistorial();
            txtMonto.setText("");
        } catch (Exception ex) {
            mostrarError("Error al registrar el pago: " + ex.getMessage());
        }
    }

    private void registrarPagoManual() {
        if (detalleActual == null) { mostrarError("Seleccione una Unidad Funcional de la tabla."); return; }
        BigDecimal monto  = parseMonto();
        String     nroRec = txtNroRecibo.getText().trim();
        if (monto == null) return;
        if (nroRec.isEmpty()) { mostrarError("Ingrese el número de recibo en el campo correspondiente."); return; }
        try {
            Pago pago = pagoCtrl.registrarPago(detalleActual.getId(), monto, nroRec);
            JOptionPane.showMessageDialog(this,
                    "✅ Pago registrado.\nRecibo: " + pago.getNroRecibo(),
                    "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);
            actualizarSaldo();
            cargarHistorial();
            txtMonto.setText("");
            txtNroRecibo.setText("");
        } catch (Exception ex) {
            mostrarError("Error al registrar el pago: " + ex.getMessage());
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        if (detalleActual == null) return;
        try {
            List<Pago> pagos = pagoCtrl.obtenerPagosPorDetalle(detalleActual.getId());
            for (Pago p : pagos) {
                modeloHistorial.addRow(new Object[]{
                        p.getNroRecibo(),
                        p.getFechaPago().toLocalDate(),
                        "$" + p.getMontoPagado().toPlainString()
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar historial: " + ex.getMessage());
        }
    }

    private void actualizarSaldo() {
        try {
            BigDecimal totalPagado = pagoCtrl.calcularTotalPagado(detalleActual.getId());
            BigDecimal nuevo = detalleActual.getTotalAPagar().subtract(totalPagado).max(BigDecimal.ZERO);
            detalleActual.setSaldoDeudor(nuevo);
            lblSaldoPendiente.setText("$" + nuevo.toPlainString());
            // actualizar celda en tabla
            int fila = tblDetalles.getSelectedRow();
            if (fila >= 0) modeloDetalles.setValueAt("$" + nuevo.toPlainString(), fila, 3);
        } catch (SQLException ignored) {}
    }

    private void limpiarFormulario() {
        detalleActual = null;
        lblUnidad.setText("— seleccione una UF —");
        lblUnidad.setForeground(AppColors.TEXTO_GRIS);
        lblSaldoPendiente.setText("$0,00");
        txtMonto.setText("");
        txtNroRecibo.setText("");
        modeloHistorial.setRowCount(0);
    }

    private BigDecimal parseMonto() {
        String txt = txtMonto.getText().trim().replace(",", ".");
        if (txt.isEmpty()) { mostrarError("Ingrese un monto."); return null; }
        try {
            BigDecimal m = new BigDecimal(txt);
            if (m.compareTo(BigDecimal.ZERO) <= 0) { mostrarError("El monto debe ser mayor a cero."); return null; }
            return m;
        } catch (NumberFormatException e) {
            mostrarError("El monto debe ser un número válido (ej: 1500.50).");
            return null;
        }
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppColors.FUENTE_NORMAL);
        lbl.setForeground(AppColors.TEXTO_OSCURO);
        return lbl;
    }

    private JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
