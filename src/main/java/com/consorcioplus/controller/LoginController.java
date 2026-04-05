package com.consorcioplus.controller;

import com.consorcioplus.model.dao.IUsuarioDAO;
import com.consorcioplus.model.dao.impl.UsuarioDAOImpl;
import com.consorcioplus.model.entity.Usuario;
import com.consorcioplus.util.PasswordUtils;
import com.consorcioplus.util.SessionManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Controller de autenticación.
 * Aplica SRP: solo gestiona el inicio y cierre de sesión.
 */
public class LoginController {

    private final IUsuarioDAO usuarioDAO;

    public LoginController() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    /**
     * Intenta autenticar al usuario.
     *
     * @param username  nombre de usuario
     * @param password  contraseña en texto plano
     * @return true si la autenticación fue exitosa
     * @throws SQLException si ocurre un error de base de datos
     */
    public boolean autenticar(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.isEmpty())  return false;

        Optional<Usuario> optUsuario = usuarioDAO.findByUsername(username.trim());
        if ((!optUsuario.isPresent())) return false;

        Usuario usuario = optUsuario.get();
        if (!PasswordUtils.matches(password, usuario.getPasswordHash())) return false;

        SessionManager.getInstance().iniciarSesion(usuario);
        return true;
    }

    /** Cierra la sesión actual del usuario. */
    public void cerrarSesion() {
        SessionManager.getInstance().cerrarSesion();
    }
}

