package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_sala")
@Data
public class UsuarioSala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_sala")
    private String idSala;

    private String estado;

    @Column(name = "fecha_union")
    private LocalDateTime fechaUnion;
}
