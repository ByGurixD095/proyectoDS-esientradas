package edu.esi.ds.esientradas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.dto.DtoEscenario;
import edu.esi.ds.esientradas.service.EscenarioService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/escenarios")
@CrossOrigin(origins = "*")
public class EscenarioController {

    @Autowired
    EscenarioService service;

    @GetMapping
    public ResponseEntity<List<DtoEscenario>> getEscenarios() {
        return ResponseEntity.ok(this.service.getEscenarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DtoEscenario> getEscenarios(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.getEscenarioById(id));
    }

}
