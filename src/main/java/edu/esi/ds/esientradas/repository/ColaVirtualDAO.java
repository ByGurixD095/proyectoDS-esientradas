package edu.esi.ds.esientradas.repository;

import edu.esi.ds.esientradas.model.ColaVirtual;
import edu.esi.ds.esientradas.model.EstadoCola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColaVirtualDAO extends JpaRepository<ColaVirtual, Long> {

    // Busca la entrada de un usuario en la cola de un espectaculo
    Optional<ColaVirtual> findByEspectaculoIdAndCorreoUsuario(
            Long espectaculoId, String correoUsuario);

    // Todos los ESPERANDO de un espectaculo, ordenados por posicion
    List<ColaVirtual> findByEspectaculoIdAndEstadoOrderByPosicionAsc(
            Long espectaculoId, EstadoCola estado);

    // El turno ACTIVO actual de un espectaculo (solo puede haber uno)
    Optional<ColaVirtual> findByEspectaculoIdAndEstado(
            Long espectaculoId, EstadoCola estado);

    // Turnos ACTIVOS que han superado el tiempo limite (para el job de expiracion)
    @Query("SELECT c FROM ColaVirtual c WHERE c.estado = 'ACTIVO' " +
            "AND c.turnoActivadoEn < :limite")
    List<ColaVirtual> findTurnosExpirados(@Param("limite") LocalDateTime limite);

    // Cuantos usuarios hay delante de una posicion dada
    long countByEspectaculoIdAndEstadoAndPosicionLessThan(
            Long espectaculoId, EstadoCola estado, Integer posicion);
}