package edu.esi.ds.esientradas.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esientradas.model.Espectaculo;

@Repository
public interface EspectaculoDAO extends JpaRepository<Espectaculo, Long> {

    List<Espectaculo> findByArtistaContainingIgnoreCase(String artista);

    List<Espectaculo> findByFechaBetween(LocalDateTime start, LocalDateTime end);

    List<Espectaculo> findByEscenarioId(Long escenarioId);

    @Query("SELECT e FROM Espectaculo e JOIN e.escenario esc WHERE UPPER(esc.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Espectaculo> buscarPorNombreEscenario(@Param("nombre") String nombre);
}