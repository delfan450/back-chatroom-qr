package com.ChatRoomQR.BackChatRoomQR.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    private String nombre;
    private String apellidos;

    // ESTO ES LO QUE FALTA:
    //@Column(name = "fecha_nacimiento") // El nombre real en la tabla SQL
    private Integer edad;                 // El nombre que usa tu código Java y Android

    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDateTime fecha_registro = LocalDateTime.now();
    private Boolean acepta_terminos = true;
}