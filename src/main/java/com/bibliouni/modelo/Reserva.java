package com.bibliouni.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * GANCHO INNOVADOR: Lista de espera inteligente.
 * Cuando un material está prestado, el estudiante se anota en la lista.
 * Al devolverse, el material queda APARTADO para el primero de la fila
 * durante 48 horas. Si no lo recoge, pasa al siguiente.
 */
@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Material material;

    private LocalDateTime fechaReserva;
    private LocalDateTime venceApartado; // se llena cuando le toca el turno

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.EN_ESPERA;

    public Reserva() {
    }

    public Reserva(Usuario usuario, Material material, LocalDateTime fechaReserva) {
        this.usuario = usuario;
        this.material = material;
        this.fechaReserva = fechaReserva;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
    public LocalDateTime getVenceApartado() { return venceApartado; }
    public void setVenceApartado(LocalDateTime venceApartado) { this.venceApartado = venceApartado; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
}
