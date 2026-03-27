package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @Query("SELECT n FROM Notificacion n WHERE n.idUsuarioReceptor = :idUsuario ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findByIdUsuarioReceptor(@Param("idUsuario") int idUsuario);

    @Query("SELECT n FROM Notificacion n WHERE n.idUsuarioReceptor = :idUsuario AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotLeidas(@Param("idUsuario") int idUsuario);
}
