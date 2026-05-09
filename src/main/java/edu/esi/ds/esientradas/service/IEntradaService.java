package edu.esi.ds.esientradas.service;

import java.util.List;
import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaComprada;
import edu.esi.ds.esientradas.dto.DtoEntradaInfo;
import edu.esi.ds.esientradas.dto.ReservaResponse;
import edu.esi.ds.esientradas.model.Entrada;

public interface IEntradaService {

    // Consultas
    List<DtoEntrada> getEntradasByEspectaculoId(Long id);

    int getNumeroEntradas(Long espectaculoId);

    DtoEntradaInfo getInfoEntradas(Long idEspectaculo);

    DtoEntrada getEntradaById(Long id);

    List<DtoEntradaComprada> getEntradasCompradasByEmail(String email);

    // Gestión de Prerreserva
    ReservaResponse prerreservar(Long id, String token);

    void cancelarPrerreserva(Long id, String token);

    void liberarEntradasCaducadas();

    // Lógica de Compra (Persistencia y Estado)
    String canBuy(String tokenPrerreserva, String tokenUsuario);

    List<Entrada> obtenerReservadasPorToken(String tokenPrerreserva);

    List<Entrada> consolidarVenta(String tokenPrerreserva, String email);
}