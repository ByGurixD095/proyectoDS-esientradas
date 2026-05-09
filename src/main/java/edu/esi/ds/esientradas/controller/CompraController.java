package edu.esi.ds.esientradas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.CompraRequest;
import edu.esi.ds.esientradas.dto.CompraResponse;
import edu.esi.ds.esientradas.dto.ConfirmRequest;
import edu.esi.ds.esientradas.dto.DtoEntradaComprada;
import edu.esi.ds.esientradas.service.ICompraService;
import edu.esi.ds.esientradas.service.IEntradaService;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "*")
public class CompraController {

    private final ICompraService service;
    private final IEntradaService entradaService;

    public CompraController(ICompraService service, IEntradaService entradaService) {
        this.service = service;
        this.entradaService = entradaService;
    }

    // GET
    @GetMapping("/mis-entradas")
    public ResponseEntity<List<DtoEntradaComprada>> misEntradas(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = service.getEmailFromToken(token);
        return ResponseEntity.ok(entradaService.getEntradasCompradasByEmail(email));
    }

    // POST

    @PostMapping("/prepay")
    public ResponseEntity<CompraResponse> prepay(@RequestBody CompraRequest request) {
        try {
            return ResponseEntity
                    .ok(this.service.crearPaymentIntent(request.precio(), request.tokenPrerreserva(),
                            request.tokenUsuario()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestBody ConfirmRequest request) {
        try {
            service.confirmarCompra(
                    request.paymentIntentId(),
                    request.tokenPrerreserva(),
                    request.email());

            return ResponseEntity.ok("Compra Exitosa.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al confirmar la compra:" + e.getMessage());
        }
    }

}