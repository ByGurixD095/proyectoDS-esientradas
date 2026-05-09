package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.dto.ColaResponse;

public interface IColaService {
    ColaResponse unirse(Long espectaculoId, String correoUsuario);

    ColaResponse consultarPosicion(Long espectaculoId, String correoUsuario);

    ColaResponse abandonar(Long espectaculoId, String correoUsuario);

    void marcarCompletado(Long espectaculoId, String correoUsuario);

    void activarCola(Long espectaculoId);

    void desactivarCola(Long espectaculoId);

    void expirarTurnosVencidos();
}