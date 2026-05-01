package edu.esi.ds.esientradas.service;

import edu.esi.ds.esientradas.model.DeZona;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Precisa;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class CorreoService {

    @Value("${correo.username}")
    private String username;

    @Value("${correo.password}")
    private String appPassword;

    public void enviarEntradas(String correo, List<Entrada> entradas) {
        try {
            // Generamos los bytes de cada QR en orden
            List<byte[]> qrImages = new ArrayList<>();
            for (Entrada e : entradas) {
                qrImages.add(generarQRBytes("ENTRADA_ID:" + e.getId()));
            }

            String html = construirContenido(entradas);
            sendHtmlEmail(correo, "Tus entradas de ESIEntradas", html, qrImages);
            System.out.println("Correo enviado correctamente a " + correo);

        } catch (Exception e) {
            System.err.println("Error al enviar correo a " + correo + ": " + e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent,
            List<byte[]> qrImages) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        MimeMultipart multipart = new MimeMultipart("related");

        // Parte HTML
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        // Un adjunto CID por cada QR
        for (int i = 0; i < qrImages.size(); i++) {
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setDataHandler(
                    new DataHandler(new ByteArrayDataSource(qrImages.get(i), "image/png")));
            imagePart.setHeader("Content-ID", "<qr" + i + ">");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    private String construirContenido(List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>¡Gracias por tu compra en ESIEntradas!</h2>");
        sb.append("<p>Tus entradas:</p>");

        for (int i = 0; i < entradas.size(); i++) {
            Entrada e = entradas.get(i);

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

            // Referencia al CID del QR adjunto
            sb.append("<div style='text-align:center;margin-top:10px;'>");
            sb.append("<img src='cid:qr").append(i)
                    .append("' width='150' height='150' alt='QR entrada'/>");
            sb.append("<p>Escanea este código en la entrada</p>");
            sb.append("</div>");

            sb.append("</div>");
        }

        sb.append("<p>Presenta este correo en la entrada. ¡Disfruta del espectáculo!</p>");
        return sb.toString();
    }

    // Ahora devuelve bytes directamente, no Base64
    private byte[] generarQRBytes(String contenido) {
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
            return baos.toByteArray();

        } catch (Exception e) {
            System.err.println("Error generando QR: " + e.getMessage());
            return new byte[0];
        }
    }

    private String formatPrecio(Long centimos) {
        if (centimos == null)
            return "N/D";
        return String.format("%.2f €", centimos / 100.0);
    }
}