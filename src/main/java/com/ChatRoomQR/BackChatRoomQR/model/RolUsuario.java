package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles_usuarios")
@Data
public class RolUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_rol")
    private Integer idRol;
}
