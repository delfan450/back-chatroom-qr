package com.ChatRoomQR.BackChatRoomQR.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data // Esto de Lombok crea los Getters y Setters automáticamente
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    private String nombre;
    private String apellidos;
    //private LocalDate fecha_nacimiento; // Usaremos esto en lugar de edad
    private int edad;
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String foto; // Aquí guardaremos el Base64 que envías desde Android

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDateTime fecha_registro = LocalDateTime.now();
    private Boolean acepta_terminos = true;
}