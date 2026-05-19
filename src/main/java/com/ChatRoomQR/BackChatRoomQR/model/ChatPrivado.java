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
    @JsonProperty("id")
    @Column(name = "id_chat_privado")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChatPrivado;

    // Android espera "id_usuario" para distinguir burbujas
    @JsonProperty("id_usuario")
    @Column(name = "id_emisor")
    private Integer idEmisor;

    @Column(name = "id_receptor")
    private Integer idReceptor;

    @Column(name = "id_sala_origen")
    private String idSalaOrigen;

    private Boolean eliminado = false;

    @Column(name = "motivo_eliminacion")
    private String motivoEliminacion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    private String estado = "PENDIENTE";

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
