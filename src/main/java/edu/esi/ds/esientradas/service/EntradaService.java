package edu.esi.ds.esientradas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    EntradasDAO dao;

    @Autowired
    UserService usuariosClient;

    @Autowired
    CorreoService correoService;

    // CONSULTAS
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

    // PRERRESERVA
    @Transactional
    public ReservaResponse prerreservar(Long id, String token) {
        Entrada entrada = dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada."));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new IllegalArgumentException("La entrada no está disponible.");
        }

        String tokenFinal;
        if (token == null || token.isBlank()) {
            tokenFinal = UUID.randomUUID().toString();
        } else {
            boolean tokenVigente = dao.findByTokenPrerreserva(token).stream()
                    .anyMatch(e -> e.getEstado() == Estado.RESERVADA
                            && e.getFechaPrerreserva() != null
                            && e.getFechaPrerreserva().plusMinutes(10).isAfter(LocalDateTime.now()));

            if (!tokenVigente) {
                throw new IllegalArgumentException("La sesión de reserva ha expirado.");
            }
            tokenFinal = token;
        }

        entrada.setEstado(Estado.RESERVADA);
        entrada.setTokenPrerreserva(tokenFinal);
        entrada.setFechaPrerreserva(LocalDateTime.now());
        dao.save(entrada);

        return new ReservaResponse(entrada.getId(), tokenFinal, entrada.getFechaPrerreserva().plusMinutes(10));
    }

    @Transactional
    public void cancelarPrerreserva(Long id, String token) {
        Entrada entrada = dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrada no encontrada."));

        if (!token.equals(entrada.getTokenPrerreserva())) {
            throw new IllegalArgumentException("Token no válido para esta entrada.");
        }

        liberarEntrada(entrada);
        dao.save(entrada);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void liberarEntradasCaducadas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);
        List<Entrada> caducadas = dao.findByEstadoAndFechaPrerreservaBefore(Estado.RESERVADA, limite);

        if (!caducadas.isEmpty()) {
            caducadas.forEach(this::liberarEntrada);
            dao.saveAll(caducadas);
        }
    }

    // COMPRA
    public boolean canBuy(String tokenPrerreserva, String tokenUsuario) {
        String email = usuariosClient.validarTokenYObtenerCorreo(tokenUsuario);
        if (email.isEmpty() || email == null) {
            return false;
        }

        List<Entrada> entradas = dao.findByTokenPrerreserva(tokenPrerreserva);
        if (entradas.isEmpty()) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (Entrada e : entradas) {
            if (e.getEstado() != Estado.RESERVADA) {
                return false;
            }
            if (e.getFechaPrerreserva() == null || e.getFechaPrerreserva().plusMinutes(10).isBefore(ahora)) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public void confirmarCompra(String tokenPrerreserva, String email) {
        List<Entrada> entradas = dao.findByTokenPrerreserva(tokenPrerreserva);
        if (entradas.isEmpty()) {
            throw new IllegalStateException("No hay entradas con ese token.");
        }
        entradas.forEach(e -> marcarComoVendida(e, email));
        dao.saveAll(entradas);
        correoService.enviarEntradas(email, entradas);
    }

    // HELPERS PRIVADOS

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

    // MAPPING
    private List<DtoEntrada> toDto(List<Entrada> entradas) {
        return entradas.stream().map(this::toDto).toList();
    }

    private DtoEntrada toDto(Entrada e) {
        DtoEntrada dto;

        if (e instanceof DeZona zona) {
            DtoEntradaDeZona d = new DtoEntradaDeZona();
            d.setZona(zona.getZona());
            d.setTipo("ZONA");
            dto = d;
        } else if (e instanceof Precisa precisa) {
            DtoEntradaPrecisa d = new DtoEntradaPrecisa();
            d.setFila(precisa.getFila());
            d.setColumna(precisa.getColumna());
            d.setPlanta(precisa.getPlanta());
            d.setTipo("PRECISA");
            dto = d;
        } else {
            throw new IllegalStateException("Tipo de entrada no reconocido.");
        }

        dto.setId(e.getId());
        dto.setEspectaculoId(e.getEspectaculo().getId());
        if (e.getPrecio() != null) {
            dto.setPrecio(BigDecimal.valueOf(e.getPrecio(), 2));
        }

        return dto;
    }
}