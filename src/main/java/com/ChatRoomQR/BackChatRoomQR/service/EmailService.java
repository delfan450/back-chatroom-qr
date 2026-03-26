package com.ChatRoomQR.BackChatRoomQR.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(destinatario)
                    .subject("Código de verificación - ChatRoom")
                    .text("Tu código de verificación es: " + codigo +
                            "\n\nEste código expira en 10 minutos.\n\n" +
                            "Si no realizaste esta solicitud, ignora este email.")
                    .build();
            resend.emails().send(request);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }

    public void enviarCodigoReenvio(String destinatario, String codigo) {
        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(destinatario)
                    .subject("Nuevo código de verificación - ChatRoom")
                    .text("Tu nuevo código de verificación es: " + codigo +
                            "\n\nEste código expira en 10 minutos.\n\n" +
                            "Si no realizaste esta solicitud, ignora este email.")
                    .build();
            resend.emails().send(request);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }
}
