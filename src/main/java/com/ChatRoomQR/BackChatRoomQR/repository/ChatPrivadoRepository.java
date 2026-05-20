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

    //Querys para Chats Privados

    @Query("SELECT c FROM ChatPrivado c WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1)) " +
           "ORDER BY c.fechaHora ASC")
    List<ChatPrivado> findMensajes(@Param("u1") int u1, @Param("u2") int u2);

    @Query("SELECT c FROM ChatPrivado c WHERE c.idReceptor = :idUsuario AND c.leida = false ORDER BY c.fechaHora DESC")
    List<ChatPrivado> findNoLeidos(@Param("idUsuario") int idUsuario);

    @Query("SELECT c FROM ChatPrivado c WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1)) " +
           "ORDER BY c.fechaHora DESC")
    List<ChatPrivado> findConversacion(@Param("u1") int u1, @Param("u2") int u2);

    @Query("SELECT c FROM ChatPrivado c WHERE c.idSalaOrigen = :idSalaOrigen AND (c.idEmisor = :idUsuario OR c.idReceptor = :idUsuario) AND (c.eliminado = false OR c.eliminado IS NULL)")
    List<ChatPrivado> findActivosBySalaAndUsuario(@Param("idSalaOrigen") String idSalaOrigen, @Param("idUsuario") int idUsuario);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatPrivado c WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1))")
    void deleteMensajesEntre(@Param("u1") int u1, @Param("u2") int u2);

    @Modifying
    @Transactional
    @Query("UPDATE ChatPrivado c SET c.estado = :estado WHERE " +
           "((c.idEmisor = :u1 AND c.idReceptor = :u2) OR " +
           "(c.idEmisor = :u2 AND c.idReceptor = :u1))")
    int updateEstadoConversacion(@Param("u1") int u1, @Param("u2") int u2, @Param("estado") String estado);

    @Query("""
           SELECT c FROM ChatPrivado c
           WHERE c.idReceptor = :idUsuario
             AND c.leida = false
             AND c.fechaHora = (
                 SELECT MAX(c2.fechaHora)
                 FROM ChatPrivado c2
                 WHERE c2.idEmisor = c.idEmisor
                   AND c2.idReceptor = :idUsuario
                   AND c2.leida = false
             )
           ORDER BY c.fechaHora DESC
           """)
    List<ChatPrivado> findResumenNoLeidos(@Param("idUsuario") int idUsuario);

}
