package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "salas")
@Data
public class Sala {
    @Id
    private String id_sala; // El código del QR (ej: "GENERAL")
    private String nombre_sala;
    private String descripcion;
}