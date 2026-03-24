package com.ChatRoomQR.BackChatRoomQR.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "accesos_salas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AccesoSalaId.class) // <--- ESTO ES CRUCIAL: Vincula con la otra clase
public class AccesoSala {

    @Id // Primera parte de la llave
    @Column(name = "id_usuario")
    private Integer id_usuario;

    @Id // Segunda parte de la llave
    @Column(name = "id_sala")
    private String id_sala;

    @Column(name = "fecha_entrada")
    private LocalDateTime fecha_entrada = LocalDateTime.now();
}