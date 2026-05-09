package edu.esi.ds.esientradas.service.implementation;

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

    @Value("${stripe.api-key}")
    private String _key;

    private final EntradaService entradaService;
    private final UserService userService;

    public CompraService(EntradaService entradaService, UserService userService) {
        this.entradaService = entradaService;
        this.userService = userService;
    }

    public String getEmailFromToken(String token) {
        return userService.validarTokenYObtenerCorreo(token);
    }

    public CompraResponse crearPaymentIntent(Long precioCentimos, String tokenPrerreserva, String tokenUsuario) {
        Stripe.apiKey = _key;

        String email = userService.validarTokenYObtenerCorreo(tokenUsuario);
        if (email == null) {
            throw new IllegalArgumentException("Token de usuario inválido.");
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(precioCentimos)
                    .setCurrency("eur")
                    .putMetadata("tokenPrerreserva", tokenPrerreserva)
                    .setReceiptEmail(email)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return new CompraResponse(intent.getClientSecret(), email);

        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Error al conectar con el servicio de pagos");
        }
    }

    public void confirmarCompra(String paymentIntentId, String tokenPrerreserva, String email) throws StripeException {
        Stripe.apiKey = _key;

        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        if ("succeeded".equals(intent.getStatus())) {
            String tokenEnMetadata = intent.getMetadata().get("tokenPrerreserva");

            if (tokenPrerreserva.equals(tokenEnMetadata)) {
                this.entradaService.confirmarCompra(tokenPrerreserva, email);
            } else {
                throw new IllegalStateException("El token de prerreserva no coincide con el pago.");
            }
        } else {
            throw new IllegalStateException(
                    "El pago no tiene estado 'succeeded'. Estado actual: " + intent.getStatus());
        }
    }
}
