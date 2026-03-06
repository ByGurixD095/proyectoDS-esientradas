package edu.esi.ds.esientradas.controller;

import edu.esi.ds.esientradas.service.ColaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/espectaculos/{espectaculoId}/cola")
@CrossOrigin(origins = "*")
public class ColaEsperaController {

    @Autowired
    private ColaService colaService;

    @PostMapping
    public ResponseEntity<Void> join(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<Void> position(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> leave(
            @PathVariable Long espectaculoId,
            @RequestHeader("X-User-Email") String correoUsuario) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
