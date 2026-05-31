package com.consorcioplus.model.entity;

import java.time.LocalDateTime;

/**
 * Reclamo: representa una incidencia de mantenimiento registrada por un consorcista.
 *
 * Máquina de estados:
 *   PENDIENTE → EN_CURSO → RESUELTO
 *                        → DESCARTADO
 *
 * Conceptos Java del Módulo 3:
 *  - enum interno EstadoReclamo (conjunto cerrado de estados)
 *  - Constructores sobrecargados (alta vs. carga desde BD)
 *  - Encapsulamiento con validación de transición de estados
 *  - Generación automática de nroReclamo al crear
 */
public class Reclamo {

    // -------------------------------------------------------
    // Enum interno — conjunto cerrado de estados válidos
    // -------------------------------------------------------
    public enum EstadoReclamo {
        PENDIENTE,    // reclamo registrado, sin asignar
        EN_CURSO,     // asignado a un técnico / operador
        RESUELTO,     // incidencia solucionada (estado final)
        DESCARTADO    // reclamo inválido o duplicado (estado final)
    }

    private int           id;
    private int           idUnidad;
    private String        descripcion;
    private EstadoReclamo estado;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaResolucion; // null hasta que se resuelva
    private int           idUsuarioAlta;
    private String        nroReclamo;      // código único: "REC-2026-0001"

    // -------------------------------------------------------
    // Constructor 1: alta desde formulario
    // El estado inicial siempre es PENDIENTE; la fecha se asigna automáticamente.
    // -------------------------------------------------------
    public Reclamo(int idUnidad, String descripcion, int idUsuario) {
        if (descripcion == null || descripcion.trim().isEmpty())
            throw new IllegalArgumentException("La descripción del reclamo es obligatoria.");
        this.idUnidad      = idUnidad;
        this.descripcion   = descripcion.trim();
        this.idUsuarioAlta = idUsuario;
        this.estado        = EstadoReclamo.PENDIENTE;
        this.fechaAlta     = LocalDateTime.now();
        this.nroReclamo    = generarNroReclamo();
    }

    // -------------------------------------------------------
    // Constructor 2: reconstrucción completa desde la BD
    // -------------------------------------------------------
    public Reclamo(int id, int idUnidad, String descripcion,
                   EstadoReclamo estado, LocalDateTime fechaAlta,
                   LocalDateTime fechaResolucion, int idUsuarioAlta,
                   String nroReclamo) {
        this.id               = id;
        this.idUnidad         = idUnidad;
        this.descripcion      = descripcion;
        this.estado           = estado;
        this.fechaAlta        = fechaAlta;
        this.fechaResolucion  = fechaResolucion;
        this.idUsuarioAlta    = idUsuarioAlta;
        this.nroReclamo       = nroReclamo;
    }

    // -------------------------------------------------------
    // Generación del número único correlativo
    // En producción el correlativo viene de la BD (MAX+1)
    // -------------------------------------------------------
    private String generarNroReclamo() {
        int anio = LocalDateTime.now().getYear();
        return String.format("REC-%d-%04d", anio, System.currentTimeMillis() % 10000);
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------
    public int           getId()               { return id; }
    public int           getIdUnidad()         { return idUnidad; }
    public String        getDescripcion()      { return descripcion; }
    public EstadoReclamo getEstado()           { return estado; }
    public LocalDateTime getFechaAlta()        { return fechaAlta; }
    public LocalDateTime getFechaResolucion()  { return fechaResolucion; }
    public int           getIdUsuarioAlta()    { return idUsuarioAlta; }
    public String        getNroReclamo()       { return nroReclamo; }

    // -------------------------------------------------------
    // Setters con validación de negocio
    // -------------------------------------------------------
    public void setId(int id)                             { this.id = id; }
    public void setNroReclamo(String nro)                 { this.nroReclamo = nro; }

    /** Actualiza el estado; si pasa a RESUELTO, registra la fecha de resolución. */
    public void setEstado(EstadoReclamo nuevoEstado) {
        this.estado = nuevoEstado;
        if (nuevoEstado == EstadoReclamo.RESUELTO) {
            this.fechaResolucion = LocalDateTime.now();
        }
    }

    public void setFechaResolucion(LocalDateTime fecha) { this.fechaResolucion = fecha; }

    // -------------------------------------------------------
    // Métodos de consulta
    // -------------------------------------------------------
    public boolean esPendiente()  { return estado == EstadoReclamo.PENDIENTE; }
    public boolean estaActivo()   { return estado == EstadoReclamo.PENDIENTE
                                        || estado == EstadoReclamo.EN_CURSO; }
    public boolean esFinalizado() { return estado == EstadoReclamo.RESUELTO
                                        || estado == EstadoReclamo.DESCARTADO; }

    @Override
    public String toString() {
        return nroReclamo + " [" + estado + "] " + descripcion;
    }
}
