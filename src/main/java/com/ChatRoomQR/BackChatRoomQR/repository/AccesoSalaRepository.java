package com.ChatRoomQR.BackChatRoomQR.repository;

import com.ChatRoomQR.BackChatRoomQR.model.AccesoSala;
import com.ChatRoomQR.BackChatRoomQR.model.AccesoSalaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccesoSalaRepository extends JpaRepository<AccesoSala, AccesoSalaId> {
    // Aquí ya hereda el findById que acepta un AccesoSalaId
}
