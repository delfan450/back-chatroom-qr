package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.MensajePrivado;
import com.ChatRoomQR.BackChatRoomQR.repository.MensajePrivadoRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat/privado")
public class ChatPrivadoController {

    @Autowired
    private MensajePrivadoRepository mensajePrivadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // POST /api/chat/privado/crear?id_usuario_1=X&id_usuario_2=Y
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearChat(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_1) || !usuarioRepository.existsById(id_usuario_2)) {
            response.put("status", "error");
            response.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(response);
        }

        // Generar un ID como min(id_usuario_1, id_usuario_2) * 10000 + max(id_usuario_1, id_usuario_2)
        int minId = Math.min(id_usuario_1, id_usuario_2);
        int maxId = Math.max(id_usuario_1, id_usuario_2);
        int id_chat_privado = minId * 10000 + maxId;

        response.put("status", "success");
        response.put("id_chat_privado", id_chat_privado);
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/mensajes?id_usuario_1=X&id_usuario_2=Y
    @GetMapping("/mensajes")
    public ResponseEntity<?> getMensajes(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2) {
        List<MensajePrivado> mensajes = mensajePrivadoRepository.findByUsuarios(id_usuario_1, id_usuario_2);

        for (MensajePrivado m : mensajes) {
            usuarioRepository.findById(m.getIdEmisor())
                    .ifPresent(u -> {
                        m.setNombre(u.getNombre());
                        m.setNombre_usuario(u.getNombre_usuario());
                    });
        }

        return ResponseEntity.ok(mensajes);
    }

    // POST /api/chat/privado/enviar?id_usuario_1=X&id_usuario_2=Y&id_usuario_emisor=Z&mensaje=W
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2,
            @RequestParam int id_usuario_emisor,
            @RequestParam String mensaje) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_1) || !usuarioRepository.existsById(id_usuario_2)) {
            response.put("status", "error");
            response.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(response);
        }

        // Determinar quién es el receptor
        int id_usuario_receptor = (id_usuario_emisor == id_usuario_1) ? id_usuario_2 : id_usuario_1;

        MensajePrivado m = new MensajePrivado();
        m.setIdEmisor(id_usuario_emisor);
        m.setIdReceptor(id_usuario_receptor);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        mensajePrivadoRepository.save(m);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
