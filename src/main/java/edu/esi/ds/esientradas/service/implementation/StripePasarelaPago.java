package edu.esi.ds.esientradas.service.implementation;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.service.IPasarelaPago;

@Service
public class StripePasarelaPago implements IPasarelaPago {

    public StripePasarelaPago(@Value("${stripe.api-key}") String key) {
        Stripe.apiKey = key;
    }

    @Override
    public String crearIntencionPago(Long precioCentimos, String tokenPrerreserva, String emailUsuario) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(precioCentimos)
                    .setCurrency("eur")
                    .putMetadata("tokenPrerreserva", tokenPrerreserva)
                    .setReceiptEmail(emailUsuario)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();

        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al conectar con el servicio de pagos");
        }
    }

    @Override
    public void verificarEstadoPago(String paymentIntentId, String tokenPrerreserva) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(intent.getStatus())) {
                throw new IllegalStateException(
                        "El pago no tiene estado 'succeeded'. Estado actual: " + intent.getStatus());
            }

            String tokenEnMetadata = intent.getMetadata().get("tokenPrerreserva");
            if (!tokenPrerreserva.equals(tokenEnMetadata)) {
                throw new IllegalStateException("El token de prerreserva no coincide con el pago.");
            }
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error al validar el pago en la pasarela");
        }
    }
}