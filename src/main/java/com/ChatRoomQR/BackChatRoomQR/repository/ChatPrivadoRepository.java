package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatPrivadoRepository extends JpaRepository<ChatPrivado, Integer> {

    @Query("SELECT c FROM ChatPrivado c WHERE " +
           "(c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1) " +
           "ORDER BY c.fechaHora ASC")
    List<ChatPrivado> findMensajes(@Param("u1") int u1, @Param("u2") int u2);

    @Query("SELECT c FROM ChatPrivado c WHERE c.idReceptor = :idUsuario AND c.leida = false ORDER BY c.fechaHora DESC")
    List<ChatPrivado> findNoLeidos(@Param("idUsuario") int idUsuario);
}
