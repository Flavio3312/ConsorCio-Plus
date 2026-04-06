package com.consorcioplus.view.unidad;

import com.consorcioplus.controller.UnidadFuncionalController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.UnidadFuncional;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Diálogo modal para crear o editar una Unidad Funcional.
 * Incluye validación en tiempo real del porcentual disponible (RD01).
 */
public class UnidadFuncionalFormDialog extends JDialog {

    private final UnidadFuncionalController controller;
    private final UnidadFuncional           ufEditar;   // null = nueva
    private final Consorcio                 consorcio;

    private JTextField txtNumero;
    private JTextField txtPiso;
    private JTextField txtPorcentual;
    private JLabel     lblPorcentualInfo;
    private boolean    guardado = false;

    public UnidadFuncionalFormDialog(JFrame parent, UnidadFuncional uf,
                                     Consorcio consorcio,
                                     UnidadFuncionalController controller) {
        super(parent, uf == null ? "Nueva Unidad Funcional" : "Editar Unidad Funcional", true);
        this.controller = controller;
        this.ufEditar   = uf;
        this.consorcio  = consorcio;
        construirUI();
        if (uf != null) preCargar(uf);
        mostrarPorcentualDisponible();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ─── UI ─────────────────────────────────────────────────────────────────

    private void construirUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppColors.FONDO_CARD);
        panel.setBorder(new EmptyBorder(20, 28, 20, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(4, 0, 4, 0);
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        gbc.gridy     = 0;

        // Subtítulo con nombre del consorcio
        JLabel lblSub = new JLabel("Consorcio: " + consorcio.getNombre());
        lblSub.setFont(AppColors.FUENTE_NORMAL);
        lblSub.setForeground(AppColors.AZUL_MEDIO);
        lblSub.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(lblSub, gbc);
        gbc.gridy++;

        // Campos
        txtNumero     = new JTextField(18);
        txtPiso       = new JTextField(18);
        txtPorcentual = new JTextField(18);

        agregarCampo(panel, gbc, "Número de UF *",  txtNumero);
        agregarCampo(panel, gbc, "Piso (opcional)",  txtPiso);
        agregarCampo(panel, gbc, "Porcentual (%) *", txtPorcentual);

        // Indicador de porcentual disponible
        lblPorcentualInfo = new JLabel(" ");
        lblPorcentualInfo.setFont(AppColors.FUENTE_PEQUEÑA);
        lblPorcentualInfo.setForeground(AppColors.AZUL_MEDIO);
        panel.add(lblPorcentualInfo, gbc);
        gbc.gridy++;

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(AppColors.FONDO_CARD);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton("Guardar");
        estilizarBoton(btnGuardar, AppColors.AZUL_MEDIO);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e  -> guardar());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        gbc.gridy++;
        panel.add(botones, gbc);

        setContentPane(panel);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc,
                               String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppColors.FUENTE_PEQUEÑA);
        lbl.setForeground(AppColors.TEXTO_GRIS);
        panel.add(lbl, gbc);
        gbc.gridy++;
        campo.setFont(AppColors.FUENTE_NORMAL);
        panel.add(campo, gbc);
        gbc.gridy++;
    }

    // ─── LÓGICA ─────────────────────────────────────────────────────────────

    private void preCargar(UnidadFuncional uf) {
        txtNumero.setText(uf.getNumero());
        txtPiso.setText(uf.getPiso() != null ? uf.getPiso() : "");
        txtPorcentual.setText(uf.getPorcentual().toPlainString());
    }

    private void mostrarPorcentualDisponible() {
        try {
            BigDecimal suma = controller.obtenerSumaPorcentuales(consorcio.getId());
            // Si es edición, restamos el porcentual propio para mostrar cuánto sobra
            if (ufEditar != null) {
                suma = suma.subtract(ufEditar.getPorcentual());
            }
            BigDecimal disponible = new BigDecimal("100.0000").subtract(suma);
            lblPorcentualInfo.setText("Porcentual disponible: " + disponible.toPlainString() + "%");
            lblPorcentualInfo.setForeground(disponible.compareTo(BigDecimal.ZERO) > 0
                ? AppColors.AZUL_MEDIO : AppColors.TEXTO_ERROR);
        } catch (SQLException ex) {
            lblPorcentualInfo.setText("No se pudo consultar la suma actual.");
        }
    }

    private void guardar() {
        try {
            String     numero     = txtNumero.getText().trim();
            String     piso       = txtPiso.getText().trim();
            BigDecimal porcentual = new BigDecimal(
                txtPorcentual.getText().trim().replace(",", "."));

            if (ufEditar == null) {
                controller.crear(numero, piso.isEmpty() ? null : piso,
                                 porcentual, consorcio.getId());
            } else {
                controller.actualizar(ufEditar.getId(), numero,
                                      piso.isEmpty() ? null : piso,
                                      porcentual, consorcio.getId());
            }
            guardado = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Ingrese un valor numérico válido para el porcentual (ej: 5.25).",
                "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean fueGuardado() { return guardado; }

    // ─── Helpers ────────────────────────────────────────────────────────────

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
