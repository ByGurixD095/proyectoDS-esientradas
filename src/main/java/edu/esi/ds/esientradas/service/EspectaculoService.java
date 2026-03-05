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
public class EspectaculoService {

    @Autowired
    EspectaculoDAO dao;

    @Transactional(readOnly = true)
    public DtoEspectaculo getEspectaculoById(Long id) {
        Espectaculo e = dao.findById(id).orElse(null);
        if (e == null) {
            return null;
        }
        return toDto(e);
    }

    @Transactional(readOnly = true)
    public List<DtoEspectaculo> getEspectaculos() {
        return mapToDtoList(dao.findAll());
    }

    @Transactional(readOnly = true)
    public List<DtoEspectaculo> getEspectaculoByArtist(String artist) {
        return mapToDtoList(dao.findByArtistaContainingIgnoreCase(artist));
    }

    public List<DtoEspectaculo> getEspectaculoByEscenario(String escenario) {
        return mapToDtoList(dao.buscarPorNombreEscenario(escenario));
    }

    @Transactional(readOnly = true)
    public List<DtoEspectaculo> getEspectaculoByDate(LocalDate date) {
        return mapToDtoList(dao.findByFechaBetween(date.atStartOfDay(), date.atTime(LocalTime.MAX)));
    }

    @Transactional(readOnly = true)
    public List<DtoEspectaculo> getEspectaculoByEscenario(Long escenarioId) {
        return mapToDtoList(dao.findByEscenarioId(escenarioId));
    }

    // DATA MAPPING
    private List<DtoEspectaculo> mapToDtoList(List<Espectaculo> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        return list.stream()
                .map(this::toDto)
                .toList();
    }

    private DtoEspectaculo toDto(Espectaculo e) {
        DtoEspectaculo dto = new DtoEspectaculo();
        dto.setId(e.getId());
        dto.setArtista(e.getArtista());
        dto.setFecha(e.getFecha());
        dto.setEscenario(e.getEscenario());
        return dto;
    }
}