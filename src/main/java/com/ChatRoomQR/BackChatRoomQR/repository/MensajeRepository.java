package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.MensajeGrupal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<MensajeGrupal, Integer> {

    // Usamos una consulta manual para evitar líos con los guiones bajos
    @Query("SELECT m FROM MensajeGrupal m WHERE m.id_sala = :idSala ORDER BY m.fecha_hora ASC")
    List<MensajeGrupal> obtenerMensajesPorSala(@Param("idSala") String idSala);
}