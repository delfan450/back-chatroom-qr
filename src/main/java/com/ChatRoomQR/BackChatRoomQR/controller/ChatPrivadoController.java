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

    // POST /api/chat/privado/crear?id_usuario1=X&id_usuario2=Y
    // Devuelve un id_conversacion estable derivado de los dos usuarios (sin tabla extra)
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearConversacion(
            @RequestParam int id_usuario1,
            @RequestParam int id_usuario2) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario1) || !usuarioRepository.existsById(id_usuario2)) {
            response.put("status", "error");
            response.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(response);
        }

        int min = Math.min(id_usuario1, id_usuario2);
        int max = Math.max(id_usuario1, id_usuario2);

        response.put("status", "success");
        response.put("id_conversacion", min + "_" + max);
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/mensajes?id_usuario1=X&id_usuario2=Y
    @GetMapping("/mensajes")
    public ResponseEntity<?> getMensajes(
            @RequestParam int id_usuario1,
            @RequestParam int id_usuario2) {

        if (!usuarioRepository.existsById(id_usuario1) || !usuarioRepository.existsById(id_usuario2)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(error);
        }

        List<MensajePrivado> mensajes = mensajePrivadoRepository.findConversacion(id_usuario1, id_usuario2);

        for (MensajePrivado m : mensajes) {
            usuarioRepository.findById(m.getIdEmisor()).ifPresent(u -> m.setNombreEmisor(u.getNombre()));
        }

        return ResponseEntity.ok(mensajes);
    }

    // POST /api/chat/privado/enviar
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam int id_emisor,
            @RequestParam int id_receptor,
            @RequestParam String mensaje) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_emisor)) {
            response.put("status", "error");
            response.put("message", "Emisor no encontrado");
            return ResponseEntity.badRequest().body(response);
        }

        if (!usuarioRepository.existsById(id_receptor)) {
            response.put("status", "error");
            response.put("message", "Receptor no encontrado");
            return ResponseEntity.badRequest().body(response);
        }

        MensajePrivado m = new MensajePrivado();
        m.setIdEmisor(id_emisor);
        m.setIdReceptor(id_receptor);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        mensajePrivadoRepository.save(m);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
