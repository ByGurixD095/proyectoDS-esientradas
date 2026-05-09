package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.dto.*;
import java.util.List;

public interface IEntradaService {
    List<DtoEntrada> getEntradasByEspectaculoId(Long id);

    int getNumeroEntradas(Long espectaculoId);

    DtoEntradaInfo getInfoEntradas(Long idEspectaculo);

    DtoEntrada getEntradaById(Long id);

    ReservaResponse prerreservar(Long id, String token);

    void cancelarPrerreserva(Long id, String token);

    void liberarEntradasCaducadas();

    String canBuy(String tokenPrerreserva, String tokenUsuario);

    void confirmarCompra(String tokenPrerreserva, String email);

    List<DtoEntradaComprada> getEntradasCompradasByEmail(String email);
}