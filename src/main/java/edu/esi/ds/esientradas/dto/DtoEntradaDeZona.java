package edu.esi.ds.esientradas.dto;

import java.math.BigDecimal;

public record DtoEntradaDeZona(
        Long id,
        Long espectaculoId,
        BigDecimal precio,
        String tipo,
        Integer zona) implements DtoEntrada {
}