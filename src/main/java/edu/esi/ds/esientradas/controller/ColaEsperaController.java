package edu.esi.ds.esientradas.controller;

import edu.esi.ds.esientradas.dto.ColaResponse;
import edu.esi.ds.esientradas.service.IColaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/espectaculos/{espectaculoId}/cola")
@CrossOrigin(origins = "*")
public class ColaEsperaController {

    private final IColaService colaService;

    public ColaEsperaController(IColaService colaService) {
        this.colaService = colaService;
    }

    // ── Usuario: consultar posicion ───────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ColaResponse> consultarPosicion(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.consultarPosicion(espectaculoId, correoUsuario));
    }

    // ── Usuario: unirse a la cola ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ColaResponse> unirse(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.unirse(espectaculoId, correoUsuario));
    }

    // ── Usuario: abandonar la cola ────────────────────────────────────────────
    @DeleteMapping
    public ResponseEntity<ColaResponse> abandonar(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.abandonar(espectaculoId, correoUsuario));
    }

    // ── Admin: activar cola ───────────────────────────────────────────────────
    @PostMapping("/activar")
    public ResponseEntity<Void> activar(@PathVariable Long espectaculoId) {
        colaService.activarCola(espectaculoId);
        return ResponseEntity.ok().build();
    }

    // ── Admin: desactivar cola ────────────────────────────────────────────────
    @DeleteMapping("/activar")
    public ResponseEntity<Void> desactivar(@PathVariable Long espectaculoId) {
        colaService.desactivarCola(espectaculoId);
        return ResponseEntity.ok().build();
    }
}