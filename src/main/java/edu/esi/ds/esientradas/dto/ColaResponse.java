package edu.esi.ds.esientradas.dto;

public record ColaResponse(
        Long colaId,
        Integer posicion,
        long usuariosDelante,
        String estadoCola) {
}
