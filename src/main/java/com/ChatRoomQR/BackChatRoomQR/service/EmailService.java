package com.ChatRoomQR.BackChatRoomQR.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoVerificacion(String email, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("noreply@chatroom-qr.com");
            mensaje.setTo(email);
            mensaje.setSubject("Código de verificación - ChatRoom QR");
            mensaje.setText("Tu código de verificación es: " + codigo + "\n\n" +
                    "Este código es válido durante 10 minutos.\n\n" +
                    "Si no realizaste esta solicitud, ignora este email.");
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }

    public void enviarCodigoReenvio(String email, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("noreply@chatroom-qr.com");
            mensaje.setTo(email);
            mensaje.setSubject("Nuevo código de verificación - ChatRoom QR");
            mensaje.setText("Tu nuevo código de verificación es: " + codigo + "\n\n" +
                    "Este código es válido durante 10 minutos.\n\n" +
                    "Si no realizaste esta solicitud, ignora este email.");
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }
}
