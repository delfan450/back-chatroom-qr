package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.MensajePrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Integer> {

    @Query("SELECT m FROM MensajePrivado m WHERE m.idChatPrivado = :idChat ORDER BY m.fechaHora ASC")
    List<MensajePrivado> findByChat(@Param("idChat") int idChatPrivado);
}
