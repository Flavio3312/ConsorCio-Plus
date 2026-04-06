package com.consorcioplus.view;

import com.consorcioplus.controller.LoginController;
import com.consorcioplus.util.AppColors;
import com.consorcioplus.util.SessionManager;
import com.consorcioplus.view.consorcio.ConsorcioListPanel;
import com.consorcioplus.view.gasto.GastoListPanel;
import com.consorcioplus.view.liquidacion.LiquidacionPanel;
import com.consorcioplus.view.persona.PersonaListPanel;
import com.consorcioplus.view.unidad.UnidadFuncionalListPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ventana principal de la aplicación ConsorCio+.
 * Implementa un layout de navegación lateral + panel de contenido central.
 */
public class MainFrame extends JFrame {

    private final LoginController loginController = new LoginController();
    private JPanel panelContenido;
    private JLabel lblUsuario;

    public MainFrame() {
        super("ConsorCio+ — Panel Principal");
        configurarVentana();
        construirUI();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout());

        // ── SIDEBAR ──────────────────────────────────────────
        JPanel sidebar = crearSidebar();

        // ── CONTENIDO ────────────────────────────────────────
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(AppColors.FONDO_PANEL);
        mostrarBienvenida();

        raiz.add(sidebar, BorderLayout.WEST);
        raiz.add(panelContenido, BorderLayout.CENTER);

        setContentPane(raiz);
    }

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(AppColors.AZUL_OSCURO);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo
        JLabel lblLogo = new JLabel("ConsorCio+", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(AppColors.TEXTO_BLANCO);
        lblLogo.setBorder(new EmptyBorder(20, 10, 8, 10));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSlogan = new JLabel("Gestión Integral", SwingConstants.CENTER);
        lblSlogan.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblSlogan.setForeground(new Color(0x90, 0xc4, 0xe8));
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2a, 0x5a, 0x8c));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Menú
        JPanel menu = new JPanel();
        menu.setBackground(AppColors.AZUL_OSCURO);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(10, 0, 0, 0));

        menu.add(crearItemMenu("🏢  Consorcios", () -> mostrarPanel(new ConsorcioListPanel())));
        menu.add(crearItemMenu("🔑  Unidades Funcionales", () -> mostrarPanel(new UnidadFuncionalListPanel())));
        menu.add(crearItemMenu("👥  Personas", () -> mostrarPanel(new PersonaListPanel())));
        menu.add(crearItemMenu("💰  Gastos", () -> mostrarPanel(new GastoListPanel())));
        menu.add(crearItemMenu("📋  Liquidaciones", () -> mostrarPanel(new LiquidacionPanel())));

        // Footer del sidebar
        JPanel footerSidebar = new JPanel(new BorderLayout());
        footerSidebar.setBackground(new Color(0x12, 0x28, 0x40));
        footerSidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        String usuario = SessionManager.getInstance().haySesionActiva()
                ? SessionManager.getInstance().getUsuarioActual().getUsername()
                : "—";
        lblUsuario = new JLabel("●  " + usuario, SwingConstants.LEFT);
        lblUsuario.setFont(AppColors.FUENTE_PEQUEÑA);
        lblUsuario.setForeground(new Color(0x90, 0xc4, 0xe8));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setFont(AppColors.FUENTE_PEQUEÑA);
        btnSalir.setBackground(new Color(0xb9, 0x1c, 0x1c));
        btnSalir.setForeground(AppColors.TEXTO_BLANCO);
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> cerrarSesion());

        footerSidebar.add(lblUsuario, BorderLayout.CENTER);
        footerSidebar.add(btnSalir, BorderLayout.SOUTH);

        sidebar.add(lblLogo);
        sidebar.add(lblSlogan);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(sep);
        sidebar.add(menu);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(footerSidebar);

        return sidebar;
    }

    private JPanel crearItemMenu(String texto, Runnable accion) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(AppColors.AZUL_OSCURO);
        item.setBorder(new EmptyBorder(0, 0, 0, 0));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(AppColors.FUENTE_NORMAL);
        lbl.setForeground(new Color(0xcc, 0xdd, 0xee));
        lbl.setBorder(new EmptyBorder(10, 16, 10, 10));

        item.add(lbl, BorderLayout.CENTER);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accion.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(AppColors.AZUL_MEDIO);

            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(AppColors.AZUL_OSCURO);
            }
        });

        return item;
    }

    private void mostrarPanel(JPanel panel) {
        panelContenido.removeAll();
        panelContenido.add(panel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarBienvenida() {
        JPanel bienvenida = new JPanel(new GridBagLayout());
        bienvenida.setBackground(AppColors.FONDO_PANEL);

        JPanel card = new JPanel();
        card.setBackground(AppColors.FONDO_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDE_PANEL),
                new EmptyBorder(40, 60, 40, 60)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lbl1 = new JLabel("Bienvenido a ConsorCio+", SwingConstants.CENTER);
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl1.setForeground(AppColors.AZUL_OSCURO);
        lbl1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl2 = new JLabel("Seleccione una opción del menú lateral para comenzar.", SwingConstants.CENTER);
        lbl2.setFont(AppColors.FUENTE_NORMAL);
        lbl2.setForeground(AppColors.TEXTO_GRIS);
        lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lbl1);
        card.add(Box.createVerticalStrut(12));
        card.add(lbl2);

        bienvenida.add(card);
        panelContenido.add(bienvenida, BorderLayout.CENTER);
    }

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión?", "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar == JOptionPane.YES_OPTION) {
            loginController.cerrarSesion();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
