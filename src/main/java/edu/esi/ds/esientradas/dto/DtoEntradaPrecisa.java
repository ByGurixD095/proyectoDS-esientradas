package edu.esi.ds.esientradas.dto;

import java.math.BigDecimal;

public record DtoEntradaPrecisa(
        Long id,
        Long espectaculoId,
        BigDecimal precio,
        String tipo,
        int fila,
        int columna,
        int planta) implements DtoEntrada {
}