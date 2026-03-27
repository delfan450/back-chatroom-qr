package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.MensajeGrupal;
import com.ChatRoomQR.BackChatRoomQR.model.UsuarioSala;
import com.ChatRoomQR.BackChatRoomQR.repository.MensajeRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioSalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private UsuarioSalaRepository usuarioSalaRepository;

    // --- 1. INFO DE LA SALA (Para cuando escanean el QR) ---
    @GetMapping("/sala/{id_sala}")
    public ResponseEntity<Map<String, Object>> obtenerInfoSala(@PathVariable String id_sala) {
        Map<String, Object> response = new HashMap<>();

        return salaRepository.findById(id_sala).map(sala -> {
            response.put("status", "success");
            response.put("nombre_sala", sala.getNombre_sala());
            response.put("descripcion", sala.getDescripcion());
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("status", "error");
            response.put("message", "La sala no existe en el sistema");
            return ResponseEntity.status(404).body(response);
        });
    }

    // --- 2. OBTENER MENSAJES ---
    @GetMapping("/mensajes/{id_sala}")
    public ResponseEntity<?> getMensajesGrupal(@PathVariable String id_sala) {
        if (!salaRepository.existsById(id_sala)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "No se pueden cargar mensajes de una sala inexistente");
            return ResponseEntity.status(404).body(error);
        }

        List<MensajeGrupal> mensajes = mensajeRepository.obtenerMensajesPorSala(id_sala);

        for (MensajeGrupal m : mensajes) {
            usuarioRepository.findById(m.getId_usuario()).ifPresent(u -> {
                m.setNombre(u.getNombre());
                m.setNombre_usuario(u.getNombre_usuario());
            });
        }
        return ResponseEntity.ok(mensajes);
    }

    // --- 3. ENVIAR MENSAJE ---
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensajeGrupal(
            @RequestParam String id_sala,
            @RequestParam int id_usuario,
            @RequestParam String mensaje) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (!salaRepository.existsById(id_sala)) {
                response.put("status", "error");
                response.put("message", "ID de sala no válido");
                return ResponseEntity.badRequest().body(response);
            }

            if (!usuarioRepository.existsById(id_usuario)) {
                response.put("status", "error");
                response.put("message", "ID de usuario no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            MensajeGrupal nuevoMensaje = new MensajeGrupal();
            nuevoMensaje.setId_sala(id_sala);
            nuevoMensaje.setId_usuario(id_usuario);
            nuevoMensaje.setMensaje(mensaje);
            nuevoMensaje.setFecha_hora(LocalDateTime.now());

            mensajeRepository.save(nuevoMensaje);

            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al procesar el mensaje: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // --- 4. VERIFICAR SESIÓN ---
    @GetMapping("/verificar-sesion")
    public ResponseEntity<Map<String, Object>> verificarSesionSala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {

        Map<String, Object> response = new HashMap<>();

        boolean salaValida = salaRepository.existsById(id_sala);

        response.put("status", salaValida ? "success" : "error");
        response.put("expirado", !salaValida);
        return ResponseEntity.ok(response);
    }

    // --- 5. UNIRSE A SALA ---
    @PostMapping("/unirse")
    public ResponseEntity<Map<String, Object>> unirseASala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {

        Map<String, Object> response = new HashMap<>();

        if (!salaRepository.existsById(id_sala)) {
            response.put("status", "error");
            response.put("message", "La sala no existe");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<UsuarioSala> existente = usuarioSalaRepository.findByIdUsuarioAndIdSala(id_usuario, id_sala);
        if (existente.isEmpty()) {
            UsuarioSala us = new UsuarioSala();
            us.setIdUsuario(id_usuario);
            us.setIdSala(id_sala);
            us.setEstado("activo");
            us.setFechaUnion(LocalDateTime.now());
            usuarioSalaRepository.save(us);
        } else {
            existente.get().setEstado("activo");
            usuarioSalaRepository.save(existente.get());
        }

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
