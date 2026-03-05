package edu.esi.ds.esientradas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

@Repository
public interface EntradasDAO extends JpaRepository<Entrada, Long> {

    List<Entrada> findByEspectaculoIdAndEstado(Long id, Estado disponible);

    List<Entrada> findByTokenPrerreserva(String token);

    List<Entrada> findByEstadoAndFechaPrerreservaBefore(Estado estado, LocalDateTime fecha);

    int countByEspectaculoIdAndEstado(Long espectaculoId, Estado disponible);

    @Query(value = "SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS libres, " +
            "SUM(CASE WHEN estado = 'RESERVADA' THEN 1 ELSE 0 END) AS reservadas, " +
            "SUM(CASE WHEN estado = 'VENDIDA' THEN 1 ELSE 0 END) AS vendidas " +
            "FROM entrada WHERE espectaculo_id = :id", nativeQuery = true)
    DtoEntradaInfo getInfoEntrada(@Param("id") Long id);
}
