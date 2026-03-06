package edu.esi.ds.esientradas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.service.EspectaculoService;

@RestController
@RequestMapping("/espectaculos")
@CrossOrigin(origins = "*")
public class EspectaculoController {

    @Autowired
    EspectaculoService service;

    // GET /espectaculos
    // GET /espectaculos?artista=Radiohead
    // GET /espectaculos?fecha=2026-03-14
    // GET /espectaculos?escenario=Palacio
    // GET /espectaculos?escenarioId=3
    // Todos los filtros son opcionales y se pueden combinar desde el service.
    // Si no se pasa ningún parámetro, devuelve todos.

    @GetMapping
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculos(
            @RequestParam(required = false) String artista,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) String escenario,
            @RequestParam(required = false) Long escenarioId) {

        List<DtoEspectaculo> result;

        if (artista != null) {
            result = service.getEspectaculoByArtist(artista);
        } else if (fecha != null) {
            result = service.getEspectaculoByDate(fecha);
        } else if (escenarioId != null) {
            result = service.getEspectaculoByEscenario(escenarioId);
        } else if (escenario != null) {
            result = service.getEspectaculoByEscenario(escenario);
        } else {
            result = service.getEspectaculos();
        }

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontraron espectáculos para los filtros especificados.");
        }

        return ResponseEntity.ok(result);
    }

    // GET /espectaculos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DtoEspectaculo> getEspectaculoById(@PathVariable Long id) {
        DtoEspectaculo result = service.getEspectaculoById(id);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró ningún espectáculo con el ID especificado.");
        }

        return ResponseEntity.ok(result);
    }
}