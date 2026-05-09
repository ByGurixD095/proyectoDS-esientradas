package edu.esi.ds.esientradas.service.implementation;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.service.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CorreoService implements ICorreoService {

    private final IEmailSender emailSender;
    private final IQrService qrService;
    private final IHtmlGenerator htmlGenerator;

    public CorreoService(IEmailSender emailSender, IQrService qrService, IHtmlGenerator htmlGenerator) {
        this.emailSender = emailSender;
        this.qrService = qrService;
        this.htmlGenerator = htmlGenerator;
    }

    @Override
    public void enviarEntradas(String correo, List<Entrada> entradas) {
        List<byte[]> qrImages = generarQRs(entradas);
        String html = htmlGenerator.generarHtmlEntradas(entradas);

        emailSender.enviar(correo, "Tus entradas de ESIEntradas", html, qrImages);
    }

    private List<byte[]> generarQRs(List<Entrada> entradas) {
        return entradas.stream()
                .map(e -> qrService.generar("ENTRADA_ID:" + e.getId()))
                .toList();
    }
}