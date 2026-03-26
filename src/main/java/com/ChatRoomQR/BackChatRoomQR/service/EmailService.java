package com.ChatRoomQR.BackChatRoomQR.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    private void enviarEmail(String destinatario, String asunto, String cuerpo) {
        System.out.println("[EmailService] Enviando email a: " + destinatario + " | Asunto: " + asunto);
        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from("noreply@chatroom.cv")
                    .to(destinatario)
                    .subject(asunto)
                    .text(cuerpo)
                    .build();

            CreateEmailResponse response = resend.emails().send(request);

            System.out.println("[EmailService] Resend response id: " + response.getId());
            System.out.println("[EmailService] Email enviado correctamente a: " + destinatario);
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
