package com.ChatRoomQR.BackChatRoomQR.service;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import com.ChatRoomQR.BackChatRoomQR.repository.ChatPrivadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrivateChatCleanupService {

    private final ChatPrivadoRepository chatPrivadoRepository;

    public PrivateChatCleanupService(ChatPrivadoRepository chatPrivadoRepository) {
        this.chatPrivadoRepository = chatPrivadoRepository;
    }

    public int cerrarChatsDeSalaParaUsuario(int idUsuario, String idSalaOrigen, String motivo) {
        if (idSalaOrigen == null || idSalaOrigen.trim().isEmpty()) {
            return 0;
        }

        List<ChatPrivado> mensajes = chatPrivadoRepository.findActivosBySalaAndUsuario(idSalaOrigen, idUsuario);
        String motivoFinal = hasText(motivo) ? motivo : "Chat privado eliminado al finalizar la sesion de sala";

        for (ChatPrivado mensaje : mensajes) {
            mensaje.setEliminado(true);
            mensaje.setMotivoEliminacion(motivoFinal);
            mensaje.setFechaEliminacion(LocalDateTime.now());
            chatPrivadoRepository.save(mensaje);
        }

        return mensajes.size();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
