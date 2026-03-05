package edu.esi.ds.esientradas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.dto.DtoEscenario;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.repository.EscenarioDAO;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EscenarioService {

    @Autowired
    EscenarioDAO dao;

    @Transactional(readOnly = true)
    public List<DtoEscenario> getEscenarios() {
        return mapToDtoList(dao.findAll());
    }

    private List<DtoEscenario> mapToDtoList(List<Escenario> escenarios) {
        return escenarios.stream().map(this::toDto).toList();
    }

    private DtoEscenario toDto(Escenario e) {
        DtoEscenario dto = new DtoEscenario();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setDescripcion(e.getDescripcion());

        return dto;
    }

}
