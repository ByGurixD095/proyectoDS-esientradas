package edu.esi.ds.esientradas.service.implementation;

import edu.esi.ds.esientradas.dto.ColaResponse;
import edu.esi.ds.esientradas.model.ColaVirtual;
import edu.esi.ds.esientradas.model.EstadoCola;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.repository.ColaVirtualDAO;
import edu.esi.ds.esientradas.repository.EspectaculoDAO;
import edu.esi.ds.esientradas.service.IColaService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ColaService implements IColaService {
        private static final int MINUTOS_TURNO = 5;

        private final ColaVirtualDAO colaDAO;
        private final EspectaculoDAO espectaculoDAO;

        public ColaService(ColaVirtualDAO colaDAO, EspectaculoDAO espectaculoDAO) {
                this.colaDAO = colaDAO;
                this.espectaculoDAO = espectaculoDAO;
        }

        // ── UNIRSE A LA COLA ──────────────────────────────────────────────────────

        @Transactional
        public ColaResponse unirse(Long espectaculoId, String correoUsuario) {
                Espectaculo esp = espectaculoDAO.findById(espectaculoId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Espectaculo no encontrado."));

                if (!esp.isColaActiva()) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT, "La cola no esta activa para este espectaculo.");
                }

                // Si ya esta en la cola, devolvemos su estado actual
                Optional<ColaVirtual> existente = colaDAO
                                .findByEspectaculoIdAndCorreoUsuario(espectaculoId, correoUsuario);
                if (existente.isPresent()) {
                        return toDto(existente.get(), espectaculoId);
                }

                // Calculamos la siguiente posicion libre
                List<ColaVirtual> esperando = colaDAO
                                .findByEspectaculoIdAndEstadoOrderByPosicionAsc(espectaculoId, EstadoCola.ESPERANDO);
                int nuevaPosicion = esperando.isEmpty()
                                ? 1
                                : esperando.get(esperando.size() - 1).getPosicion() + 1;

                ColaVirtual entrada = new ColaVirtual();
                entrada.setEspectaculo(esp);
                entrada.setCorreoUsuario(correoUsuario);
                entrada.setPosicion(nuevaPosicion);
                entrada.setEstado(EstadoCola.ESPERANDO);
                entrada.setUnidoEn(LocalDateTime.now());
                colaDAO.save(entrada);

                // Si no hay nadie ACTIVO, activamos al primero de la cola (que puede ser este
                // mismo)
                _activarSiguienteSiLibre(espectaculoId);

                // Recargamos para obtener el estado actualizado
                ColaVirtual guardada = colaDAO
                                .findByEspectaculoIdAndCorreoUsuario(espectaculoId, correoUsuario)
                                .orElse(entrada);

                return toDto(guardada, espectaculoId);
        }

        // ── CONSULTAR POSICION ────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public ColaResponse consultarPosicion(Long espectaculoId, String correoUsuario) {
                ColaVirtual entrada = colaDAO
                                .findByEspectaculoIdAndCorreoUsuario(espectaculoId, correoUsuario)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "No estas en la cola de este espectaculo."));

                return toDto(entrada, espectaculoId);
        }

        // ── ABANDONAR LA COLA ─────────────────────────────────────────────────────

        @Transactional
        public ColaResponse abandonar(Long espectaculoId, String correoUsuario) {
                ColaVirtual entrada = colaDAO
                                .findByEspectaculoIdAndCorreoUsuario(espectaculoId, correoUsuario)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "No estas en la cola de este espectaculo."));

                boolean eraActivo = entrada.getEstado() == EstadoCola.ACTIVO;
                colaDAO.delete(entrada);

                // Si era el turno activo, avanzamos al siguiente
                if (eraActivo) {
                        _activarSiguienteSiLibre(espectaculoId);
                }

                return new ColaResponse(null, null, 0, "ABANDONADO", false, null);
        }

        // ── MARCAR COMPRA COMPLETADA ──────────────────────────────────────────────
        // Llamado desde EntradaService tras confirmar la compra con exito

        @Transactional
        public void marcarCompletado(Long espectaculoId, String correoUsuario) {
                colaDAO.findByEspectaculoIdAndCorreoUsuario(espectaculoId, correoUsuario)
                                .ifPresent(entrada -> {
                                        entrada.setEstado(EstadoCola.COMPLETADO);
                                        colaDAO.save(entrada);
                                        _activarSiguienteSiLibre(espectaculoId);
                                });
        }

        // ── ACTIVAR / DESACTIVAR COLA (para admin) ────────────────────────────────

        @Transactional
        public void activarCola(Long espectaculoId) {
                Espectaculo esp = espectaculoDAO.findById(espectaculoId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Espectaculo no encontrado."));
                esp.setColaActiva(true);
                espectaculoDAO.save(esp);
        }

        @Transactional
        public void desactivarCola(Long espectaculoId) {
                Espectaculo esp = espectaculoDAO.findById(espectaculoId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Espectaculo no encontrado."));
                esp.setColaActiva(false);
                espectaculoDAO.save(esp);
        }

        @Transactional
        public void expirarTurnosVencidos() {
                LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_TURNO);
                List<ColaVirtual> expirados = colaDAO.findTurnosExpirados(limite);

                if (expirados.isEmpty())
                        return;

                for (ColaVirtual c : expirados) {
                        c.setEstado(EstadoCola.EXPIRADO);
                        colaDAO.save(c);
                        System.out.println("[ColaService] Turno expirado para: " + c.getCorreoUsuario()
                                        + " en espectaculo " + c.getEspectaculo().getId());
                        // Activar siguiente para cada espectaculo afectado
                        _activarSiguienteSiLibre(c.getEspectaculo().getId());
                }
        }

        // ── HELPERS ───────────────────────────────────────────────────────────────

        private void _activarSiguienteSiLibre(Long espectaculoId) {
                // Solo activamos si no hay ningun turno ACTIVO actualmente
                boolean hayActivo = colaDAO
                                .findByEspectaculoIdAndEstado(espectaculoId, EstadoCola.ACTIVO)
                                .isPresent();

                if (hayActivo)
                        return;

                // Tomamos el primero ESPERANDO
                List<ColaVirtual> esperando = colaDAO
                                .findByEspectaculoIdAndEstadoOrderByPosicionAsc(espectaculoId, EstadoCola.ESPERANDO);

                if (esperando.isEmpty())
                        return;

                ColaVirtual siguiente = esperando.get(0);
                siguiente.setEstado(EstadoCola.ACTIVO);
                siguiente.setTurnoActivadoEn(LocalDateTime.now());
                colaDAO.save(siguiente);

                System.out.println("[ColaService] Turno activado para: " + siguiente.getCorreoUsuario()
                                + " (pos " + siguiente.getPosicion() + ") en espectaculo " + espectaculoId);
        }

        private ColaResponse toDto(ColaVirtual c, Long espectaculoId) {
                boolean esTuTurno = c.getEstado() == EstadoCola.ACTIVO;

                LocalDateTime expira = esTuTurno && c.getTurnoActivadoEn() != null
                                ? c.getTurnoActivadoEn().plusMinutes(MINUTOS_TURNO)
                                : null;

                long delante = c.getEstado() == EstadoCola.ESPERANDO
                                ? colaDAO.countByEspectaculoIdAndEstadoAndPosicionLessThan(
                                                espectaculoId, EstadoCola.ESPERANDO, c.getPosicion())
                                : 0;

                // Si hay alguien ACTIVO y este usuario esta ESPERANDO, sumamos 1 (el activo
                // esta delante)
                if (c.getEstado() == EstadoCola.ESPERANDO) {
                        boolean hayActivo = colaDAO
                                        .findByEspectaculoIdAndEstado(espectaculoId, EstadoCola.ACTIVO)
                                        .isPresent();
                        if (hayActivo)
                                delante += 1;
                }

                return new ColaResponse(
                                c.getId(),
                                c.getPosicion(),
                                delante,
                                c.getEstado().name(),
                                esTuTurno,
                                expira);
        }
}