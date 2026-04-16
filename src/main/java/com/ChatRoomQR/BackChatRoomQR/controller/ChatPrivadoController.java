package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import com.ChatRoomQR.BackChatRoomQR.repository.ChatPrivadoRepository;
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
    private UsuarioRepository usuarioRepository;

    // POST /api/chat/privado/crear?id_usuario_1=X&id_usuario_2=Y
    // Devuelve ID determinístico del chat (no crea fila; sólo identifica el par)
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

        List<ChatPrivado> mensajes = chatPrivadoRepository.findMensajes(id_usuario_1, id_usuario_2);

        for (ChatPrivado m : mensajes) {
            usuarioRepository.findById(m.getIdEmisor()).ifPresent(u -> {
                m.setNombre(u.getNombre());
                m.setNombre_usuario(u.getNombreUsuario());
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

        int idReceptor = (id_usuario_emisor == id_usuario_1) ? id_usuario_2 : id_usuario_1;

        ChatPrivado m = new ChatPrivado();
        m.setIdEmisor(id_usuario_emisor);
        m.setIdReceptor(idReceptor);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        m.setLeida(false);
        chatPrivadoRepository.save(m);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/no-leidos?id_usuario=X
    @GetMapping("/no-leidos")
    public ResponseEntity<?> getNoLeidos(@RequestParam int id_usuario) {
        List<ChatPrivado> noLeidos = chatPrivadoRepository.findNoLeidos(id_usuario);

        for (ChatPrivado m : noLeidos) {
            usuarioRepository.findById(m.getIdEmisor()).ifPresent(u -> {
                m.setNombre(u.getNombre());
                m.setNombre_usuario(u.getNombreUsuario());
            });
        }

        return ResponseEntity.ok(noLeidos);
    }

    // PUT /api/chat/privado/marcar-leido/{id}
    @PutMapping("/marcar-leido/{id}")
    public ResponseEntity<Map<String, Object>> marcarLeido(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        return chatPrivadoRepository.findById(id).map(m -> {
            m.setLeida(true);
            chatPrivadoRepository.save(m);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("status", "error");
            response.put("message", "Mensaje no encontrado");
            return ResponseEntity.status(404).body(response);
        });
    }

    // PUT /api/chat/privado/marcar-todos-leidos?id_usuario_1=X&id_usuario_2=Y&id_usuario_lector=Z
    @PutMapping("/marcar-todos-leidos")
    public ResponseEntity<Map<String, Object>> marcarTodosLeidos(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2,
            @RequestParam int id_usuario_lector) {

        List<ChatPrivado> mensajes = chatPrivadoRepository.findMensajes(id_usuario_1, id_usuario_2);
        for (ChatPrivado m : mensajes) {
            if (m.getIdReceptor().equals(id_usuario_lector) && !Boolean.TRUE.equals(m.getLeida())) {
                m.setLeida(true);
                chatPrivadoRepository.save(m);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
