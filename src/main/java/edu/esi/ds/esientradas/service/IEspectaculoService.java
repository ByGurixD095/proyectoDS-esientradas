package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import java.time.LocalDate;
import java.util.List;

public interface IEspectaculoService {
    DtoEspectaculo getEspectaculoById(Long id);

    List<DtoEspectaculo> getEspectaculos();

    List<DtoEspectaculo> getEspectaculoByArtist(String artista);

    List<DtoEspectaculo> getEspectaculoByDate(LocalDate fecha);

    List<DtoEspectaculo> getEspectaculoByEscenario(String nombre);

    List<DtoEspectaculo> getEspectaculoByEscenario(Long escenarioId);
}