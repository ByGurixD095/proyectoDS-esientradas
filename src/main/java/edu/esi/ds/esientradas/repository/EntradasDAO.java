package edu.esi.ds.esientradas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import jakarta.persistence.LockModeType;

@Repository
public interface EntradasDAO extends JpaRepository<Entrada, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Entrada e WHERE e.id = :id")
    Optional<Entrada> findByIdWithLock(@Param("id") Long id);

    List<Entrada> findByEspectaculoIdAndEstado(Long id, Estado disponible);

    List<Entrada> findByTokenPrerreserva(String token);

    List<Entrada> findByEstadoAndFechaPrerreservaBefore(Estado estado, LocalDateTime fecha);

    int countByEspectaculoIdAndEstado(Long espectaculoId, Estado disponible);

    @Query("SELECT new edu.esi.ds.esientradas.dto.DtoEntradaInfo(" +
            "COUNT(e), " +
            "SUM(CASE WHEN e.estado = edu.esi.ds.esientradas.model.Estado.DISPONIBLE THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN e.estado = edu.esi.ds.esientradas.model.Estado.RESERVADA THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN e.estado = edu.esi.ds.esientradas.model.Estado.VENDIDA THEN 1L ELSE 0L END)) " +
            "FROM Entrada e WHERE e.espectaculo.id = :id")
    DtoEntradaInfo getInfoEntrada(@Param("id") Long id);
}
