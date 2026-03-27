package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatPrivadoRepository extends JpaRepository<ChatPrivado, Integer> {

    @Query("SELECT c FROM ChatPrivado c WHERE " +
           "(c.idUsuario1 = :u1 AND c.idUsuario2 = :u2) OR " +
           "(c.idUsuario1 = :u2 AND c.idUsuario2 = :u1)")
    Optional<ChatPrivado> findByUsuarios(@Param("u1") int u1, @Param("u2") int u2);
}
