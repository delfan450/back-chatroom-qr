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

        List<ChatPrivado> metas = chatPrivadoRepository.findMetasActivasBySalaAndUsuario(idSalaOrigen, idUsuario);
        String motivoFinal = hasText(motivo) ? motivo : "Chat privado eliminado al finalizar la sesion de sala";

        for (ChatPrivado meta : metas) {
            meta.setEliminado(true);
            meta.setMotivoEliminacion(motivoFinal);
            meta.setFechaEliminacion(LocalDateTime.now());
            chatPrivadoRepository.save(meta);

            Integer otherUserId = meta.getIdUsuarioMenor().equals(idUsuario)
                    ? meta.getIdUsuarioMayor()
                    : meta.getIdUsuarioMenor();
            chatPrivadoRepository.deleteMensajesEntre(idUsuario, otherUserId);
        }

        return metas.size();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
