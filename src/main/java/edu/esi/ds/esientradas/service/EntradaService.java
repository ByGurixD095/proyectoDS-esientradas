package edu.esi.ds.esientradas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.esi.ds.esientradas.dto.CompraResponse;
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

    @Transactional(readOnly = true)
    public List<DtoEntrada> getEntradasByEspectaculoId(Long id) {
        return mapToDto(this.dao.findByEspectaculoIdAndEstado(id, Estado.DISPONIBLE));
    }

    @Transactional(readOnly = true)
    public int getNumeroEntradas(Long espectaculoId) {
        return this.dao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public DtoEntradaInfo getInfoEntradas(Long idEspectaculo) {
        return this.dao.getInfoEntrada(idEspectaculo);
    }

    @Transactional(readOnly = true)
    public DtoEntrada getEntradaById(Long id) {
        return toDto(this.dao.findById(id).orElse(null));
    }

    // PRERRESERVA
    @Transactional
    public ReservaResponse prerreservar(Long id, String token) {
        Entrada e = this.dao.findById(id).orElse(null);

        if (e == null) {
            throw new IllegalArgumentException("No se encontró la entrada con el id especificado.");
        }

        if (e.getEstado() != Estado.DISPONIBLE) {
            throw new IllegalArgumentException("La entrada no está disponible para su reserva.");
        }

        String _token = "";

        if (token == null || token.isBlank()) {
            _token = UUID.randomUUID().toString();
        } else {
            validateToken(token);
            _token = token;
        }

        e.setEstado(Estado.RESERVADA);
        e.setTokenPrerreserva(_token);
        e.setFechaPrerreserva(LocalDateTime.now());

        this.dao.save(e);

        return new ReservaResponse(
                e.getId(),
                _token,
                e.getFechaPrerreserva().plusMinutes(10));

    }

    @Transactional
    public void cancelarPrerreserva(Long id, String token) {
        Entrada entrada = this.dao.findById(id).orElse(null);

        if (entrada == null) {
            throw new IllegalArgumentException("No se encontró la entrada con el id especificado.");
        }

        if (!token.equals(entrada.getTokenPrerreserva())) {
            throw new IllegalArgumentException("El token de prerreserva no es válido para la entrada especificada.");
        }

        this.freeTicket(entrada);
        this.dao.save(entrada);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void liberarEntradasCaducadas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(10);

        List<Entrada> caducadas = this.dao.findByEstadoAndFechaPrerreservaBefore(Estado.RESERVADA, limite);

        if (!caducadas.isEmpty()) {
            for (Entrada e : caducadas) {
                this.freeTicket(e);
            }

            this.dao.saveAll(caducadas);
        }
    }

    // COMPRA
    @Transactional
    public CompraResponse comprar(String tokenPrerreserva, String tokenUsuario) {
        String email = this.usuariosClient.validarTokenYObtenerCorreo(tokenUsuario);

        List<Entrada> entradas = this.dao.findByTokenPrerreserva(tokenPrerreserva);
        if (entradas.isEmpty()) {
            throw new IllegalStateException("No existen entradas prerreservadas con ese token");
        }

        entradas.stream().forEach(e -> {
            if (e.getEstado() != Estado.RESERVADA) {
                throw new IllegalStateException("La entrada ya no está reservada.");
            }

            if (e.getFechaPrerreserva() == null ||
                    e.getFechaPrerreserva().plusMinutes(10).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("La sesión de reserva ha expirado.");
            }
        });

        /**
         * AQUI IRÁ LA LÓGICA DE PAGO
         * 
         * CUANDO MACARIO LO EXPLIQUE PROCEDERÉ A IMPLEMENTARLO
         */
        for (Entrada e : entradas) {
            setEntradacomoVendida(e, email);
        }

        this.dao.saveAll(entradas);

        correoService.enviarEntradas(email, entradas);

        return new CompraResponse("Compra realizada con éxito", email);
    }

    // DATA MAPPING
    private List<DtoEntrada> mapToDto(List<Entrada> entradas) {
        return entradas.stream().map(this::toDto).toList();
    }

    private DtoEntrada toDto(Entrada e) {
        DtoEntrada dto;

        if (e instanceof DeZona zona) {
            DtoEntradaDeZona dtoZona = new DtoEntradaDeZona();
            dtoZona.setZona(zona.getZona());
            dtoZona.setTipo("ZONA");
            dto = dtoZona;
        } else if (e instanceof Precisa precisa) {
            DtoEntradaPrecisa dtoPrecisa = new DtoEntradaPrecisa();
            dtoPrecisa.setFila(precisa.getFila());
            dtoPrecisa.setColumna(precisa.getColumna());
            dtoPrecisa.setPlanta(precisa.getPlanta());
            dtoPrecisa.setTipo("PRECISA");
            dto = dtoPrecisa;
        } else {
            throw new IllegalStateException("Tipo de entrada no reconocido");
        }

        dto.setId(e.getId());
        dto.setEspectaculoId(e.getEspectaculo().getId());
        if (e.getPrecio() != null) {
            dto.setPrecio(BigDecimal.valueOf(e.getPrecio(), 2));
        }

        return dto;
    }

    @Transactional
    private void validateToken(String token) {
        List<Entrada> tieneEntradas = this.dao.findByTokenPrerreserva(token);

        if (tieneEntradas.isEmpty()) {
            throw new IllegalArgumentException("El token de prerreserva no es válido.");
        }

        boolean valid = tieneEntradas.stream().anyMatch(e -> e.getEstado() == Estado.RESERVADA &&
                e.getFechaPrerreserva() != null &&
                e.getFechaPrerreserva().plusMinutes(10).isAfter(LocalDateTime.now()));

        if (!valid) {
            throw new IllegalArgumentException(
                    "La sesión de reserva ha expirado. Por favor, seleccione las entradas de nuevo.");
        }
    }

    private void freeTicket(Entrada e) {
        e.setEstado(Estado.DISPONIBLE);
        e.setTokenPrerreserva(null);
        e.setFechaPrerreserva(null);
    }

    private void setEntradacomoVendida(Entrada e, String email) {
        e.setEstado(Estado.VENDIDA);
        e.setCorreoComprador(email);
        e.setTokenPrerreserva(null);
        e.setFechaPrerreserva(null);
    }
}
