package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
@Data
public class Denuncia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_denuncia;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "tipo_denuncia")
    private String tipoDenuncia;

    @Column(columnDefinition = "TEXT")
    private String razonDenuncia;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String estado = "pendiente";
}
