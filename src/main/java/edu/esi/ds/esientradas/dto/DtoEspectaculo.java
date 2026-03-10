package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

public record DtoEspectaculo(Long id, String artista, LocalDateTime fecha, String escenario) {
}