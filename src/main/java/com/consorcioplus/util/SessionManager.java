package com.consorcioplus.util;

import com.consorcioplus.model.entity.Usuario;

/**
 * Gestiona la sesión del usuario actualmente autenticado.
 *
 * <p>Patrón Singleton: existe un único usuario en sesión por ejecución.
 * Principio aplicado: Single Responsibility — solo gestiona el estado de sesión.
 */
public final class SessionManager {

    private static SessionManager instance;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Inicia la sesión con el usuario autenticado. */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /** Cierra la sesión actual. */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    /** @return el usuario actualmente en sesión, o null si no hay sesión activa */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /** @return true si hay una sesión activa */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * @return true si el usuario en sesión tiene perfil ADMINISTRADOR
     */
    public boolean esAdministrador() {
        return haySesionActiva() &&
               "ADMINISTRADOR".equals(usuarioActual.getPerfil());
    }
}
