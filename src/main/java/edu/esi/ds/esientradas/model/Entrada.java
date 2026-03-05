package edu.esi.ds.esientradas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Entrada {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    protected Long id;
    private Long precio; // Ojo: en céntimos de euro

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    protected Espectaculo espectaculo;

    @Enumerated(EnumType.STRING)
    protected Estado estado;

    @Column(name = "token_prerreserva", length = 36)
    private String tokenPrerreserva;

    @Column(name = "fecha_prerreserva")
    private LocalDateTime fechaPrerreserva;

    @Column(name = "correo_comprador")
    private String correoComprador;

    public String getTokenPrerreserva() {
        return tokenPrerreserva;
    }

    public void setTokenPrerreserva(String tokenPrerreserva) {
        this.tokenPrerreserva = tokenPrerreserva;
    }

    public LocalDateTime getFechaPrerreserva() {
        return fechaPrerreserva;
    }

    public void setFechaPrerreserva(LocalDateTime fechaPrerreserva) {
        this.fechaPrerreserva = fechaPrerreserva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Espectaculo getEspectaculo() {
        return espectaculo;
    }

    public void setEspectaculo(Espectaculo espectaculo) {
        this.espectaculo = espectaculo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Long getPrecio() {
        return precio;
    }

    public void setPrecio(Long precio) {
        this.precio = precio;
    }

    public String getCorreoComprador() {
        return correoComprador;
    }

    public void setCorreoComprador(String correoComprador) {
        this.correoComprador = correoComprador;
    }
}
