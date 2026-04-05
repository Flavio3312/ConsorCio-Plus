package com.consorcioplus.view.consorcio;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Diálogo modal para crear o editar un Consorcio. */
public class ConsorcioFormDialog extends JDialog {

    private final ConsorcioController controller;
    private final Consorcio            consorcioEditar;   // null = nuevo

    private JTextField txtNombre;
    private JTextField txtDireccion;
    private JTextField txtCuit;
    private JSpinner   spnPisos;
    private boolean    guardado = false;

    public ConsorcioFormDialog(JFrame parent, Consorcio consorcio,
                                ConsorcioController controller) {
        super(parent, consorcio == null ? "Nuevo Consorcio" : "Editar Consorcio", true);
        this.controller       = controller;
        this.consorcioEditar  = consorcio;
        construirUI();
        if (consorcio != null) preCargar(consorcio);
        pack();
        setLocationRelativeTo(parent);
    }

    private void construirUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.FONDO_CARD);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0; gbc.gridy = 0;

        txtNombre   = new JTextField(28);
        txtDireccion = new JTextField(28);
        txtCuit      = new JTextField(14);
        spnPisos     = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

        agregarCampo(panel, gbc, "Nombre *",    txtNombre);
        agregarCampo(panel, gbc, "Dirección *", txtDireccion);
        agregarCampo(panel, gbc, "CUIT",         txtCuit);
        agregarCampo(panel, gbc, "Total Pisos *", spnPisos);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(AppColors.FONDO_CARD);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton("Guardar");
        estilizarBoton(btnGuardar, AppColors.AZUL_MEDIO);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(botones, gbc);

        setContentPane(panel);
        setResizable(false);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, String label, JComponent campo) {
        gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppColors.FUENTE_PEQUEÑA);
        lbl.setForeground(AppColors.TEXTO_GRIS);
        panel.add(lbl, gbc);
        gbc.gridy++;
        campo.setFont(AppColors.FUENTE_NORMAL);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.add(campo, gbc);
        gbc.gridy++;
    }

    private void preCargar(Consorcio c) {
        txtNombre.setText(c.getNombre());
        txtDireccion.setText(c.getDireccion());
        txtCuit.setText(c.getCuit() != null ? c.getCuit() : "");
        spnPisos.setValue(c.getTotalPisos());
    }

    private void guardar() {
        try {
            String nombre   = txtNombre.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String cuit      = txtCuit.getText().trim();
            int    pisos     = (int) spnPisos.getValue();

            if (consorcioEditar == null) {
                controller.crear(nombre, direccion, cuit.isEmpty() ? null : cuit, pisos);
            } else {
                controller.actualizar(consorcioEditar.getId(), nombre, direccion,
                                      cuit.isEmpty() ? null : cuit, pisos);
            }
            guardado = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean fuarGuardado() { return guardado; }

    private void estilizarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(AppColors.TEXTO_BLANCO);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
    }
}
