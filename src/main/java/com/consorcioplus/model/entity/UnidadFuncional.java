package com.consorcioplus.model.entity;

import java.math.BigDecimal;

/**
 * Unidad Funcional (UF) dentro de un Consorcio.
 * El porcentual de participación determina la cuota de gastos.
 */
public class UnidadFuncional {

    private int        id;
    private String     numero;
    private String     piso;
    private BigDecimal porcentual;     // ej. 5.2500
    private int        idConsorcio;
    private String     nombreConsorcio; // Campo auxiliar para listados
    private boolean    activo;

    public UnidadFuncional() {}

    public UnidadFuncional(String numero, String piso,
                           BigDecimal porcentual, int idConsorcio) {
        this.numero      = numero;
        this.piso        = piso;
        this.porcentual  = porcentual;
        this.idConsorcio = idConsorcio;
        this.activo      = true;
    }

    public int        getId()                    { return id; }
    public void       setId(int id)              { this.id = id; }

    public String     getNumero()                { return numero; }
    public void       setNumero(String n)        { this.numero = n; }

    public String     getPiso()                  { return piso; }
    public void       setPiso(String p)          { this.piso = p; }

    public BigDecimal getPorcentual()            { return porcentual; }
    public void       setPorcentual(BigDecimal p){ this.porcentual = p; }

    public int        getIdConsorcio()           { return idConsorcio; }
    public void       setIdConsorcio(int ic)     { this.idConsorcio = ic; }

    public String     getNombreConsorcio()       { return nombreConsorcio; }
    public void       setNombreConsorcio(String n){ this.nombreConsorcio = n; }

    public boolean    isActivo()                 { return activo; }
    public void       setActivo(boolean a)       { this.activo = a; }

    @Override
    public String toString() {
        return "UF " + numero + " (Piso " + piso + ") — " + porcentual + "%";
    }
}
