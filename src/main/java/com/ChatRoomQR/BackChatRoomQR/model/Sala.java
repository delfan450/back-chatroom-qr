package com.ChatRoomQR.BackChatRoomQR.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "salas")
@Data
public class Sala {
    @Id
    private String id_sala;

    @JsonProperty("nombre")
    private String nombre_sala;

    private String descripcion;
    private Double latitud;
    private Double longitud;
    private Double radio_metros;
    private Integer tiempo_maximo;
    private Integer tiempo_pendiente;
}
