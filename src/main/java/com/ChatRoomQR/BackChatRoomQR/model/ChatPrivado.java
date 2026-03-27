package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chats_privados")
@Data
public class ChatPrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_chat_privado;

    @Column(name = "id_usuario_1")
    private Integer idUsuario1;

    @Column(name = "id_usuario_2")
    private Integer idUsuario2;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
