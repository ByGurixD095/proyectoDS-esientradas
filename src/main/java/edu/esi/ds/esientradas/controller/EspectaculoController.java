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

    @GetMapping
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculos() {
        return ResponseEntity.ok(this.service.getEspectaculos());
    }

    @GetMapping(params = "id")
    public ResponseEntity<DtoEspectaculo> getEspectaculo(@RequestParam Long id) {

        DtoEspectaculo result = this.service.getEspectaculoById(id);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró ningún espectáculo con el ID especificado.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping(params = "artist")
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculo(@RequestParam String artist) {

        List<DtoEspectaculo> result = this.service.getEspectaculoByArtist(artist);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontraron espectáculos para el artista especificado.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{escenario}")
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculoByEscenario(@PathVariable String escenario) {
        List<DtoEspectaculo> result = this.service.getEspectaculoByEscenario(escenario);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontraron espectáculos para el artista especificado.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping(params = "date")
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculo(@RequestParam LocalDate date) {

        List<DtoEspectaculo> result = this.service.getEspectaculoByDate(date);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontraron espectáculos para la fecha especificada.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping(params = "escenarioId")
    public ResponseEntity<List<DtoEspectaculo>> getEspectaculos(@RequestParam Long escenarioId) {

        List<DtoEspectaculo> result = this.service.getEspectaculoByEscenario(escenarioId);

        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontraron espectáculos para la fecha especificada.");
        }

        return ResponseEntity.ok(result);
    }
}
