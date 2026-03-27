package com.ChatRoomQR.BackChatRoomQR.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_mensajes_privados")
@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class MensajePrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Android espera "id_usuario" para que MensajeAdapter distinga burbujas
    @JsonProperty("id_usuario")
    @Column(name = "id_emisor")
    private Integer idEmisor;

    @Column(name = "id_receptor")
    private Integer idReceptor;

    private String mensaje;

    // Android espera "fecha_hora"
    @JsonProperty("fecha_hora")
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();

    // Android espera "nombre"
    @Transient
    private String nombre;
}
