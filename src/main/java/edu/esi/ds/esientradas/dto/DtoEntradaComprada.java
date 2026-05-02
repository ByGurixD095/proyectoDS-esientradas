package edu.esi.ds.esientradas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DtoEntradaComprada(
        Long id,
        String tipo,
        BigDecimal precio,
        Long espectaculoId,
        String artista,
        LocalDateTime fechaEspectaculo,
        String escenario,

        Integer planta,
        Integer fila,
        Integer columna,

        Integer zona) {
}