package edu.esi.ds.esientradas.service.implementation;

import edu.esi.ds.esientradas.service.IEmailSender;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Properties;

@Service
public class GmailService implements IEmailSender {

    private final String username;
    private final String appPassword;

    public GmailService(@Value("${correo.username}") String username,
            @Value("${correo.password}") String appPassword) {
        this.username = username;
        this.appPassword = appPassword;
    }

    @Override
    public void enviar(String to, String subject, String htmlContent, List<byte[]> imagenes) {
        // [Inference] Lógica técnica de JavaMail trasladada desde el servicio original
        try {
            Session session = crearSesion();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "ESIEntradas", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");

            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            for (int i = 0; i < imagenes.size(); i++) {
                MimeBodyPart imgPart = new MimeBodyPart();
                imgPart.setDataHandler(new DataHandler(new ByteArrayDataSource(imagenes.get(i), "image/png")));
                imgPart.setHeader("Content-ID", "<img" + i + ">");
                imgPart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(imgPart);
            }

            message.setContent(multipart);
            Transport.send(message);
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
        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });
    }
}