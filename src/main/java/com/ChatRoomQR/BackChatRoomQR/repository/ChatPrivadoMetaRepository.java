package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatPrivadoMetaRepository extends JpaRepository<ChatPrivadoMeta, Integer> {
    Optional<ChatPrivadoMeta> findByIdUsuarioMenorAndIdUsuarioMayor(int idUsuarioMenor, int idUsuarioMayor);
}
