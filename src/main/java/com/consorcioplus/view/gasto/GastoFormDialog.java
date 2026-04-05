package com.consorcioplus.view.gasto;

import com.consorcioplus.controller.GastoController;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

/** Diálogo modal para registrar un nuevo Gasto. */
public class GastoFormDialog extends JDialog {

    private final GastoController controller;
    private final int              idConsorcio;
    private final LocalDate        periodo;

    private JComboBox<String> cbCategoria;
    private JTextField        txtDescripcion;
    private JTextField        txtMonto;
    private JTextField        txtNroFactura;
    private boolean           guardado = false;

    public GastoFormDialog(JFrame parent, int idConsorcio, LocalDate periodo,
                            GastoController controller) {
        super(parent, "Registrar Gasto — " + periodo.getMonthValue() + "/" + periodo.getYear(), true);
        this.controller  = controller;
        this.idConsorcio = idConsorcio;
        this.periodo     = periodo;
        construirUI();
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

        cbCategoria   = new JComboBox<>(new String[]{"ORDINARIO", "EXTRAORDINARIO"});
        txtDescripcion = new JTextField(26);
        txtMonto      = new JTextField(14);
        txtNroFactura = new JTextField(14);

        agregarCampo(panel, gbc, "Categoría *",    cbCategoria);
        agregarCampo(panel, gbc, "Descripción *",  txtDescripcion);
        agregarCampo(panel, gbc, "Monto ($) *",    txtMonto);
        agregarCampo(panel, gbc, "Nro. Factura",   txtNroFactura);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setBackground(AppColors.FONDO_CARD);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton("Registrar");
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
        p.add(campo, gbc); gbc.gridy++;
    }

    private void guardar() {
        try {
            String categoria  = (String) cbCategoria.getSelectedItem();
            String descripcion = txtDescripcion.getText().trim();
            String montoStr   = txtMonto.getText().trim();
            String nroFact    = txtNroFactura.getText().trim();

            if (montoStr.isEmpty()) throw new IllegalArgumentException("El monto es obligatorio.");
            BigDecimal monto;
            try {
                monto = new BigDecimal(montoStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("El monto ingresado no es válido.");
            }

            controller.registrar(idConsorcio, periodo, categoria, descripcion, monto,
                nroFact.isEmpty() ? null : nroFact, null);
            guardado = true;
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean fueGuardado() { return guardado; }

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
