package edu.esi.ds.esientradas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.repository.EspectaculoDAO;
import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.model.Espectaculo;

@Service
@Transactional(readOnly = true)
public class EspectaculoService {

    @Autowired
    private EspectaculoDAO dao;

    public DtoEspectaculo getEspectaculoById(Long id) {
        return dao.findById(id).map(this::toDto).orElse(null);
    }

    public List<DtoEspectaculo> getEspectaculos() {
        return toDto(dao.findAll());
    }

    public List<DtoEspectaculo> getEspectaculoByArtist(String artista) {
        return toDto(dao.findByArtistaContainingIgnoreCase(artista));
    }

    public List<DtoEspectaculo> getEspectaculoByDate(LocalDate fecha) {
        return toDto(dao.findByFechaBetween(fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX)));
    }

    public List<DtoEspectaculo> getEspectaculoByEscenario(String nombre) {
        return toDto(dao.buscarPorNombreEscenario(nombre));
    }

    public List<DtoEspectaculo> getEspectaculoByEscenario(Long escenarioId) {
        return toDto(dao.findByEscenarioId(escenarioId));
    }

    // MAPPING
    private List<DtoEspectaculo> toDto(List<Espectaculo> list) {
        return list.stream().map(this::toDto).toList();
    }

    private DtoEspectaculo toDto(Espectaculo e) {
        return new DtoEspectaculo(
                e.getId(),
                e.getArtista(),
                e.getFecha(),
                e.getEscenario().getNombre());
    }
}