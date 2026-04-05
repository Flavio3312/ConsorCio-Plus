package com.consorcioplus.view.liquidacion;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.LiquidacionController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.Liquidacion;
import com.consorcioplus.model.entity.LiquidacionDetalle;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Panel de Liquidación Mensual: permite ejecutar la liquidación y ver el detalle. */
public class LiquidacionPanel extends JPanel {

    private final LiquidacionController liquidacionController = new LiquidacionController();
    private final ConsorcioController   consorcioController   = new ConsorcioController();

    private JComboBox<Consorcio> cbConsorcio;
    private JSpinner             spnMes;
    private JSpinner             spnAnio;
    private DefaultTableModel    modeloHistorial;
    private DefaultTableModel    modeloDetalle;
    private JTable               tablaHistorial;
    private JTable               tablaDetalle;

    public LiquidacionPanel() {
        super(new BorderLayout(0, 0));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarConsorcios();
    }

    private void construirUI() {
        // ── HEADER ────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.FONDO_CARD);
        header.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel titulo = new JLabel("Liquidación Mensual de Expensas");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setBackground(AppColors.FONDO_CARD);

        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.setPreferredSize(new Dimension(220, 32));

        LocalDate hoy = LocalDate.now();
        spnMes  = new JSpinner(new SpinnerNumberModel(hoy.getMonthValue(), 1, 12, 1));
        spnAnio = new JSpinner(new SpinnerNumberModel(hoy.getYear(), 2020, 2099, 1));
        spnMes.setPreferredSize(new Dimension(55, 32));
        spnAnio.setPreferredSize(new Dimension(75, 32));

        JButton btnEjecutar = new JButton("▶ Ejecutar Liquidación");
        estilizarBoton(btnEjecutar, AppColors.AZUL_MEDIO);
        btnEjecutar.addActionListener(e -> ejecutarLiquidacion());

        JButton btnVerHistorial = new JButton("Ver Historial");
        estilizarBoton(btnVerHistorial, AppColors.TEXTO_GRIS);
        btnVerHistorial.addActionListener(e -> cargarHistorial());

        controles.add(new JLabel("Consorcio:"));
        controles.add(cbConsorcio);
        controles.add(new JLabel("Mes:"));
        controles.add(spnMes);
        controles.add(new JLabel("Año:"));
        controles.add(spnAnio);
        controles.add(btnVerHistorial);
        controles.add(btnEjecutar);

        header.add(titulo,    BorderLayout.WEST);
        header.add(controles, BorderLayout.EAST);

        // ── CUERPO: HISTORIAL + DETALLE ───────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380);
        split.setResizeWeight(0.35);

        // Historial
        String[] colsHist = {"ID", "Período", "Total Ord.", "Total Ext.", "Cerrada"};
        modeloHistorial = new DefaultTableModel(colsHist, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorial = new JTable(modeloHistorial);
        estilizarTabla(tablaHistorial);
        tablaHistorial.getColumnModel().getColumn(0).setMaxWidth(40);
        tablaHistorial.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarDetalleSeleccionado();
        });
        JScrollPane scrollHist = new JScrollPane(tablaHistorial);
        scrollHist.setBorder(BorderFactory.createTitledBorder("Historial de Liquidaciones"));

        // Detalle por UF
        String[] colsDet = {"UF", "Piso", "%", "Ord.", "Ext.", "Mora", "Total", "Saldo"};
        modeloDetalle = new DefaultTableModel(colsDet, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDetalle = new JTable(modeloDetalle);
        estilizarTabla(tablaDetalle);
        JScrollPane scrollDet = new JScrollPane(tablaDetalle);
        scrollDet.setBorder(BorderFactory.createTitledBorder("Detalle por Unidad Funcional"));

        split.setLeftComponent(scrollHist);
        split.setRightComponent(scrollDet);

        add(header, BorderLayout.NORTH);
        add(split,  BorderLayout.CENTER);
    }

    private void cargarConsorcios() {
        try {
            List<Consorcio> lista = consorcioController.listarActivos();
            for (Consorcio c : lista) cbConsorcio.addItem(c);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarLiquidacion() {
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) return;
        int mes  = (int) spnMes.getValue();
        int anio = (int) spnAnio.getValue();
        LocalDate periodo = LocalDate.of(anio, mes, 1);

        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Ejecutar la liquidación del período " + mes + "/" + anio
            + " para el consorcio\n\"" + consorcio.getNombre() + "\"?\n\n"
            + "Esta acción cerrará el período y no podrá revertirse.",
            "Confirmar Liquidación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmar != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Liquidacion liq = liquidacionController.ejecutarLiquidacion(consorcio.getId(), periodo);
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,
                "Liquidación ejecutada exitosamente.\n"
                + "Total Ordinario: $" + liq.getTotalOrdinario().toPlainString() + "\n"
                + "Total Extraordinario: $" + liq.getTotalExtraordinario().toPlainString() + "\n"
                + "Total General: $" + liq.getTotalGeneral().toPlainString(),
                "Liquidación Completada", JOptionPane.INFORMATION_MESSAGE);
            cargarHistorial();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHistorial() {
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) return;
        modeloHistorial.setRowCount(0);
        try {
            List<Liquidacion> lista = liquidacionController.listarPorConsorcio(consorcio.getId());
            for (Liquidacion l : lista) {
                modeloHistorial.addRow(new Object[]{
                    l.getId(),
                    l.getPeriodo().getMonthValue() + "/" + l.getPeriodo().getYear(),
                    "$" + l.getTotalOrdinario().toPlainString(),
                    "$" + l.getTotalExtraordinario().toPlainString(),
                    l.isCerrada() ? "Sí" : "No"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalleSeleccionado() {
        int fila = tablaHistorial.getSelectedRow();
        if (fila < 0) return;
        int idLiq = (int) modeloHistorial.getValueAt(fila, 0);
        modeloDetalle.setRowCount(0);
        try {
            List<LiquidacionDetalle> detalles = liquidacionController.listarDetalles(idLiq);
            for (LiquidacionDetalle d : detalles) {
                modeloDetalle.addRow(new Object[]{
                    d.getNumeroUnidad(), d.getPisoUnidad(),
                    d.getPorcentual().toPlainString() + "%",
                    "$" + d.getExpensaOrdinaria().toPlainString(),
                    "$" + d.getExpensaExtraordinaria().toPlainString(),
                    "$" + d.getMoraAplicada().toPlainString(),
                    "$" + d.getTotalAPagar().toPlainString(),
                    "$" + d.getSaldoDeudor().toPlainString()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void estilizarTabla(JTable t) {
        t.setFont(AppColors.FUENTE_TABLA);
        t.setRowHeight(26);
        t.setGridColor(AppColors.BORDE_PANEL);
        t.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        t.getTableHeader().setBackground(AppColors.AZUL_CLARO);
        t.getTableHeader().setForeground(AppColors.AZUL_OSCURO);
        t.setSelectionBackground(AppColors.AZUL_CLARO);
    }

    private void estilizarBoton(JButton btn, Color bg) {
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBackground(bg);
        btn.setForeground(AppColors.TEXTO_BLANCO);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
    }
}
