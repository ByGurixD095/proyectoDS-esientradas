package edu.esi.ds.esientradas.service;

import com.stripe.exception.StripeException;
import edu.esi.ds.esientradas.dto.CompraResponse;

public interface ICompraService {
    String getEmailFromToken(String token);

    CompraResponse crearPaymentIntent(Long precioCentimos, String tokenPrerreserva, String tokenUsuario);

    void confirmarCompra(String paymentIntentId, String tokenPrerreserva, String email) throws StripeException;
}