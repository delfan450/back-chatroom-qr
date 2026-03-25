package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.UsuarioSala;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioSalaRepository extends JpaRepository<UsuarioSala, Integer> {
    List<UsuarioSala> findByIdUsuario(Integer idUsuario);
    Optional<UsuarioSala> findByIdUsuarioAndIdSala(Integer idUsuario, String idSala);
}
