package edu.esi.ds.esientradas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.esi.ds.esientradas.dto.CompraRequest;
import edu.esi.ds.esientradas.dto.CompraResponse;
import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.dto.ReservaResponse;
import edu.esi.ds.esientradas.service.EntradaService;

@RestController
@RequestMapping("/entradas")
@CrossOrigin(origins = "*")
public class EntradaController {

    @Autowired
    EntradaService service;

    @GetMapping("/espectaculos/{espectaculoId}")
    public ResponseEntity<List<DtoEntrada>> getEntradaByEspectaculoId(@PathVariable Long espectaculoId) {
        List<DtoEntrada> result = this.service.getEntradasByEspectaculoId(espectaculoId);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se encontraron entradas para el espectaculo especificado.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/espectaculo/{espectaculoId}/info")
    public ResponseEntity<DtoEntradaInfo> getEntradaInfo(@PathVariable Long espectaculoId) {
        return ResponseEntity.ok(this.service.getInfoEntradas(espectaculoId));
    }

    @GetMapping("/espectaculos/{espectaculoId}/cantidad")
    public ResponseEntity<Integer> getNumeroEntradas(@PathVariable Long espectaculoId) {
        int entradas = this.service.getNumeroEntradas(espectaculoId);

        if (entradas == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se encontraron entradas para el espectaculo especificado.");
        }

        return ResponseEntity.ok(entradas);
    }

    @GetMapping("/{entradaId}")
    public ResponseEntity<DtoEntrada> getEntradaById(@PathVariable Long entradaId) {
        DtoEntrada result = this.service.getEntradaById(entradaId);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró la entrada para el id especificado.");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{entradaId}/prerreservar")
    public ResponseEntity<ReservaResponse> prerreservarEntrada(
            @PathVariable Long entradaId,
            @RequestBody ReservaResponse body) {

        return ResponseEntity.ok(this.service.prerreservar(entradaId, body.token()));
    }

    @DeleteMapping("/{entradaId}/prerreservar/{token}")
    public ResponseEntity<Void> cancelarPrerreserva(@PathVariable Long entradaId, @PathVariable String token) {
        this.service.cancelarPrerreserva(entradaId, token);

        return ResponseEntity.noContent().build();
    }

    // COMPRAR
    @PostMapping("/comprar")
    public ResponseEntity<CompraResponse> comprarEntradas(@RequestBody CompraRequest request) {
        try {
            return ResponseEntity.ok(this.service.comprar(request.tokenPrerreserva(), request.tokenUsuario()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
