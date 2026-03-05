package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

import edu.esi.ds.esientradas.model.Escenario;

public class DtoEspectaculo {

    private Long id;
    private String artista;
    private LocalDateTime fecha;
    private String escenario;

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setEscenario(Escenario escenario) {
        this.escenario = escenario.getNombre();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtista() {
        return artista;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEscenario() {
        return escenario;
    }

    public Long getId() {
        return this.id;
    }
}
