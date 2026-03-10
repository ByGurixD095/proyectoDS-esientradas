package edu.esi.ds.esientradas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.dto.ReservaResponse;
import edu.esi.ds.esientradas.service.EntradaService;

@RestController
@RequestMapping("/espectaculos/{espectaculoId}/entradas")
@CrossOrigin(origins = "*")
public class EntradaController {

    @Autowired
    EntradaService service;

    // GET

    @GetMapping
    public ResponseEntity<List<DtoEntrada>> getEntradas(@PathVariable Long espectaculoId) {
        return ResponseEntity.ok(service.getEntradasByEspectaculoId(espectaculoId));
    }

    @GetMapping("/info")
    public ResponseEntity<DtoEntradaInfo> getInfo(@PathVariable Long espectaculoId) {
        return ResponseEntity.ok(service.getInfoEntradas(espectaculoId));
    }

    @GetMapping("/cantidad")
    public ResponseEntity<Integer> getCantidad(@PathVariable Long espectaculoId) {
        return ResponseEntity.ok(service.getNumeroEntradas(espectaculoId));
    }

    @GetMapping("/{entradaId}")
    public ResponseEntity<DtoEntrada> getEntradaById(
            @PathVariable Long espectaculoId,
            @PathVariable Long entradaId) {
        DtoEntrada result = service.getEntradaById(entradaId);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró la entrada especificada.");
        }

        return ResponseEntity.ok(result);
    }

    // POST

    @PostMapping("/{entradaId}/prerreservar")
    public ResponseEntity<ReservaResponse> prerreservar(
            @PathVariable Long espectaculoId,
            @PathVariable Long entradaId,
            @RequestBody ReservaResponse body) {
        return ResponseEntity.ok(service.prerreservar(entradaId, body.token()));
    }

    // DELETE

    @DeleteMapping("/{entradaId}/prerreservar/{token}")
    public ResponseEntity<Void> cancelarPrerreserva(
            @PathVariable Long espectaculoId,
            @PathVariable Long entradaId,
            @PathVariable String token) {
        service.cancelarPrerreserva(entradaId, token);
        return ResponseEntity.noContent().build();
    }
}