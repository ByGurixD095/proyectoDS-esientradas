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

    // GET

    @GetMapping
    public ResponseEntity<ColaResponse> position(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(this.colaService.consultarPosicion(espectaculoId, correoUsuario));
    }

    // POST

    @PostMapping
    public ResponseEntity<ColaResponse> join(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(this.colaService.unirse(espectaculoId, correoUsuario));
    }

    // DELETE

    @DeleteMapping
    public ResponseEntity<ColaResponse> leave(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.ok(this.colaService.abandonar(espectaculoId, correoUsuario));
    }
}
