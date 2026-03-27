package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_mensajes_grupal")
@Data
public class MensajeGrupal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_mensaje;

    @Column(name = "id_sala")
    private String id_sala;
    private Integer id_usuario;
    private String mensaje;
    private LocalDateTime fecha_hora = LocalDateTime.now();

    // Campo transaccional para el nombre del emisor (no está en la tabla, pero Android lo necesita)
    @Transient
    private String nombre;

    // Campo transaccional para el nombre de usuario del emisor
    @Transient
    private String nombre_usuario;
}