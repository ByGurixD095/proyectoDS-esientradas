package edu.esi.ds.esientradas.dto;

public record ConfirmRequest(String paymentIntentId, String tokenPrerreserva, String email) {
}
