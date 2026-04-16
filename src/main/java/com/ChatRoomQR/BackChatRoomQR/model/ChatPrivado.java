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
