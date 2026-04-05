package com.consorcioplus.model.entity;

/**
 * Persona: puede ser Propietario o Inquilino.
 * Tipo establecido por el campo {@code tipo} (PROPIETARIO | INQUILINO).
 */
public class Persona {

    private int    id;
    private String tipo;       // "PROPIETARIO" | "INQUILINO"
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private boolean activo;

    public Persona() {}

    public Persona(String tipo, String nombre, String apellido,
                   String dni, String telefono, String email) {
        this.tipo      = tipo;
        this.nombre    = nombre;
        this.apellido  = apellido;
        this.dni       = dni;
        this.telefono  = telefono;
        this.email     = email;
        this.activo    = true;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public String getTipo()             { return tipo; }
    public void   setTipo(String t)     { this.tipo = t; }

    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }

    public String getApellido()         { return apellido; }
    public void   setApellido(String a) { this.apellido = a; }

    public String getDni()              { return dni; }
    public void   setDni(String d)      { this.dni = d; }

    public String getTelefono()         { return telefono; }
    public void   setTelefono(String t) { this.telefono = t; }

    public String getEmail()            { return email; }
    public void   setEmail(String e)    { this.email = e; }

    public boolean isActivo()           { return activo; }
    public void    setActivo(boolean a) { this.activo = a; }

    /** Nombre completo para mostrar en la UI. */
    public String getNombreCompleto()   { return apellido + ", " + nombre; }

    @Override
    public String toString()            { return getNombreCompleto() + " (" + tipo + ")"; }
}
