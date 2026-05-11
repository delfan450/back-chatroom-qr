package com.ChatRoomQR.BackChatRoomQR.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chats_privados")
@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ChatPrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Android espera "id_usuario" para distinguir burbujas
    @JsonProperty("id_usuario")
    @Column(name = "id_emisor")
    private Integer idEmisor;

    @Column(name = "id_receptor")
    private Integer idReceptor;

    @Column(name = "id_usuario_menor")
    private Integer idUsuarioMenor;

    @Column(name = "id_usuario_mayor")
    private Integer idUsuarioMayor;

    @Column(name = "id_sala_origen")
    private String idSalaOrigen;

    @Column(name = "es_meta")
    private Boolean esMeta = false;

    private Boolean eliminado = false;

    @Column(name = "motivo_eliminacion")
    private String motivoEliminacion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    private String mensaje;

    @JsonProperty("fecha_hora")
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();

    private Boolean leida = false;

    @Transient
    private String nombre;

    @Transient
    private String nombre_usuario;
}
