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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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

        sb.append("<h2>¡Gracias por tu compra en ESIEntradas!</h2>");
        sb.append("<p>Tus entradas:</p>");

        for (Entrada e : entradas) {

            String contenidoQR = "ENTRADA_ID:" + e.getId();
            String qrBase64 = generarQRBase64(contenidoQR);

            sb.append("<div style='border:1px solid #ccc;padding:15px;margin-bottom:20px;'>");

            sb.append("<p><b>Espectáculo:</b> ").append(e.getEspectaculo().getArtista()).append("</p>");
            sb.append("<p><b>Fecha:</b> ").append(e.getEspectaculo().getFecha()).append("</p>");
            sb.append("<p><b>Recinto:</b> ").append(e.getEspectaculo().getEscenario().getNombre()).append("</p>");
            sb.append("<p><b>Precio:</b> ").append(formatPrecio(e.getPrecio())).append("</p>");

            if (e instanceof Precisa p) {
                sb.append("<p><b>Planta:</b> ").append(p.getPlanta()).append("</p>");
                sb.append("<p><b>Fila:</b> ").append(p.getFila()).append("</p>");
                sb.append("<p><b>Butaca:</b> ").append(p.getColumna()).append("</p>");
            } else if (e instanceof DeZona dz) {
                sb.append("<p><b>Zona:</b> ").append(dz.getZona()).append("</p>");
            }

            sb.append("<div style='text-align:center;margin-top:10px;'>");
            sb.append("<img src='data:image/png;base64,")
                    .append(qrBase64)
                    .append("' width='150' height='150'/>");
            sb.append("<p>Escanea este código en la entrada</p>");
            sb.append("</div>");

            sb.append("</div>");
        }

        sb.append("<p>Presenta este correo en la entrada. ¡Disfruta del espectáculo!</p>");

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
        body.put("htmlContent", contenido);

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

    private String generarQRBase64(String contenido) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix matrix = qrWriter.encode(contenido, BarcodeFormat.QR_CODE, 200, 200);

            BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            return "";
        }
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format("%.2f €", centimos / 100.0);
    }
}