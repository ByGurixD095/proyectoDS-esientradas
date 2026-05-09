package edu.esi.ds.esientradas.service.implementation;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class GmailService {

    @Value("${correo.username}")
    private String username;

    @Value("${correo.password}")
    private String appPassword;

    public void enviar(String to, String subject, String htmlContent, List<byte[]> imagenes) {
        if (username == null || username.isBlank() ||
                appPassword == null || appPassword.isBlank()) {
            System.err.println("[EmailSenderService] Credenciales no configuradas — correo no enviado a: " + to);
            return;
        }

        try {
            Session session = crearSesion();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "ESIEntradas", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");

            MimeMultipart multipart = new MimeMultipart("related");

            // Parte HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            for (int i = 0; i < imagenes.size(); i++) {
                byte[] imgBytes = imagenes.get(i);
                if (imgBytes == null || imgBytes.length == 0)
                    continue;

                MimeBodyPart imgPart = new MimeBodyPart();
                imgPart.setDataHandler(new DataHandler(
                        new ByteArrayDataSource(imgBytes, "image/png")));
                imgPart.setHeader("Content-ID", "<img" + i + ">");
                imgPart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(imgPart);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("[EmailSenderService] Correo enviado correctamente a: " + to);

        } catch (AuthenticationFailedException e) {
            System.err.println("[EmailSenderService] Error de autenticacion Gmail: " + e.getMessage());

        } catch (MessagingException e) {
            System.err.println("[EmailSenderService] Error al enviar correo a " + to + ": " + e.getMessage());

        } catch (Exception e) {
            System.err.println("[EmailSenderService] Error inesperado: " + e.getMessage());
        }
    }

    private Session crearSesion() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });
    }
}