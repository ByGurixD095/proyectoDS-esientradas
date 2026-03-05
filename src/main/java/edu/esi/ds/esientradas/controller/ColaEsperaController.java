package edu.esi.ds.esientradas.controller;

import edu.esi.ds.esientradas.dto.ColaResponse;
import edu.esi.ds.esientradas.service.ColaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/espectaculos/{espectaculoId}/cola")
@CrossOrigin(origins = "*")
public class ColaEsperaController {

    @Autowired
    private ColaService colaService;

    @PostMapping
    public ResponseEntity<ColaResponse> join(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.unirse(espectaculoId, correoUsuario));
    }

    @GetMapping
    public ResponseEntity<ColaResponse> position(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.consultarPosicion(espectaculoId, correoUsuario));
    }

    @DeleteMapping
    public ResponseEntity<ColaResponse> leave(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(colaService.abandonar(espectaculoId, correoUsuario));
    }
}
