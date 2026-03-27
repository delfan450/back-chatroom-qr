package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_notificacion;

    @Column(name = "id_usuario_receptor")
    private Integer idUsuarioReceptor;

    @Column(name = "id_usuario_remitente")
    private Integer idUsuarioRemitente;

    @Column(name = "tipo_notificacion")
    private String tipoNotificacion; // "mensaje_privado", "mencion"

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private Boolean leida = false;
}
