package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_mensajes_privados")
@Data
public class MensajePrivado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_emisor")
    private Integer idEmisor;

    @Column(name = "id_receptor")
    private Integer idReceptor;

    private String mensaje;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Transient
    private String nombreEmisor;
}
