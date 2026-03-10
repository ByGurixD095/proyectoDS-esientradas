package edu.esi.ds.esientradas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaDeZona;
import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.dto.DtoEntradaPrecisa;
import edu.esi.ds.esientradas.dto.ReservaResponse;
import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Precisa;
import edu.esi.ds.esientradas.repository.EntradasDAO;

@Service
public class EntradaService {

    private static final int TTL_MINUTOS = 10;

    @Autowired
    EntradasDAO dao;

    @Autowired
    UserService usuariosClient;

    @Autowired
    CorreoService correoService;

    // ── CONSULTAS ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DtoEntrada> getEntradasByEspectaculoId(Long id) {
        return toDto(dao.findByEspectaculoIdAndEstado(id, Estado.DISPONIBLE));
    }

    @Transactional(readOnly = true)
    public int getNumeroEntradas(Long espectaculoId) {
        return dao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public DtoEntradaInfo getInfoEntradas(Long idEspectaculo) {
        return dao.getInfoEntrada(idEspectaculo);
    }

    @Transactional(readOnly = true)
    public DtoEntrada getEntradaById(Long id) {
        return dao.findById(id).map(this::toDto).orElse(null);
    }

    // ── PRERRESERVA ────────────────────────────────────────────────────────────

    @Transactional
    public ReservaResponse prerreservar(Long id, String token) {
        Entrada entrada = dao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada."));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La entrada no está disponible.");
        }

        String tokenFinal = (token == null || token.isBlank())
                ? UUID.randomUUID().toString()
                : validarTokenVigente(token);

        entrada.setEstado(Estado.RESERVADA);
        entrada.setTokenPrerreserva(tokenFinal);
        entrada.setFechaPrerreserva(LocalDateTime.now());
        dao.save(entrada);

        return new ReservaResponse(entrada.getId(), tokenFinal, entrada.getFechaPrerreserva().plusMinutes(TTL_MINUTOS));
    }

    @Transactional
    public void cancelarPrerreserva(Long id, String token) {
        Entrada entrada = dao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada."));

        if (!token.equals(entrada.getTokenPrerreserva())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido para esta entrada.");
        }

        liberarEntrada(entrada);
        dao.save(entrada);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void liberarEntradasCaducadas() {
        List<Entrada> caducadas = dao.findByEstadoAndFechaPrerreservaBefore(
                Estado.RESERVADA, LocalDateTime.now().minusMinutes(TTL_MINUTOS));
        if (!caducadas.isEmpty()) {
            caducadas.forEach(this::liberarEntrada);
            dao.saveAll(caducadas);
        }
    }

    // ── COMPRA ─────────────────────────────────────────────────────────────────

    public String canBuy(String tokenPrerreserva, String tokenUsuario) {
        String email = usuariosClient.validarTokenYObtenerCorreo(tokenUsuario);

        List<Entrada> entradas = dao.findByTokenPrerreserva(tokenPrerreserva);
        if (entradas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay entradas prerreservadas con ese token.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (Entrada e : entradas) {
            if (e.getEstado() != Estado.RESERVADA) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Una de las entradas ya no está reservada.");
            }
            if (e.getFechaPrerreserva() == null || e.getFechaPrerreserva().plusMinutes(TTL_MINUTOS).isBefore(ahora)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de reserva ha expirado.");
            }
        }

        return email;
    }

    @Transactional
    public void confirmarCompra(String tokenPrerreserva, String email) {
        List<Entrada> entradas = dao.findByTokenPrerreserva(tokenPrerreserva);
        if (entradas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay entradas con ese token.");
        }
        entradas.forEach(e -> marcarComoVendida(e, email));
        dao.saveAll(entradas);
        correoService.enviarEntradas(email, entradas);
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private String validarTokenVigente(String token) {
        boolean vigente = dao.findByTokenPrerreserva(token).stream()
                .anyMatch(e -> e.getEstado() == Estado.RESERVADA
                        && e.getFechaPrerreserva() != null
                        && e.getFechaPrerreserva().plusMinutes(TTL_MINUTOS).isAfter(LocalDateTime.now()));

        if (!vigente) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión de reserva ha expirado.");
        }
        return token;
    }

    private void liberarEntrada(Entrada e) {
        e.setEstado(Estado.DISPONIBLE);
        e.setTokenPrerreserva(null);
        e.setFechaPrerreserva(null);
    }

    private void marcarComoVendida(Entrada e, String email) {
        e.setEstado(Estado.VENDIDA);
        e.setCorreoComprador(email);
        e.setTokenPrerreserva(null);
        e.setFechaPrerreserva(null);
    }

    // ── MAPPING ────────────────────────────────────────────────────────────────

    private List<DtoEntrada> toDto(List<Entrada> entradas) {
        return entradas.stream().map(this::toDto).toList();
    }

    private DtoEntrada toDto(Entrada e) {
        BigDecimal precio = (e.getPrecio() != null) ? BigDecimal.valueOf(e.getPrecio(), 2) : null;

        if (e instanceof DeZona zona) {
            return new DtoEntradaDeZona(e.getId(), e.getEspectaculo().getId(), precio, "ZONA", zona.getZona());
        } else if (e instanceof Precisa precisa) {
            return new DtoEntradaPrecisa(e.getId(), e.getEspectaculo().getId(), precio, "PRECISA",
                    precisa.getFila(), precisa.getColumna(), precisa.getPlanta());
        }
        throw new IllegalStateException("Tipo desconocido");
    }
}