package com.consorcioplus.view.persona;

import com.consorcioplus.controller.PersonaController;
import com.consorcioplus.model.entity.Persona;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Diálogo modal para crear o editar una Persona. */
public class PersonaFormDialog extends JDialog {

    private final PersonaController controller;
    private final Persona            personaEditar;

    private JComboBox<String> cbTipo;
    private JTextField        txtNombre;
    private JTextField        txtApellido;
    private JTextField        txtDni;
    private JTextField        txtTelefono;
    private JTextField        txtEmail;
    private boolean           guardada = false;

    public PersonaFormDialog(JFrame parent, Persona persona, PersonaController controller) {
        super(parent, persona == null ? "Nueva Persona" : "Editar Persona", true);
        this.controller   = controller;
        this.personaEditar = persona;
        construirUI();
        if (persona != null) preCargar(persona);
        pack();
        setLocationRelativeTo(parent);
    }

    private void construirUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.FONDO_CARD);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        cbTipo      = new JComboBox<>(new String[]{"PROPIETARIO", "INQUILINO"});
        txtNombre   = new JTextField(22);
        txtApellido = new JTextField(22);
        txtDni      = new JTextField(15);
        txtTelefono = new JTextField(15);
        txtEmail    = new JTextField(22);

        agregarCampo(panel, gbc, "Tipo *",      cbTipo);
        agregarCampo(panel, gbc, "Nombre *",    txtNombre);
        agregarCampo(panel, gbc, "Apellido *",  txtApellido);
        agregarCampo(panel, gbc, "DNI",         txtDni);
        agregarCampo(panel, gbc, "Teléfono",    txtTelefono);
        agregarCampo(panel, gbc, "Email",       txtEmail);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setBackground(AppColors.FONDO_CARD);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton("Guardar");
        estilizarBoton(btnGuardar, AppColors.AZUL_MEDIO);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        gbc.gridy++; panel.add(botones, gbc);
        setContentPane(panel);
        setResizable(false);
    }

    private void agregarCampo(JPanel p, GridBagConstraints gbc, String lbl, JComponent campo) {
        JLabel label = new JLabel(lbl);
        label.setFont(AppColors.FUENTE_PEQUEÑA);
        label.setForeground(AppColors.TEXTO_GRIS);
        p.add(label, gbc); gbc.gridy++;
        campo.setFont(AppColors.FUENTE_NORMAL);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(campo, gbc); gbc.gridy++;
    }

    private void preCargar(Persona p) {
        cbTipo.setSelectedItem(p.getTipo());
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtDni.setText(p.getDni() != null ? p.getDni() : "");
        txtTelefono.setText(p.getTelefono() != null ? p.getTelefono() : "");
        txtEmail.setText(p.getEmail() != null ? p.getEmail() : "");
    }

    private void guardar() {
        try {
            String tipo     = (String) cbTipo.getSelectedItem();
            String nombre   = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String dni      = txtDni.getText().trim();
            String tel      = txtTelefono.getText().trim();
            String email    = txtEmail.getText().trim();

            if (personaEditar == null) {
                controller.crear(tipo, nombre, apellido,
                    dni.isEmpty() ? null : dni,
                    tel.isEmpty() ? null : tel,
                    email.isEmpty() ? null : email);
            } else {
                controller.actualizar(personaEditar.getId(), tipo, nombre, apellido,
                    dni.isEmpty() ? null : dni,
                    tel.isEmpty() ? null : tel,
                    email.isEmpty() ? null : email);
            }
            guardada = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean fueGuardada() { return guardada; }

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
