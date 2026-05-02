package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cola_virtual", uniqueConstraints = @UniqueConstraint(columnNames = { "espectaculo_id",
        "correo_usuario" }))
public class ColaVirtual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    private Espectaculo espectaculo;

    @Column(name = "correo_usuario", nullable = false, length = 255)
    private String correoUsuario;

    // Posicion en la cola (1 = primero)
    @Column(nullable = false)
    private Integer posicion;

    // Estados: ESPERANDO, ACTIVO, EXPIRADO, COMPLETADO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCola estado = EstadoCola.ESPERANDO;

    // Momento en que se activo el turno — para calcular expiracion
    @Column(name = "turno_activado_en")
    private LocalDateTime turnoActivadoEn;

    // Momento en que se unio a la cola
    @Column(name = "unido_en", nullable = false)
    private LocalDateTime unidoEn = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Espectaculo getEspectaculo() {
        return espectaculo;
    }

    public void setEspectaculo(Espectaculo e) {
        this.espectaculo = e;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String c) {
        this.correoUsuario = c;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer p) {
        this.posicion = p;
    }

    public EstadoCola getEstado() {
        return estado;
    }

    public void setEstado(EstadoCola e) {
        this.estado = e;
    }

    public LocalDateTime getTurnoActivadoEn() {
        return turnoActivadoEn;
    }

    public void setTurnoActivadoEn(LocalDateTime t) {
        this.turnoActivadoEn = t;
    }

    public LocalDateTime getUnidoEn() {
        return unidoEn;
    }

    public void setUnidoEn(LocalDateTime u) {
        this.unidoEn = u;
    }
}