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

    @Column(unique = true)
    private String nombre_usuario;

    private String nombre;
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private java.time.LocalDate fechaNacimiento;

    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt;

    private LocalDateTime fecha_registro = LocalDateTime.now();
    private Boolean acepta_terminos = true;
}