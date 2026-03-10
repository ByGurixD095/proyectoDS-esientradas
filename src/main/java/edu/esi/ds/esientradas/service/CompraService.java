package edu.esi.ds.esientradas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dto.CompraResponse;

@Service
public class CompraService {

    @Value("${api_key_Stripe}")
    private String _key;

    @Autowired
    EntradaService entradaService;

    public CompraResponse crearPaymentIntent(Long precioCentimos, String tokenPrerreserva, String tokenUsuario) {
        Stripe.apiKey = _key;

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(precioCentimos)
                    .setCurrency("eur")
                    .putMetadata("tokenPrerreserva", tokenPrerreserva)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new CompraResponse(intent.getClientSecret(), "ejemplo");

        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al conectar con el servicio de pagos");
        }
    }

    public void confirmarCompra(String tokenPrerreserva, String email) {
        this.entradaService.confirmarCompra(tokenPrerreserva, email);
    }

}
