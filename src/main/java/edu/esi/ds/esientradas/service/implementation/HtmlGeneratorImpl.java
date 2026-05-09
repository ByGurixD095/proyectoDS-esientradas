package edu.esi.ds.esientradas.service.implementation;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.service.IHtmlGenerator;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HtmlGeneratorImpl implements IHtmlGenerator {

    @Override
    public String generarHtmlEntradas(List<Entrada> entradas) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
        html.append("<h1 style='color: #2c3e50;'>Tus Entradas de ESIEntradas</h1>");
        html.append("<p>Gracias por tu compra. Aquí tienes los detalles de tus pases:</p>");

        html.append(
                "<table border='0' cellpadding='10' cellspacing='0' style='width: 100%; border-collapse: collapse;'>");

        for (int i = 0; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);
            html.append("<tr style='border-bottom: 1px solid #eee;'>");

            // Columna de datos
            html.append("<td>")
                    .append("<strong>Artista:</strong> ").append(e.getEspectaculo().getArtista()).append("<br>")
                    .append("<strong>Fecha:</strong> ").append(e.getEspectaculo().getFecha()).append("<br>")
                    .append("<strong>Ubicación:</strong> ").append(e.getEspectaculo().getEscenario().getNombre())
                    .append("</td>");

            // Columna del QR (Referencia interna al attachment CID)
            html.append("<td style='text-align: right;'>")
                    .append("<img src='cid:img").append(i).append("' width='150' height='150' alt='Código QR'>")
                    .append("</td>");

            html.append("</tr>");
        }

        html.append("</table>");
        html.append("<p style='margin-top: 20px; font-size: 12px; color: #7f8c8d;'>")
                .append("Presenta estos códigos QR en el acceso al recinto.")
                .append("</p>");
        html.append("</body></html>");

        return html.toString();
    }
}