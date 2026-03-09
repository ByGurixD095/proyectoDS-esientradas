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

@Service
public class CompraService {

    @Value("${api_key_Stripe}")
    private String _key;

    @Autowired
    EntradaService entradaService;

    public String crearPaymentIntent(Long precioCentimos, String tokenPrerreserva, String tokenUsuario) {
        boolean canBuy = entradaService.canBuy(tokenPrerreserva, tokenUsuario);
        if (!canBuy) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede comprar las entradas.");
        }

        Stripe.apiKey = _key;

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(precioCentimos)
                    .setCurrency("eur")
                    .putMetadata("tokenPrerreserva", tokenPrerreserva)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();

        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al conectar con el servicio de pagos");
        }
    }

    public void confirmarCompra(String tokenPrerreserva, String email) {
        this.entradaService.confirmarCompra(tokenPrerreserva, email);
    }

}
