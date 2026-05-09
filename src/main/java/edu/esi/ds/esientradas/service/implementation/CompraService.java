package edu.esi.ds.esientradas.service.implementation;

import org.springframework.stereotype.Service;
import edu.esi.ds.esientradas.dto.CompraResponse;
import edu.esi.ds.esientradas.service.ICompraService;
import edu.esi.ds.esientradas.service.IEntradaService;
import edu.esi.ds.esientradas.service.IPasarelaPago;
import edu.esi.ds.esientradas.service.IUsuarioService;

@Service
public class CompraService implements ICompraService {

    private final IPasarelaPago pasarelaPago;
    private final IEntradaService entradaService;
    private final IUsuarioService userService;

    public CompraService(IPasarelaPago pasarelaPago, IEntradaService entradaService, IUsuarioService userService) {
        this.pasarelaPago = pasarelaPago;
        this.entradaService = entradaService;
        this.userService = userService;
    }

    @Override
    public String getEmailFromToken(String token) {
        return userService.validarTokenYObtenerCorreo(token);
    }

    @Override
    public CompraResponse crearPaymentIntent(Long precioCentimos, String tokenPrerreserva, String tokenUsuario) {
        String email = userService.validarTokenYObtenerCorreo(tokenUsuario);
        if (email == null) {
            throw new IllegalArgumentException("Token de usuario inválido.");
        }

        String clientSecret = pasarelaPago.crearIntencionPago(precioCentimos, tokenPrerreserva, email);
        return new CompraResponse(clientSecret, email);
    }

    @Override
    public void confirmarCompra(String paymentIntentId, String tokenPrerreserva, String email) {
        pasarelaPago.verificarEstadoPago(paymentIntentId, tokenPrerreserva);
        entradaService.confirmarCompra(tokenPrerreserva, email);
    }
}