package com.bibliouni.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Material de la biblioteca (libro, revista, tesis...).
 */
@Entity
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    private String tipo; // LIBRO, REVISTA, TESIS

    @Enumerated(EnumType.STRING)
    private EstadoMaterial estado = EstadoMaterial.DISPONIBLE;

    public Material() {
    }

    public Material(String titulo, String autor, String tipo) {
        this.titulo = titulo;
        this.autor = autor;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public EstadoMaterial getEstado() { return estado; }
    public void setEstado(EstadoMaterial estado) { this.estado = estado; }
}
