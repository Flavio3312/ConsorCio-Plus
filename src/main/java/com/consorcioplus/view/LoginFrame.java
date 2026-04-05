package com.consorcioplus.view;

import com.consorcioplus.controller.LoginController;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * Pantalla de inicio de sesión de ConsorCio+.
 * Aplica principio KISS: formulario simple, validación inmediata.
 */
public class LoginFrame extends JFrame {

    private final LoginController loginController = new LoginController();

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblError;

    public LoginFrame() {
        super("ConsorCio+ — Iniciar Sesión");
        configurarVentana();
        construirUI();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(AppColors.FONDO_PANEL);

        // ── HEADER AZUL ──────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 4));
        header.setBackground(AppColors.AZUL_OSCURO);
        header.setBorder(new EmptyBorder(28, 20, 20, 20));

        JLabel lblApp = new JLabel("ConsorCio+", SwingConstants.CENTER);
        lblApp.setFont(AppColors.FUENTE_TITULO);
        lblApp.setForeground(AppColors.TEXTO_BLANCO);

        JLabel lblSub = new JLabel("Sistema de Administración de Consorcios", SwingConstants.CENTER);
        lblSub.setFont(AppColors.FUENTE_PEQUEÑA);
        lblSub.setForeground(new Color(0xcc, 0xdd, 0xee));

        JLabel lblVer = new JLabel("Iteración 1 — v0.1.0", SwingConstants.CENTER);
        lblVer.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblVer.setForeground(new Color(0x88, 0xaa, 0xcc));

        header.add(lblApp);
        header.add(lblSub);
        header.add(lblVer);

        // ── FORMULARIO ───────────────────────────────────────
        JPanel form = new JPanel();
        form.setBackground(AppColors.FONDO_CARD);
        form.setBorder(new EmptyBorder(30, 40, 30, 40));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtUsername = crearCampoTexto("Usuario");
        txtPassword = new JPasswordField();
        estilizarInput(txtPassword, "Contraseña");

        btnLogin = new JButton("Iniciar Sesión");
        estilizarBoton(btnLogin);

        lblError = new JLabel(" ");
        lblError.setFont(AppColors.FUENTE_PEQUEÑA);
        lblError.setForeground(AppColors.TEXTO_ERROR);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        form.add(new JLabel("Usuario"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(14));
        form.add(new JLabel("Contraseña"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(6));
        form.add(lblError);
        form.add(Box.createVerticalStrut(16));
        form.add(btnLogin);

        estilizarLabels(form);

        // ── FOOTER ───────────────────────────────────────────
        JLabel footer = new JLabel("Buenos Aires · Marzo 2026", SwingConstants.CENTER);
        footer.setFont(AppColors.FUENTE_PEQUEÑA);
        footer.setForeground(AppColors.TEXTO_GRIS);
        footer.setBorder(new EmptyBorder(8, 0, 10, 0));

        raiz.add(header, BorderLayout.NORTH);
        raiz.add(form,   BorderLayout.CENTER);
        raiz.add(footer, BorderLayout.SOUTH);

        setContentPane(raiz);

        // ── EVENTOS ──────────────────────────────────────────
        btnLogin.addActionListener(e -> intentarLogin());
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarLogin();
            }
        });
    }

    private void intentarLogin() {
        String usuario = txtUsername.getText().trim();
        String pass    = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || pass.isEmpty()) {
            lblError.setText("Ingrese usuario y contraseña.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return loginController.autenticar(usuario, pass);
            }

            @Override protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        abrirMainFrame();
                    } else {
                        lblError.setText("Usuario o contraseña incorrectos.");
                        txtPassword.setText("");
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar Sesión");
                    }
                } catch (Exception ex) {
                    lblError.setText("Error de conexión con la base de datos.");
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Iniciar Sesión");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void abrirMainFrame() {
        MainFrame main = new MainFrame();
        main.setVisible(true);
        dispose();
    }

    // ── Helpers de estilo ──────────────────────────────────
    private JTextField crearCampoTexto(String placeholder) {
        JTextField field = new JTextField();
        estilizarInput(field, placeholder);
        return field;
    }

    private void estilizarInput(JComponent campo, String placeholder) {
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campo.setFont(AppColors.FUENTE_NORMAL);
        campo.setBackground(AppColors.FONDO_PANEL);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDE_INPUT),
            new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void estilizarBoton(JButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBackground(AppColors.AZUL_MEDIO);
        btn.setForeground(AppColors.TEXTO_BLANCO);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppColors.AZUL_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppColors.AZUL_MEDIO); }
        });
    }

    private void estilizarLabels(JPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (!lbl.equals(lblError)) {
                    lbl.setFont(AppColors.FUENTE_PEQUEÑA);
                    lbl.setForeground(AppColors.TEXTO_GRIS);
                    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                }
            }
        }
    }
}

