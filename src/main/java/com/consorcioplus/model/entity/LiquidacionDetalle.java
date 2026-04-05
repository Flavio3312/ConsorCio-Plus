package com.consorcioplus.model.entity;

import java.math.BigDecimal;

/** Detalle de liquidación por Unidad Funcional. */
public class LiquidacionDetalle {

    private int        id;
    private int        idLiquidacion;
    private int        idUnidad;
    private String     numeroUnidad;       // Auxiliar UI
    private String     pisoUnidad;         // Auxiliar UI
    private BigDecimal porcentual;         // Auxiliar UI
    private BigDecimal expensaOrdinaria;
    private BigDecimal expensaExtraordinaria;
    private BigDecimal moraAplicada;
    private BigDecimal totalAPagar;
    private BigDecimal saldoDeudor;

    public LiquidacionDetalle() {
        this.expensaOrdinaria      = BigDecimal.ZERO;
        this.expensaExtraordinaria = BigDecimal.ZERO;
        this.moraAplicada          = BigDecimal.ZERO;
        this.totalAPagar           = BigDecimal.ZERO;
        this.saldoDeudor           = BigDecimal.ZERO;
    }

    public int        getId()                           { return id; }
    public void       setId(int id)                     { this.id = id; }

    public int        getIdLiquidacion()                { return idLiquidacion; }
    public void       setIdLiquidacion(int il)          { this.idLiquidacion = il; }

    public int        getIdUnidad()                     { return idUnidad; }
    public void       setIdUnidad(int iu)               { this.idUnidad = iu; }

    public String     getNumeroUnidad()                 { return numeroUnidad; }
    public void       setNumeroUnidad(String n)         { this.numeroUnidad = n; }

    public String     getPisoUnidad()                   { return pisoUnidad; }
    public void       setPisoUnidad(String p)           { this.pisoUnidad = p; }

    public BigDecimal getPorcentual()                   { return porcentual; }
    public void       setPorcentual(BigDecimal p)       { this.porcentual = p; }

    public BigDecimal getExpensaOrdinaria()              { return expensaOrdinaria; }
    public void       setExpensaOrdinaria(BigDecimal e)  { this.expensaOrdinaria = e; }

    public BigDecimal getExpensaExtraordinaria()         { return expensaExtraordinaria; }
    public void       setExpensaExtraordinaria(BigDecimal e){ this.expensaExtraordinaria = e; }

    public BigDecimal getMoraAplicada()                  { return moraAplicada; }
    public void       setMoraAplicada(BigDecimal m)      { this.moraAplicada = m; }

    public BigDecimal getTotalAPagar()                   { return totalAPagar; }
    public void       setTotalAPagar(BigDecimal t)       { this.totalAPagar = t; }

    public BigDecimal getSaldoDeudor()                   { return saldoDeudor; }
    public void       setSaldoDeudor(BigDecimal s)       { this.saldoDeudor = s; }
}
