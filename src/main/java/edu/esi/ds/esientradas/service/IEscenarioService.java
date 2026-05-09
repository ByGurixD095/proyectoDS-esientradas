package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.dto.DtoEscenario;
import java.util.List;

public interface IEscenarioService {
    List<DtoEscenario> getEscenarios();

    DtoEscenario getEscenarioById(Long id);
}