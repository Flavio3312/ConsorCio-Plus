package com.consorcioplus.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago: registra cada cobro recibido contra un detalle de liquidación.
 * Soporta pagos parciales (un detalle puede tener múltiples pagos).
 * Genera un nro_recibo único e irrepetible.
 *
 * Conceptos Java del Módulo 3:
 *  - POJO con encapsulamiento estricto (todos los atributos privados)
 *  - Constructores sobrecargados: uno para alta desde formulario, otro para carga desde BD
 *  - BigDecimal para precisión exacta en montos financieros
 */
public class Pago {

    private int           id;
    private int           idLiqDetalle;    // FK → liquidacion_detalle
    private LocalDateTime fechaPago;
    private BigDecimal    montoPagado;     // permite pagos parciales
    private String        nroRecibo;       // único irrepetible (ej: "REC-2026-0042")
    private int           idUsuario;       // auditoría: quién registró el cobro

    // -------------------------------------------------------
    // Constructor 1: alta desde formulario (sin id ni fecha —
    // se asignan en el DAO al persistir)
    // -------------------------------------------------------
    public Pago(int idLiqDetalle, BigDecimal montoPagado, String nroRecibo, int idUsuario) {
        if (montoPagado == null || montoPagado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto pagado debe ser mayor a 0.");
        }
        if (nroRecibo == null || nroRecibo.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de recibo es obligatorio.");
        }
        this.idLiqDetalle = idLiqDetalle;
        this.montoPagado  = montoPagado;
        this.nroRecibo    = nroRecibo.trim().toUpperCase();
        this.idUsuario    = idUsuario;
        this.fechaPago    = LocalDateTime.now(); // timestamp automático al crear
    }

    // -------------------------------------------------------
    // Constructor 2: reconstrucción desde la base de datos
    // (todos los campos incluido id y fecha exacta del registro)
    // -------------------------------------------------------
    public Pago(int id, int idLiqDetalle, LocalDateTime fechaPago,
                BigDecimal montoPagado, String nroRecibo, int idUsuario) {
        this.id           = id;
        this.idLiqDetalle = idLiqDetalle;
        this.fechaPago    = fechaPago;
        this.montoPagado  = montoPagado;
        this.nroRecibo    = nroRecibo;
        this.idUsuario    = idUsuario;
    }

    // --- Getters ---
    public int           getId()           { return id; }
    public int           getIdLiqDetalle() { return idLiqDetalle; }
    public LocalDateTime getFechaPago()    { return fechaPago; }
    public BigDecimal    getMontoPagado()  { return montoPagado; }
    public String        getNroRecibo()    { return nroRecibo; }
    public int           getIdUsuario()    { return idUsuario; }

    // --- Setters con validación ---
    public void setId(int id)                     { this.id = id; }
    public void setMontoPagado(BigDecimal monto)  {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Monto inválido.");
        this.montoPagado = monto;
    }

    @Override
    public String toString() {
        return "Pago{nroRecibo='" + nroRecibo + "', monto=" + montoPagado
                + ", fecha=" + fechaPago + "}";
    }
}
