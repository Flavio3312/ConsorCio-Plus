package com.consorcioplus.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de colores y fuentes corporativas de ConsorCio+.
 *
 * <p>Centralizar los colores evita "magic colors" dispersos en las vistas.
 * Principio: Don't Repeat Yourself (DRY).
 */
public final class AppColors {

    // ── Colores primarios
    public static final Color AZUL_OSCURO  = new Color(0x1a, 0x3a, 0x5c);
    public static final Color AZUL_MEDIO   = new Color(0x25, 0x63, 0xa8);
    public static final Color AZUL_CLARO   = new Color(0xd6, 0xe4, 0xf0);
    public static final Color AZUL_HOVER   = new Color(0x1e, 0x4f, 0x8c);

    // ── Fondos
    public static final Color FONDO_PANEL  = new Color(0xf5, 0xf7, 0xfa);
    public static final Color FONDO_CARD   = Color.WHITE;
    public static final Color FONDO_TABLA  = new Color(0xf9, 0xfb, 0xff);
    public static final Color FILA_ALTERNA = new Color(0xec, 0xf3, 0xfb);

    // ── Textos
    public static final Color TEXTO_OSCURO  = new Color(0x1a, 0x1a, 0x2e);
    public static final Color TEXTO_GRIS    = new Color(0x4a, 0x4a, 0x4a);
    public static final Color TEXTO_BLANCO  = Color.WHITE;
    public static final Color TEXTO_EXITO   = new Color(0x15, 0x80, 0x3d);
    public static final Color TEXTO_ERROR   = new Color(0xb9, 0x1c, 0x1c);

    // ── Bordes
    public static final Color BORDE_PANEL  = new Color(0xb0, 0xc4, 0xde);
    public static final Color BORDE_INPUT  = new Color(0x94, 0xa3, 0xb8);

    // ── Estado de reclamos
    public static final Color COLOR_PENDIENTE   = new Color(0xef, 0x88, 0x0a);
    public static final Color COLOR_EN_CURSO    = new Color(0x25, 0x63, 0xa8);
    public static final Color COLOR_RESUELTO    = new Color(0x15, 0x80, 0x3d);
    public static final Color COLOR_DESCARTADO  = new Color(0x6b, 0x72, 0x80);

    // ── Fuentes
    public static final Font FUENTE_TITULO   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO= new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_TABLA    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_BOTON    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FUENTE_PEQUEÑA  = new Font("Segoe UI", Font.PLAIN, 11);

    private AppColors() {}
}
