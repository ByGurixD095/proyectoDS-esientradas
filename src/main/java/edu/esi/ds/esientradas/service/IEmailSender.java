package edu.esi.ds.esientradas.service;

import java.util.List;

public interface IEmailSender {
    void enviar(String to, String subject, String body, List<byte[]> attachments);
}