package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatPrivadoRepository extends JpaRepository<ChatPrivado, Integer> {

    @Query("SELECT c FROM ChatPrivado c WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1)) " +
           "AND (c.esMeta = false OR c.esMeta IS NULL) " +
           "ORDER BY c.fechaHora ASC")
    List<ChatPrivado> findMensajes(@Param("u1") int u1, @Param("u2") int u2);

    @Query("SELECT c FROM ChatPrivado c WHERE c.idReceptor = :idUsuario AND c.leida = false AND (c.esMeta = false OR c.esMeta IS NULL) ORDER BY c.fechaHora DESC")
    List<ChatPrivado> findNoLeidos(@Param("idUsuario") int idUsuario);

    @Query("SELECT c FROM ChatPrivado c WHERE c.esMeta = true AND c.idUsuarioMenor = :idUsuarioMenor AND c.idUsuarioMayor = :idUsuarioMayor")
    java.util.Optional<ChatPrivado> findMetaByPair(@Param("idUsuarioMenor") int idUsuarioMenor, @Param("idUsuarioMayor") int idUsuarioMayor);

    @Query("SELECT c FROM ChatPrivado c WHERE c.esMeta = true AND c.idSalaOrigen = :idSalaOrigen AND (c.idUsuarioMenor = :idUsuario OR c.idUsuarioMayor = :idUsuario) AND (c.eliminado = false OR c.eliminado IS NULL)")
    List<ChatPrivado> findMetasActivasBySalaAndUsuario(@Param("idSalaOrigen") String idSalaOrigen, @Param("idUsuario") int idUsuario);

    @Query("SELECT c FROM ChatPrivado c WHERE c.esMeta = true AND (c.idUsuarioMenor = :idUsuario OR c.idUsuarioMayor = :idUsuario) AND (c.eliminado = false OR c.eliminado IS NULL)")
    List<ChatPrivado> findMetasActivasByUsuario(@Param("idUsuario") int idUsuario);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatPrivado c WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1)) " +
           "AND (c.esMeta = false OR c.esMeta IS NULL)")
    void deleteMensajesEntre(@Param("u1") int u1, @Param("u2") int u2);

    @Query("""
           SELECT c FROM ChatPrivado c
           WHERE c.idReceptor = :idUsuario
             AND c.leida = false
             AND (c.esMeta = false OR c.esMeta IS NULL)
             AND c.fechaHora = (
                 SELECT MAX(c2.fechaHora)
                 FROM ChatPrivado c2
                 WHERE c2.idEmisor = c.idEmisor
                   AND c2.idReceptor = :idUsuario
                   AND (c2.esMeta = false OR c2.esMeta IS NULL)
                   AND c2.leida = false
             )
           ORDER BY c.fechaHora DESC
           """)
    List<ChatPrivado> findResumenNoLeidos(@Param("idUsuario") int idUsuario);
}
