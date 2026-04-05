package com.consorcioplus.view.consorcio;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel que lista los Consorcios y permite su gestión (Alta/Modificación/Baja lógica).
 */
public class ConsorcioListPanel extends JPanel {

    private final ConsorcioController controller = new ConsorcioController();
    private DefaultTableModel          modelo;
    private JTable                     tabla;

    public ConsorcioListPanel() {
        super(new BorderLayout(0, 0));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        // ── HEADER ────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.FONDO_CARD);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Gestión de Consorcios");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);

        JButton btnNuevo = crearBoton("+ Nuevo Consorcio", AppColors.AZUL_MEDIO);
        btnNuevo.addActionListener(e -> abrirFormulario(null));

        header.add(titulo,   BorderLayout.WEST);
        header.add(btnNuevo, BorderLayout.EAST);

        // ── TABLA ─────────────────────────────────────────
        String[] columnas = {"ID", "Nombre", "Dirección", "CUIT", "Pisos"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 || c == 4 ? Integer.class : String.class;
            }
        };

        tabla = new JTable(modelo);
        estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDE_PANEL));
        scroll.getViewport().setBackground(AppColors.FONDO_TABLA);

        // ── BOTONES DE ACCIÓN ──────────────────────────────
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setBackground(AppColors.FONDO_CARD);

        JButton btnEditar = crearBoton("✏ Editar", AppColors.AZUL_MEDIO);
        JButton btnBaja   = crearBoton("🗑 Dar de Baja", AppColors.TEXTO_ERROR);

        btnEditar.addActionListener(e -> editarSeleccionado());
        btnBaja.addActionListener(e   -> darBajaSeleccionado());

        acciones.add(btnEditar);
        acciones.add(btnBaja);

        add(header,  BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        try {
            List<Consorcio> lista = controller.listarActivos();
            for (Consorcio c : lista) {
                modelo.addRow(new Object[]{
                    c.getId(), c.getNombre(), c.getDireccion(),
                    c.getCuit() != null ? c.getCuit() : "—",
                    c.getTotalPisos()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar consorcios: " + ex.getMessage(),
                "Error de BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFormulario(Consorcio consorcio) {
        ConsorcioFormDialog dialog = new ConsorcioFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), consorcio, controller);
        dialog.setVisible(true);
        if (dialog.fuarGuardado()) cargarDatos();
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarAlerta("Seleccione un consorcio para editar."); return; }
        int id = (int) modelo.getValueAt(fila, 0);
        try {
            controller.buscarPorId(id).ifPresent(this::abrirFormulario);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darBajaSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarAlerta("Seleccione un consorcio para dar de baja."); return; }
        int id   = (int) modelo.getValueAt(fila, 0);
        String nombres = (String) modelo.getValueAt(fila, 1);
        int ok = JOptionPane.showConfirmDialog(this,
            "¿Dar de baja el consorcio \"" + nombres + "\"?\nEsta acción es reversible desde la base de datos.",
            "Confirmar Baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                controller.darDeBaja(id);
                cargarDatos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Helpers de estilo ──────────────────────────────────
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
