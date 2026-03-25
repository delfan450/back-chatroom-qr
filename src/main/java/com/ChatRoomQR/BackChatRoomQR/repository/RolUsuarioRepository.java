package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {
    Optional<RolUsuario> findByIdUsuario(Integer idUsuario);

    @Query(value = "SELECT ro.nombre FROM roles_usuarios ru JOIN roles ro ON ro.id = ru.id_rol WHERE ru.id_usuario = :idUsuario", nativeQuery = true)
    Optional<String> findRolNameByIdUsuario(@Param("idUsuario") Integer idUsuario);
}
