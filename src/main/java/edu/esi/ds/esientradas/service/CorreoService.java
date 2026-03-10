package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Precisa;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorreoService {

    private JavaMailSender mailSender;

    public void enviarEntradas(String correo, List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Gracias por tu compra en ESIEntradas!\n\n");
        sb.append("Tus entradas:\n");

        for (Entrada e : entradas) {
            sb.append("\n─────────────────────────────────\n");
            sb.append("Espectáculo : ").append(e.getEspectaculo().getArtista()).append("\n");
            sb.append("Fecha       : ").append(e.getEspectaculo().getFecha()).append("\n");
            sb.append("Recinto     : ").append(e.getEspectaculo().getEscenario().getNombre()).append("\n");
            sb.append("Precio      : ").append(formatPrecio(e.getPrecio())).append("\n");

            if (e instanceof Precisa p) {
                sb.append("Planta      : ").append(p.getPlanta()).append("\n");
                sb.append("Fila        : ").append(p.getFila()).append("\n");
                sb.append("Butaca      : ").append(p.getColumna()).append("\n");
            } else if (e instanceof DeZona dz) {
                sb.append("Zona        : ").append(dz.getZona()).append("\n");
            }
        }

        sb.append("\n─────────────────────────────────\n");
        sb.append("Presenta este correo en la entrada. ¡Disfruta del espectáculo!\n");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(correo);
        msg.setSubject("Tus entradas de ESIEntradas");
        msg.setText(sb.toString());
        mailSender.send(msg);
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format("%.2f €", centimos / 100.0);
    }
}
