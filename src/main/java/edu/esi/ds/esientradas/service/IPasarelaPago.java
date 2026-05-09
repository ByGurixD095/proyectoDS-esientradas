package edu.esi.ds.esientradas.service;

public interface IPasarelaPago {
    String crearIntencionPago(Long precioCentimos, String tokenPrerreserva, String emailUsuario);

    void verificarEstadoPago(String paymentIntentId, String tokenPrerreserva);
}