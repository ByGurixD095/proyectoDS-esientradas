package edu.esi.ds.esientradas.dto;

import java.math.BigDecimal;

public sealed interface DtoEntrada permits DtoEntradaDeZona, DtoEntradaPrecisa {
    Long id();

    Long espectaculoId();

    BigDecimal precio();

    String tipo();
}