package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.MensajePrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Integer> {

    @Query("SELECT m FROM MensajePrivado m WHERE " +
           "(m.idEmisor = :a AND m.idReceptor = :b) OR " +
           "(m.idEmisor = :b AND m.idReceptor = :a) " +
           "ORDER BY m.fechaHora ASC")
    List<MensajePrivado> findConversacion(@Param("a") int idUsuario1, @Param("b") int idUsuario2);
}
