package com.ChatRoomQR.BackChatRoomQR.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    @Value("${SENDGRID_FROM_EMAIL}")
    private String fromEmail;

    private void enviarEmail(String destinatario, String asunto, String cuerpo) {
        System.out.println("[EmailService] Enviando email a: " + destinatario + " | Asunto: " + asunto);
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(destinatario);
            Content content = new Content("text/plain", cuerpo);
            Mail mail = new Mail(from, asunto, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("[EmailService] SendGrid status: " + response.getStatusCode());
            System.out.println("[EmailService] SendGrid body: " + response.getBody());

            if (response.getStatusCode() == 202) {
                System.out.println("[EmailService] Email enviado correctamente a: " + destinatario);
            } else {
                System.err.println("[EmailService] Error. Status: " + response.getStatusCode() + " | Body: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("[EmailService] Excepcion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        enviarEmail(
            destinatario,
            "Código de verificación - ChatRoom",
            "Tu código de verificación es: " + codigo + "\n\nEste código expira en 10 minutos."
        );
    }

    public void enviarCodigoReenvio(String destinatario, String codigo) {
        enviarEmail(
            destinatario,
            "Nuevo código de verificación - ChatRoom",
            "Tu nuevo código de verificación es: " + codigo + "\n\nEste código expira en 10 minutos."
        );
    }
}
