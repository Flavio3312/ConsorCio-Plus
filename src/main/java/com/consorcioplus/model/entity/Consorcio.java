package com.consorcioplus.model.entity;

/**
 * Entidad que representa un Consorcio / Edificio.
 */
public class Consorcio {

    private int    id;
    private String nombre;
    private String direccion;
    private String cuit;
    private int    totalPisos;
    private boolean activo;

    public Consorcio() {}

    public Consorcio(String nombre, String direccion, String cuit, int totalPisos) {
        this.nombre      = nombre;
        this.direccion   = direccion;
        this.cuit        = cuit;
        this.totalPisos  = totalPisos;
        this.activo      = true;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }

    public String getDireccion()        { return direccion; }
    public void   setDireccion(String d){ this.direccion = d; }

    public String getCuit()             { return cuit; }
    public void   setCuit(String c)     { this.cuit = c; }

    public int    getTotalPisos()       { return totalPisos; }
    public void   setTotalPisos(int p)  { this.totalPisos = p; }

    public boolean isActivo()           { return activo; }
    public void    setActivo(boolean a) { this.activo = a; }

    @Override
    public String toString() { return nombre + " — " + direccion; }
}
