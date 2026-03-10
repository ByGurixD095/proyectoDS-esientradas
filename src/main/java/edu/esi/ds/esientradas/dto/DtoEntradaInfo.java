package edu.esi.ds.esientradas.dto;

import java.math.BigDecimal;

public record DtoEntradaInfo(BigDecimal total, BigDecimal libres, BigDecimal reservadas, BigDecimal vendidas) {
}