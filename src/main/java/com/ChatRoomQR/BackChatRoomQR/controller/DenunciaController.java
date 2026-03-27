package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.Denuncia;
import com.ChatRoomQR.BackChatRoomQR.repository.DenunciaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearDenuncia(
            @RequestParam int id_usuario_denunciante,
            @RequestParam int id_usuario_denunciado,
            @RequestParam String tipo_denuncia,
            @RequestParam(required = false) String razon_denuncia) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_denunciante)) {
            response.put("status", "error");
            response.put("message", "El usuario denunciante no existe");
            return ResponseEntity.badRequest().body(response);
        }

        if (!usuarioRepository.existsById(id_usuario_denunciado)) {
            response.put("status", "error");
            response.put("message", "El usuario denunciado no existe");
            return ResponseEntity.badRequest().body(response);
        }

        if (tipo_denuncia == null || tipo_denuncia.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Debes seleccionar un tipo de denuncia");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Denuncia denuncia = new Denuncia();
            denuncia.setIdUsuario(id_usuario_denunciado);
            denuncia.setTipoDenuncia(tipo_denuncia.toLowerCase());
            if (razon_denuncia != null && !razon_denuncia.trim().isEmpty()) {
                denuncia.setRazonDenuncia(razon_denuncia.trim());
            }
            denuncia.setFechaCreacion(LocalDateTime.now());
            denuncia.setEstado("pendiente");

            denunciaRepository.save(denuncia);

            response.put("status", "success");
            response.put("message", "Denuncia registrada correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al registrar la denuncia: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
