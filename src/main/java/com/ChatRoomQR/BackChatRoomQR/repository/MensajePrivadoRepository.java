package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.MensajePrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Integer> {

    @Query("SELECT m FROM MensajePrivado m WHERE " +
           "(m.idEmisor = :u1 AND m.idReceptor = :u2) OR " +
           "(m.idEmisor = :u2 AND m.idReceptor = :u1) " +
           "ORDER BY m.fechaHora ASC")
    List<MensajePrivado> findByUsuarios(@Param("u1") int u1, @Param("u2") int u2);
}
