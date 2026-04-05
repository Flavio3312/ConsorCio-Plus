package com.consorcioplus.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gasto mensual de un Consorcio.
 * Categoría: ORDINARIO (a cargo del habitante) o EXTRAORDINARIO (a cargo del propietario).
 */
public class Gasto {

    private int        id;
    private int        idConsorcio;
    private String     nombreConsorcio;  // Auxiliar para UI
    private LocalDate  periodo;          // Primer día del mes: YYYY-MM-01
    private String     categoria;        // "ORDINARIO" | "EXTRAORDINARIO"
    private String     descripcion;
    private BigDecimal monto;
    private String     nroFactura;
    private Integer    idProveedor;
    private String     nombreProveedor;  // Auxiliar para UI

    public Gasto() {}

    public int        getId()                     { return id; }
    public void       setId(int id)               { this.id = id; }

    public int        getIdConsorcio()             { return idConsorcio; }
    public void       setIdConsorcio(int ic)       { this.idConsorcio = ic; }

    public String     getNombreConsorcio()         { return nombreConsorcio; }
    public void       setNombreConsorcio(String n) { this.nombreConsorcio = n; }

    public LocalDate  getPeriodo()                 { return periodo; }
    public void       setPeriodo(LocalDate p)      { this.periodo = p; }

    public String     getCategoria()               { return categoria; }
    public void       setCategoria(String c)       { this.categoria = c; }

    public String     getDescripcion()             { return descripcion; }
    public void       setDescripcion(String d)     { this.descripcion = d; }

    public BigDecimal getMonto()                   { return monto; }
    public void       setMonto(BigDecimal m)       { this.monto = m; }

    public String     getNroFactura()              { return nroFactura; }
    public void       setNroFactura(String n)      { this.nroFactura = n; }

    public Integer    getIdProveedor()             { return idProveedor; }
    public void       setIdProveedor(Integer ip)   { this.idProveedor = ip; }

    public String     getNombreProveedor()         { return nombreProveedor; }
    public void       setNombreProveedor(String n) { this.nombreProveedor = n; }
}
