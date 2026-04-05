package com.consorcioplus.view.persona;

import com.consorcioplus.controller.PersonaController;
import com.consorcioplus.model.entity.Persona;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Panel de listado y gestión de Personas (Propietarios e Inquilinos). */
public class PersonaListPanel extends JPanel {

    private final PersonaController    controller = new PersonaController();
    private       DefaultTableModel    modelo;
    private       JTable               tabla;

    public PersonaListPanel() {
        super(new BorderLayout());
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.FONDO_CARD);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Gestión de Personas");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);

        JButton btnNuevo = crearBoton("+ Nueva Persona", AppColors.AZUL_MEDIO);
        btnNuevo.addActionListener(e -> abrirFormulario(null));

        header.add(titulo,   BorderLayout.WEST);
        header.add(btnNuevo, BorderLayout.EAST);

        String[] cols = {"ID", "Tipo", "Apellido y Nombre", "DNI", "Teléfono", "Email"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setFont(AppColors.FUENTE_TABLA);
        tabla.setRowHeight(30);
        tabla.setGridColor(AppColors.BORDE_PANEL);
        tabla.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tabla.getTableHeader().setBackground(AppColors.AZUL_CLARO);
        tabla.getTableHeader().setForeground(AppColors.AZUL_OSCURO);
        tabla.setSelectionBackground(AppColors.AZUL_CLARO);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getColumnModel().getColumn(1).setMaxWidth(110);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(AppColors.FONDO_TABLA);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setBackground(AppColors.FONDO_CARD);
        JButton btnEditar = crearBoton("✏ Editar",      AppColors.AZUL_MEDIO);
        JButton btnBaja   = crearBoton("🗑 Dar de Baja", AppColors.TEXTO_ERROR);
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnBaja.addActionListener(e   -> darBajaSeleccionada());
        acciones.add(btnEditar);
        acciones.add(btnBaja);

        add(header,   BorderLayout.NORTH);
        add(scroll,   BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        try {
            List<Persona> lista = controller.listarActivos();
            for (Persona p : lista) {
                modelo.addRow(new Object[]{
                    p.getId(), p.getTipo(), p.getNombreCompleto(),
                    p.getDni(), p.getTelefono(), p.getEmail()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFormulario(Persona persona) {
        PersonaFormDialog dlg = new PersonaFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), persona, controller);
        dlg.setVisible(true);
        if (dlg.fueGuardada()) cargarDatos();
    }

    private void editarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { alerta("Seleccione una persona para editar."); return; }
        int id = (int) modelo.getValueAt(fila, 0);
        try {
            controller.buscarPorId(id).ifPresent(this::abrirFormulario);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darBajaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { alerta("Seleccione una persona."); return; }
        int id   = (int) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 2);
        if (JOptionPane.showConfirmDialog(this, "¿Dar de baja a " + nombre + "?",
            "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controller.darDeBaja(id);
                cargarDatos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton crearBoton(String txt, Color bg) {
        JButton btn = new JButton(txt);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBackground(bg);
        btn.setForeground(AppColors.TEXTO_BLANCO);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        return btn;
    }

    private void alerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }
}
