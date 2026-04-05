package com.consorcioplus.view.gasto;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.GastoController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.Gasto;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Panel de gestión de Gastos mensuales. */
public class GastoListPanel extends JPanel {

    private final GastoController     gastoController    = new GastoController();
    private final ConsorcioController consorcioController = new ConsorcioController();

    private JComboBox<Consorcio> cbConsorcio;
    private JSpinner             spnMes;
    private JSpinner             spnAnio;
    private DefaultTableModel    modelo;
    private JTable               tabla;
    private JLabel               lblTotalOrd;
    private JLabel               lblTotalExt;

    public GastoListPanel() {
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

        JLabel titulo = new JLabel("Gestión de Gastos");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);

        // Filtros
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setBackground(AppColors.FONDO_CARD);

        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.setPreferredSize(new Dimension(220, 32));

        LocalDate hoy = LocalDate.now();
        spnMes  = new JSpinner(new SpinnerNumberModel(hoy.getMonthValue(), 1, 12, 1));
        spnAnio = new JSpinner(new SpinnerNumberModel(hoy.getYear(), 2020, 2099, 1));
        spnMes.setPreferredSize(new Dimension(55, 32));
        spnAnio.setPreferredSize(new Dimension(75, 32));

        JButton btnBuscar = new JButton("Buscar");
        estilizarBoton(btnBuscar, AppColors.AZUL_MEDIO);
        btnBuscar.addActionListener(e -> cargarGastos());

        JButton btnNuevo = new JButton("+ Agregar Gasto");
        estilizarBoton(btnNuevo, AppColors.TEXTO_EXITO);
        btnNuevo.addActionListener(e -> agregarGasto());

        filtros.add(new JLabel("Consorcio:"));
        filtros.add(cbConsorcio);
        filtros.add(new JLabel("Mes:"));
        filtros.add(spnMes);
        filtros.add(new JLabel("Año:"));
        filtros.add(spnAnio);
        filtros.add(btnBuscar);
        filtros.add(btnNuevo);

        header.add(titulo,  BorderLayout.WEST);
        header.add(filtros, BorderLayout.EAST);

        // ── TABLA ─────────────────────────────────────────
        String[] cols = {"ID", "Categoría", "Descripción", "Monto", "Nro. Factura", "Proveedor"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setFont(AppColors.FUENTE_TABLA);
        tabla.setRowHeight(28);
        tabla.setGridColor(AppColors.BORDE_PANEL);
        tabla.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tabla.getTableHeader().setBackground(AppColors.AZUL_CLARO);
        tabla.getTableHeader().setForeground(AppColors.AZUL_OSCURO);
        tabla.setSelectionBackground(AppColors.AZUL_CLARO);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(AppColors.FONDO_TABLA);

        // ── FOOTER CON TOTALES ────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        footer.setBackground(AppColors.FONDO_CARD);
        lblTotalOrd = new JLabel("Ordinario: $0.00");
        lblTotalExt = new JLabel("Extraordinario: $0.00");
        lblTotalOrd.setFont(AppColors.FUENTE_BOTON);
        lblTotalExt.setFont(AppColors.FUENTE_BOTON);
        lblTotalOrd.setForeground(AppColors.AZUL_OSCURO);
        lblTotalExt.setForeground(AppColors.AZUL_OSCURO);

        JButton btnEliminar = new JButton("🗑 Eliminar");
        estilizarBoton(btnEliminar, AppColors.TEXTO_ERROR);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        footer.add(lblTotalOrd);
        footer.add(new JLabel("|"));
        footer.add(lblTotalExt);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(btnEliminar);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void cargarConsorcios() {
        try {
            List<Consorcio> lista = consorcioController.listarActivos();
            for (Consorcio c : lista) cbConsorcio.addItem(c);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar consorcios: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarGastos() {
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) return;
        int mes  = (int) spnMes.getValue();
        int anio = (int) spnAnio.getValue();
        LocalDate periodo = LocalDate.of(anio, mes, 1);

        modelo.setRowCount(0);
        BigDecimal totalOrd = BigDecimal.ZERO;
        BigDecimal totalExt = BigDecimal.ZERO;

        try {
            List<Gasto> lista = gastoController.listarPorConsorcioYPeriodo(consorcio.getId(), periodo);
            for (Gasto g : lista) {
                modelo.addRow(new Object[]{
                    g.getId(), g.getCategoria(), g.getDescripcion(),
                    "$" + g.getMonto().toPlainString(),
                    g.getNroFactura() != null ? g.getNroFactura() : "—",
                    g.getNombreProveedor() != null ? g.getNombreProveedor() : "—"
                });
                if ("ORDINARIO".equals(g.getCategoria())) totalOrd = totalOrd.add(g.getMonto());
                else totalExt = totalExt.add(g.getMonto());
            }
            lblTotalOrd.setText("Ordinario: $" + totalOrd.toPlainString());
            lblTotalExt.setText("Extraordinario: $" + totalExt.toPlainString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarGasto() {
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un consorcio primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mes  = (int) spnMes.getValue();
        int anio = (int) spnAnio.getValue();
        LocalDate periodo = LocalDate.of(anio, mes, 1);

        try {
            if (gastoController.periodoCerrado(consorcio.getId(), periodo)) {
                JOptionPane.showMessageDialog(this,
                    "El período " + mes + "/" + anio + " está cerrado. No se pueden agregar gastos.",
                    "Período Cerrado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GastoFormDialog dlg = new GastoFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            consorcio.getId(), periodo, gastoController);
        dlg.setVisible(true);
        if (dlg.fueGuardado()) cargarGastos();
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un gasto.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar este gasto?", "Confirmar",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                gastoController.eliminar(id);
                cargarGastos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
