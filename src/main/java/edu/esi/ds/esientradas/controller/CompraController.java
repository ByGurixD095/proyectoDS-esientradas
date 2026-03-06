package edu.esi.ds.esientradas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.CompraRequest;
import edu.esi.ds.esientradas.dto.CompraResponse;
import edu.esi.ds.esientradas.service.EntradaService;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins = "*")
public class CompraController {

    @Autowired
    EntradaService service;

    // POST /compras
    @PostMapping
    public ResponseEntity<CompraResponse> comprar(@RequestBody CompraRequest request) {
        try {
            return ResponseEntity.ok(service.comprar(request.tokenPrerreserva(), request.tokenUsuario()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}