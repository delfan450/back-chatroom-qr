package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "chats_privados_meta",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario_menor", "id_usuario_mayor"})
)
@Data
public class ChatPrivadoMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_usuario_menor", nullable = false)
    private Integer idUsuarioMenor;

    @Column(name = "id_usuario_mayor", nullable = false)
    private Integer idUsuarioMayor;

    @Column(name = "id_sala_origen")
    private String idSalaOrigen;

    private Boolean eliminado = false;

    @Column(name = "motivo_eliminacion")
    private String motivoEliminacion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;
}
