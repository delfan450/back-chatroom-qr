package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivadoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatPrivadoMetaRepository extends JpaRepository<ChatPrivadoMeta, Integer> {
    Optional<ChatPrivadoMeta> findByIdUsuarioMenorAndIdUsuarioMayor(int idUsuarioMenor, int idUsuarioMayor);

    @Query("SELECT m FROM ChatPrivadoMeta m WHERE m.idSalaOrigen = :idSalaOrigen AND (m.idUsuarioMenor = :idUsuario OR m.idUsuarioMayor = :idUsuario) AND (m.eliminado = false OR m.eliminado IS NULL)")
    List<ChatPrivadoMeta> findActivosBySalaAndUsuario(@Param("idSalaOrigen") String idSalaOrigen, @Param("idUsuario") int idUsuario);
}

