package com.consorcioplus.view.unidad;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.UnidadFuncionalController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.UnidadFuncional;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel ABM de Unidades Funcionales.
 * Permite seleccionar un consorcio y gestionar sus UFs (Alta/Modificación/Baja lógica).
 * Muestra en tiempo real la suma de porcentuales del consorcio seleccionado.
 */
public class UnidadFuncionalListPanel extends JPanel {

    private final UnidadFuncionalController controller = new UnidadFuncionalController();
    private final ConsorcioController       consorcioCtrl = new ConsorcioController();

    private JComboBox<Consorcio> cbConsorcio;
    private DefaultTableModel    modelo;
    private JTable               tabla;
    private JLabel               lblSumaPorcentual;

    public UnidadFuncionalListPanel() {
        super(new BorderLayout(0, 0));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarConsorcios();
    }

    // ─── UI ─────────────────────────────────────────────────────────────────

    private void construirUI() {
        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.FONDO_CARD);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Gestión de Unidades Funcionales");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);

        JButton btnNueva = crearBoton("+ Nueva UF", AppColors.AZUL_MEDIO);
        btnNueva.addActionListener(e -> abrirFormulario(null));

        header.add(titulo,  BorderLayout.WEST);
        header.add(btnNueva, BorderLayout.EAST);

        // ── FILTRO POR CONSORCIO ──
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtro.setBackground(AppColors.FONDO_CARD);
        filtro.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDE_PANEL),
            new EmptyBorder(4, 12, 4, 12)
        ));

        JLabel lblConsorcio = new JLabel("Consorcio:");
        lblConsorcio.setFont(AppColors.FUENTE_NORMAL);
        lblConsorcio.setForeground(AppColors.TEXTO_GRIS);

        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.setPreferredSize(new Dimension(280, 30));
        cbConsorcio.addActionListener(e -> cargarDatos());

        lblSumaPorcentual = new JLabel("Suma porcentuales: 0.0000%");
        lblSumaPorcentual.setFont(AppColors.FUENTE_NORMAL);
        lblSumaPorcentual.setForeground(AppColors.AZUL_MEDIO);
        lblSumaPorcentual.setBorder(new EmptyBorder(0, 24, 0, 0));

        filtro.add(lblConsorcio);
        filtro.add(cbConsorcio);
        filtro.add(lblSumaPorcentual);

        // ── TABLA ──
        String[] columnas = {"ID", "Nro. UF", "Piso", "Porcentual (%)"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        tabla = new JTable(modelo);
        estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDE_PANEL));
        scroll.getViewport().setBackground(AppColors.FONDO_TABLA);

        // ── BOTONES DE ACCIÓN ──
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setBackground(AppColors.FONDO_CARD);

        JButton btnEditar = crearBoton("✏ Editar",        AppColors.AZUL_MEDIO);
        JButton btnBaja   = crearBoton("✖ Dar de Baja",   AppColors.TEXTO_ERROR);

        btnEditar.addActionListener(e -> editarSeleccionada());
        btnBaja.addActionListener(e   -> darBajaSeleccionada());

        acciones.add(btnEditar);
        acciones.add(btnBaja);

        // ── ENSAMBLADO ──
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(header, BorderLayout.NORTH);
        norte.add(filtro, BorderLayout.SOUTH);

        add(norte,   BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    // ─── CARGA DE DATOS ─────────────────────────────────────────────────────

    private void cargarConsorcios() {
        try {
            List<Consorcio> lista = consorcioCtrl.listarActivos();
            cbConsorcio.removeAllItems();
            for (Consorcio c : lista) cbConsorcio.addItem(c);
            // cargarDatos() se dispara por el addActionListener del combo
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar consorcios: " + ex.getMessage(),
                "Error de BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) return;

        try {
            List<UnidadFuncional> lista = controller.listarPorConsorcio(consorcio.getId());
            for (UnidadFuncional uf : lista) {
                modelo.addRow(new Object[]{
                    uf.getId(),
                    uf.getNumero(),
                    uf.getPiso() != null ? uf.getPiso() : "-",
                    uf.getPorcentual().toPlainString() + "%"
                });
            }
            actualizarSumaPorcentual(consorcio.getId());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar unidades: " + ex.getMessage(),
                "Error de BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarSumaPorcentual(int idConsorcio) {
        try {
            BigDecimal suma = controller.obtenerSumaPorcentuales(idConsorcio);
            boolean completo = suma.compareTo(new BigDecimal("100.0000")) == 0;
            lblSumaPorcentual.setText("Suma porcentuales: " + suma.toPlainString() + "%"
                + (completo ? "  ✔ OK" : "  ⚠ (debe ser 100%)"));
            lblSumaPorcentual.setForeground(completo ? new Color(0x16, 0xa3, 0x4a) : new Color(0xc0, 0x5a, 0x00));
        } catch (SQLException ex) {
            lblSumaPorcentual.setText("Suma: error");
        }
    }

    // ─── ACCIONES ───────────────────────────────────────────────────────────

    private void abrirFormulario(UnidadFuncional uf) {
        Consorcio consorcio = (Consorcio) cbConsorcio.getSelectedItem();
        if (consorcio == null) {
            mostrarAlerta("Seleccione un consorcio primero.");
            return;
        }
        UnidadFuncionalFormDialog dialog = new UnidadFuncionalFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            uf, consorcio, controller
        );
        dialog.setVisible(true);
        if (dialog.fueGuardado()) cargarDatos();
    }

    private void editarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarAlerta("Seleccione una Unidad Funcional para editar."); return; }
        int id = (int) modelo.getValueAt(fila, 0);
        try {
            controller.buscarPorId(id).ifPresent(this::abrirFormulario);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darBajaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarAlerta("Seleccione una Unidad Funcional para dar de baja."); return; }
        int    id  = (int)    modelo.getValueAt(fila, 0);
        String nro = (String) modelo.getValueAt(fila, 1);
        int ok = JOptionPane.showConfirmDialog(this,
            "¿Dar de baja la Unidad Funcional \"" + nro + "\"?\n"
            + "Esto la excluirá de nuevas liquidaciones.",
            "Confirmar Baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                controller.darDeBaja(id);
                cargarDatos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─── Helpers de estilo ──────────────────────────────────────────────────

    private JButton crearBoton(String txt, Color bg) {
        JButton btn = new JButton(txt);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBackground(bg);
        btn.setForeground(AppColors.TEXTO_BLANCO);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void estilizarTabla(JTable t) {
        t.setFont(AppColors.FUENTE_TABLA);
        t.setRowHeight(30);
        t.setGridColor(AppColors.BORDE_PANEL);
        t.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        t.getTableHeader().setBackground(AppColors.AZUL_CLARO);
        t.getTableHeader().setForeground(AppColors.AZUL_OSCURO);
        t.setSelectionBackground(AppColors.AZUL_CLARO);
        t.setSelectionForeground(AppColors.AZUL_OSCURO);
        t.setShowVerticalLines(false);
    }

    private void mostrarAlerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }
}
