package edu.esi.ds.esientradas.dto;

public record CompraRequest(
                String tokenPrerreserva,
                String tokenUsuario,
                Long precio) {
}