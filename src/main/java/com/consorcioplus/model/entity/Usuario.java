package com.consorcioplus.model.entity;

/**
 * Entidad que representa un usuario del sistema ConsorCio+.
 * Principio Clean Code: POJO simple, nombres expresivos, sin lógica de negocio.
 */
public class Usuario {

    private int    id;
    private String username;
    private String passwordHash;
    private String perfil;   // "ADMINISTRADOR" | "OPERADOR"
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String username, String passwordHash,
                   String perfil, boolean activo) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.perfil       = perfil;
        this.activo       = activo;
    }

    // ── Getters & Setters ─────────────────────────────────
    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPasswordHash()          { return passwordHash; }
    public void   setPasswordHash(String h)  { this.passwordHash = h; }

    public String getPerfil()                { return perfil; }
    public void   setPerfil(String p)        { this.perfil = p; }

    public boolean isActivo()                { return activo; }
    public void    setActivo(boolean a)      { this.activo = a; }

    @Override
    public String toString()  { return username + " (" + perfil + ")"; }
}
