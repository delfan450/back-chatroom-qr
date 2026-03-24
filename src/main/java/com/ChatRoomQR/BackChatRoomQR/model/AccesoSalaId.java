package com.ChatRoomQR.BackChatRoomQR.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccesoSalaId implements Serializable {
    private Integer id_usuario;
    private String id_sala;
}
