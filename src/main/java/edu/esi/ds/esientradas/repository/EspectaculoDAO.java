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

    @Query("SELECT e FROM Espectaculo e JOIN FETCH e.escenario")
    List<Espectaculo> findAll();

    @Query("SELECT e FROM Espectaculo e JOIN FETCH e.escenario WHERE UPPER(e.artista) LIKE UPPER(CONCAT('%', :artista, '%'))")
    List<Espectaculo> findByArtistaContainingIgnoreCase(@Param("artista") String artista);

    @Query("SELECT e FROM Espectaculo e JOIN FETCH e.escenario WHERE e.fecha BETWEEN :start AND :end")
    List<Espectaculo> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Espectaculo e JOIN FETCH e.escenario WHERE e.escenario.id = :escenarioId")
    List<Espectaculo> findByEscenarioId(@Param("escenarioId") Long escenarioId);

    @Query("SELECT e FROM Espectaculo e JOIN FETCH e.escenario esc WHERE UPPER(esc.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))")
    List<Espectaculo> buscarPorNombreEscenario(@Param("nombre") String nombre);
}