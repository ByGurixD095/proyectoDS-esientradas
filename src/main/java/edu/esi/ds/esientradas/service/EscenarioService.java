package edu.esi.ds.esientradas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.dto.DtoEscenario;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.repository.EscenarioDAO;

@Service
@Transactional(readOnly = true)
public class EscenarioService {

    @Autowired
    EscenarioDAO dao;

    public List<DtoEscenario> getEscenarios() {
        return dao.findAll().stream().map(this::toDto).toList();
    }

    public DtoEscenario getEscenarioById(Long id) {
        return dao.findById(id).map(this::toDto).orElse(null);
    }

    private DtoEscenario toDto(Escenario e) {
        return new DtoEscenario(e.getId(), e.getNombre(), e.getDescripcion());
    }
}