package edu.esi.ds.esientradas.service.implementation;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.ColaResponse;
import edu.esi.ds.esientradas.dto.CompraResponse;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.service.IColaService;
import edu.esi.ds.esientradas.service.ICompraService;
import edu.esi.ds.esientradas.service.ICorreoService;
import edu.esi.ds.esientradas.service.IEntradaService;
import edu.esi.ds.esientradas.service.IPasarelaPago;
import edu.esi.ds.esientradas.service.IUsuarioService;

@Service
public class CompraService implements ICompraService {

    private final IPasarelaPago pasarelaPago;
    private final IEntradaService entradaService;
    private final IUsuarioService userService;
    private final IColaService colaService;
    private final ICorreoService correoService;

    public CompraService(IPasarelaPago pasarelaPago, IEntradaService entradaService,
            IUsuarioService userService, IColaService colaService,
            ICorreoService correoService) {
        this.pasarelaPago = pasarelaPago;
        this.entradaService = entradaService;
        this.userService = userService;
        this.colaService = colaService;
        this.correoService = correoService;
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

        List<Entrada> entradas = entradaService.obtenerReservadasPorToken(tokenPrerreserva);
        if (entradas.isEmpty()) {
            throw new IllegalArgumentException("No hay entradas prerreservadas con ese token.");
        }

        boolean colaActiva = entradas.get(0).getEspectaculo().isColaActiva();
        if (colaActiva) {
            Long espectaculoId = entradas.get(0).getEspectaculo().getId();
            try {
                ColaResponse posicion = colaService.consultarPosicion(espectaculoId, email);
                if (!posicion.esTuTurno()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aún no es tu turno en la cola. Posición: " + posicion.posicion());
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Debes unirte a la cola para este espectáculo.");
            }
        }

        String clientSecret = pasarelaPago.crearIntencionPago(precioCentimos, tokenPrerreserva, email);
        return new CompraResponse(clientSecret, email);
    }

    @Override
    @Transactional
    public void confirmarCompra(String paymentIntentId, String tokenPrerreserva, String email) {
        pasarelaPago.verificarEstadoPago(paymentIntentId, tokenPrerreserva);

        List<Entrada> entradas = entradaService.obtenerReservadasPorToken(tokenPrerreserva);
        if (entradas.get(0).getEstado() == Estado.VENDIDA) {
            return;
        }

        Long espectaculoId = entradas.get(0).getEspectaculo().getId();
        boolean colaActiva = entradas.get(0).getEspectaculo().isColaActiva();

        if (colaActiva) {
            try {
                ColaResponse posicion = colaService.consultarPosicion(espectaculoId, email);
                if (!posicion.esTuTurno()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aún no es tu turno. Posición: " + posicion.posicion());
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Debes unirte a la cola para este espectáculo.");
            }
        }

        entradas = entradaService.consolidarVenta(tokenPrerreserva, email);

        if (colaActiva) {
            colaService.marcarCompletado(espectaculoId, email);
        }
        correoService.enviarEntradas(email, entradas);
    }
}