package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import com.ChatRoomQR.BackChatRoomQR.model.MensajePrivado;
import com.ChatRoomQR.BackChatRoomQR.repository.ChatPrivadoRepository;
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
    private ChatPrivadoRepository chatPrivadoRepository;

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

        ChatPrivado chat = chatPrivadoRepository
                .findByUsuarios(id_usuario_1, id_usuario_2)
                .orElseGet(() -> {
                    ChatPrivado nuevo = new ChatPrivado();
                    nuevo.setIdUsuario1(id_usuario_1);
                    nuevo.setIdUsuario2(id_usuario_2);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    return chatPrivadoRepository.save(nuevo);
                });

        response.put("status", "success");
        response.put("id_chat_privado", chat.getId_chat_privado());
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/mensajes?id_chat_privado=X
    @GetMapping("/mensajes")
    public ResponseEntity<?> getMensajes(@RequestParam int id_chat_privado) {
        List<MensajePrivado> mensajes = mensajePrivadoRepository.findByChat(id_chat_privado);

        for (MensajePrivado m : mensajes) {
            usuarioRepository.findById(m.getIdUsuarioEmisor())
                    .ifPresent(u -> m.setNombre(u.getNombre()));
        }

        return ResponseEntity.ok(mensajes);
    }

    // POST /api/chat/privado/enviar?id_chat_privado=X&id_usuario_emisor=Y&mensaje=Z
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam int id_chat_privado,
            @RequestParam int id_usuario_emisor,
            @RequestParam String mensaje) {

        Map<String, Object> response = new HashMap<>();

        if (!chatPrivadoRepository.existsById(id_chat_privado)) {
            response.put("status", "error");
            response.put("message", "Chat no encontrado");
            return ResponseEntity.badRequest().body(response);
        }

        MensajePrivado m = new MensajePrivado();
        m.setIdChatPrivado(id_chat_privado);
        m.setIdUsuarioEmisor(id_usuario_emisor);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        mensajePrivadoRepository.save(m);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
