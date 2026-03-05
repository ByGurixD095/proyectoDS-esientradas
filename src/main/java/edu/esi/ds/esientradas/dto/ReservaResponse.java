package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

public record ReservaResponse(Long entradaId, String token, LocalDateTime expiraEn) {
}