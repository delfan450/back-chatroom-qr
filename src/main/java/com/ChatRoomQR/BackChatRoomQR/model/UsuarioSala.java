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
    @Column(name = "motivo_expulsion")
    private String motivoExpulsion;
    @Column(name = "duracion_expulsion")
    private Integer duracionExpulsion;
    @Column(name = "fecha_expulsion")
    private LocalDateTime fechaExpulsion;
    @Column(name = "ultima_latitud")
    private Double ultimaLatitud;
    @Column(name = "ultima_longitud")
    private Double ultimaLongitud;
    @Column(name = "ultima_ubicacion_at")
    private LocalDateTime ultimaUbicacionAt;
}
