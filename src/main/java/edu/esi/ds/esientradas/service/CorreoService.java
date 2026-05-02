package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Precisa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CorreoService {

    @Autowired
    private GmailService emailSender;

    @Autowired
    private QRService qrService;

    public void enviarEntradas(String correo, List<Entrada> entradas) {
        List<byte[]> qrImages = generarQRs(entradas);
        String html = construirHtml(entradas);
        emailSender.enviar(correo, "Tus entradas de ESIEntradas", html, qrImages);
    }

    private List<byte[]> generarQRs(List<Entrada> entradas) {
        List<byte[]> result = new ArrayList<>();
        for (Entrada e : entradas) {
            String contenido = "ENTRADA_ID:" + e.getId() + '\n' +
                    "Espectáculo: " + e.getEspectaculo().getArtista();
            result.add(qrService.generar(contenido));
        }
        return result;
    }

    private String construirHtml(List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family:Helvetica Neue,Arial,sans-serif;max-width:600px;margin:0 auto;");
        sb.append("background:#f5f5f7;padding:32px;border-radius:12px;'>");

        sb.append("<div style='background:#000;border-radius:8px;padding:24px;margin-bottom:24px;text-align:center;'>");
        sb.append("<h1 style='color:#fff;font-size:24px;margin:0;'>esi<strong>entradas</strong></h1>");
        sb.append("</div>");

        sb.append("<h2 style='color:#1d1d1f;font-size:20px;margin:0 0 8px;'>Gracias por tu compra</h2>");
        sb.append("<p style='color:#6e6e73;font-size:15px;margin:0 0 24px;'>Aqui tienes tus entradas:</p>");

        for (int i = 0; i < entradas.size(); i++) {
            sb.append(construirEntradaHtml(entradas.get(i), i));
        }

        long total = entradas.stream()
                .mapToLong(e -> e.getPrecio() != null ? e.getPrecio() : 0L)
                .sum();

        sb.append("<div style='background:#fff;border-radius:10px;padding:16px 20px;margin-top:8px;");
        sb.append("border:1px solid rgba(0,0,0,0.08);'>");
        sb.append("<span style='font-size:15px;color:#6e6e73;'>Total pagado: </span>");
        sb.append("<span style='font-size:20px;font-weight:600;color:#1d1d1f;'>")
                .append(formatPrecio(total)).append("</span>");
        sb.append("</div>");

        sb.append("<p style='color:#6e6e73;font-size:13px;text-align:center;margin-top:24px;'>");
        sb.append("Presenta este correo en la entrada. Disfruta del espectaculo!</p>");
        sb.append("</div></body></html>");

        return sb.toString();
    }

    private String construirEntradaHtml(Entrada e, int index) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style='background:#fff;border-radius:10px;padding:20px;margin-bottom:12px;");
        sb.append("border:1px solid rgba(0,0,0,0.08);'>");

        sb.append("<div style='display:flex;justify-content:space-between;align-items:center;");
        sb.append("border-bottom:1px solid rgba(0,0,0,0.06);padding-bottom:12px;margin-bottom:12px;'>");
        sb.append("<span style='font-size:17px;font-weight:600;color:#1d1d1f;'>")
                .append(e.getEspectaculo().getArtista()).append("</span>");
        sb.append("<span style='font-size:17px;font-weight:600;color:#0071e3;'>")
                .append(formatPrecio(e.getPrecio())).append("</span>");
        sb.append("</div>");

        sb.append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>");
        fila(sb, "Fecha", e.getEspectaculo().getFecha().toString().replace("T", " "));
        fila(sb, "Recinto", e.getEspectaculo().getEscenario().getNombre());

        if (e instanceof Precisa p) {
            fila(sb, "Planta", String.valueOf(p.getPlanta()));
            fila(sb, "Fila", String.valueOf(p.getFila()));
            fila(sb, "Butaca", String.valueOf(p.getColumna()));
        } else if (e instanceof DeZona dz) {
            fila(sb, "Zona", String.valueOf(dz.getZona()));
        }

        sb.append("</table>");

        sb.append("<div style='text-align:center;margin-top:16px;'>");
        sb.append("<img src='cid:img").append(index)
                .append("' width='150' height='150' alt='QR entrada'/>");
        sb.append("<p style='font-size:12px;color:#6e6e73;margin:4px 0 0;'>")
                .append("Escanea este codigo en la entrada</p>");
        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }
    
    private void fila(StringBuilder sb, String label, String valor) {
        sb.append("<tr>")
                .append("<td style='color:#6e6e73;padding:4px 0;width:80px;'>").append(label).append("</td>")
                .append("<td style='color:#1d1d1f;padding:4px 0;font-weight:500;'>").append(valor).append("</td>")
                .append("</tr>");
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format(" %.2f EUR", centimos / 100.0);
    }
}