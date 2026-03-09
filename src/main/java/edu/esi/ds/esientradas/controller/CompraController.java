package edu.esi.ds.esientradas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.CompraRequest;
import edu.esi.ds.esientradas.service.CompraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "")
public class CompraController {

    @Autowired
    CompraService service;

    @GetMapping("/prepay")
    public ResponseEntity<String> prepay(@RequestBody CompraRequest request) {
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
    public ResponseEntity<Void> confirm(@RequestBody ConfirmRequest request) {
        // TODO: process POST request

        return entity;
    }

}