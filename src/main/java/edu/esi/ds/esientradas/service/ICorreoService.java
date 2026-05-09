package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.Entrada;
import java.util.List;

public interface ICorreoService {
    void enviarEntradas(String correo, List<Entrada> entradas);

    void enviar(String to, String subject, String htmlContent, List<byte[]> imagenes);
}