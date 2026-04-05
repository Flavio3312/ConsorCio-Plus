package com.consorcioplus;

import com.consorcioplus.util.DatabaseConnection;
import com.consorcioplus.view.LoginFrame;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación ConsorCio+.
 *
 * Principios aplicados:
 * - Look and Feel del sistema operativo para la mejor experiencia nativa.
 * - Invocación en el Event Dispatch Thread (EDT) para thread-safety en Swing.
 * - Shutdown hook para cerrar la conexión JDBC correctamente.
 */
public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();
        registrarShutdownHook();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /**
     * Configura el Look & Feel Nimbus —un L&F multiplataforma de Java
     * que respeta correctamente setBackground/setForeground en botones,
     * a diferencia del L&F nativo de Windows que ignora colores personalizados.
     */
    private static void configurarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Personalizar defaults de Nimbus para coincidir con nuestra paleta
                    UIManager.put("control", new java.awt.Color(0xf5, 0xf7, 0xfa));
                    UIManager.put("nimbusBase", new java.awt.Color(0x1a, 0x3a, 0x5c));
                    UIManager.put("nimbusBlueGrey", new java.awt.Color(0xb0, 0xc4, 0xde));
                    UIManager.put("nimbusFocus", new java.awt.Color(0x25, 0x63, 0xa8));
                    return;
                }
            }
            // Fallback: si Nimbus no está disponible, usar Metal (cross-platform)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look & Feel: " + e.getMessage());
        }
    }

    /**
     * Registra un hook para cerrar la conexión JDBC al terminar la JVM,
     * evitando connection leaks.
     */
    private static void registrarShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Conexión JDBC cerrada correctamente.");
        }, "ShutdownHook-JDBC"));
    }
}
