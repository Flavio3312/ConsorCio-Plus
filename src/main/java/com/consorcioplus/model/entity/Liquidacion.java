package com.consorcioplus.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Liquidación mensual de un Consorcio. */
public class Liquidacion {

    private int           id;
    private int           idConsorcio;
    private String        nombreConsorcio;
    private LocalDate     periodo;
    private BigDecimal    totalOrdinario;
    private BigDecimal    totalExtraordinario;
    private LocalDateTime fechaCierre;
    private boolean       cerrada;

    public Liquidacion() {
        this.totalOrdinario      = BigDecimal.ZERO;
        this.totalExtraordinario = BigDecimal.ZERO;
    }

    public int           getId()                          { return id; }
    public void          setId(int id)                    { this.id = id; }

    public int           getIdConsorcio()                 { return idConsorcio; }
    public void          setIdConsorcio(int ic)           { this.idConsorcio = ic; }

    public String        getNombreConsorcio()              { return nombreConsorcio; }
    public void          setNombreConsorcio(String n)      { this.nombreConsorcio = n; }

    public LocalDate     getPeriodo()                     { return periodo; }
    public void          setPeriodo(LocalDate p)          { this.periodo = p; }

    public BigDecimal    getTotalOrdinario()               { return totalOrdinario; }
    public void          setTotalOrdinario(BigDecimal t)   { this.totalOrdinario = t; }

    public BigDecimal    getTotalExtraordinario()          { return totalExtraordinario; }
    public void          setTotalExtraordinario(BigDecimal t){ this.totalExtraordinario = t; }

    public LocalDateTime getFechaCierre()                 { return fechaCierre; }
    public void          setFechaCierre(LocalDateTime f)  { this.fechaCierre = f; }

    public boolean       isCerrada()                      { return cerrada; }
    public void          setCerrada(boolean c)            { this.cerrada = c; }

    /** Total general de la liquidacion. */
    public BigDecimal getTotalGeneral() {
        return totalOrdinario.add(totalExtraordinario);
    }
}
