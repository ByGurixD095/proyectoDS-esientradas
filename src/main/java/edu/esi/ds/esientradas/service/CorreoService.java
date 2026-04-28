package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Precisa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CorreoService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarEntradas(String correo, List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Gracias por tu compra en ESIEntradas!\n\n");
        sb.append("Tus entradas:\n");

        for (Entrada e : entradas) {
            sb.append("\n─────────────────────────────────\n");
            sb.append("Espectáculo : ").append(e.getEspectaculo().getArtista()).append("\n");
            sb.append("Fecha : ").append(e.getEspectaculo().getFecha()).append("\n");
            sb.append("Recinto : ").append(e.getEspectaculo().getEscenario().getNombre()).append("\n");
            sb.append("Precio : ").append(formatPrecio(e.getPrecio())).append("\n");

            if (e instanceof Precisa p) {
                sb.append("Planta : ").append(p.getPlanta()).append("\n");
                sb.append("Fila : ").append(p.getFila()).append("\n");
                sb.append("Butaca : ").append(p.getColumna()).append("\n");
            } else if (e instanceof DeZona dz) {
                sb.append("Zona : ").append(dz.getZona()).append("\n");
            }
        }

        sb.append("\n─────────────────────────────────\n");
        sb.append("Presenta este correo en la entrada. ¡Disfruta del espectáculo!\n");

        enviarConBrevo(correo, "Tus entradas de ESIEntradas", sb.toString());
    }

    private void enviarConBrevo(String destinatario, String asunto, String contenido) {
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "ESIEntradas", "email", "info@esientradas.com"));
        body.put("to", List.of(Map.of("email", destinatario)));
        body.put("subject", asunto);
        body.put("textContent", contenido);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(url, request, String.class);
            System.out.println(
                    "Correo enviado a " + destinatario + " mediante Brevo con " + contenido.length() + " caracteres.");
        } catch (Exception e) {
            System.err.println("Error al enviar el correo con Brevo: " + e.getMessage());
        }
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format("%.2f €", centimos / 100.0);
    }
}