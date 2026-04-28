package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Precisa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CorreoService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate; // ← inyectado, no instanciado a mano

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void enviarEntradas(String correo, List<Entrada> entradas) {
        String contenido = construirContenido(entradas);
        enviarConBrevo(correo, "Tus entradas de ESIEntradas", contenido);
    }

    private String construirContenido(List<Entrada> entradas) {
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
        return sb.toString();
    }

    private void enviarConBrevo(String destinatario, String asunto, String contenido) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> sender = new HashMap<>();
        sender.put("name", "ESIEntradas");
        sender.put("email", "gonzalo.lopez16@alu.uclm.es");

        Map<String, String> destinatarioMap = new HashMap<>();
        destinatarioMap.put("email", destinatario);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(destinatarioMap));
        body.put("subject", asunto);
        body.put("textContent", contenido);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Correo enviado correctamente a " + destinatario);
            } else {
                System.err.println("Brevo devolvió estado inesperado: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            // 4xx: API key mal, payload incorrecto, etc.
            System.err.println("Error del cliente al enviar correo con Brevo ["
                    + e.getStatusCode() + "]: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            // 5xx: Brevo caído
            System.err.println("Error del servidor Brevo ["
                    + e.getStatusCode() + "]: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error inesperado enviando correo: " + e.getMessage());
        }
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format("%.2f €", centimos / 100.0);
    }
}