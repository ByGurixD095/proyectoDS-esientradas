package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

public record ColaResponse(
                Long colaId,
                Integer posicion,
                long usuariosDelante,
                String estadoCola,
                boolean esTuTurno,
                LocalDateTime expiraTurnoEn) {
}