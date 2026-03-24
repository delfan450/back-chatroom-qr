package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.MensajeGrupal;
import com.ChatRoomQR.BackChatRoomQR.model.Sala;
import com.ChatRoomQR.BackChatRoomQR.repository.MensajeRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
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

    // --- 1. INFO DE LA SALA ---
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
            return ResponseEntity.status(404).body(response);
        });
    }

    // --- 2. OBTENER MENSAJES ---
    @GetMapping("/mensajes/{idSala}") // Cambiado a idSala para coincidir con Android Path
    public ResponseEntity<?> getMensajesGrupal(@PathVariable String idSala) {
        // Usamos idSala porque así lo definiste en @Path("id_sala") de tu Interface (o idSala)
        List<MensajeGrupal> mensajes = mensajeRepository.obtenerMensajesPorSala(idSala);
        return ResponseEntity.ok(mensajes);
    }

    // --- 3. ENVIAR MENSAJE ---
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensajeGrupal(
            @RequestParam String idSala,    // CAMBIO: Quitado el guion bajo
            @RequestParam int idUsuario,    // CAMBIO: Quitado el guion bajo
            @RequestParam String mensaje) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Buscamos en los repositorios usando los nombres de las variables
            if (!salaRepository.existsById(idSala)) {
                response.put("status", "error");
                response.put("message", "Sala no encontrada");
                return ResponseEntity.badRequest().body(response);
            }

            MensajeGrupal nuevoMensaje = new MensajeGrupal();
            nuevoMensaje.setId_sala(idSala);
            nuevoMensaje.setId_usuario(idUsuario);
            nuevoMensaje.setMensaje(mensaje);
            nuevoMensaje.setFecha_hora(LocalDateTime.now());

            mensajeRepository.save(nuevoMensaje);

            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // --- 4. VERIFICAR SESIÓN ---
    @GetMapping("/verificar-sesion")
    public ResponseEntity<Map<String, Object>> verificarSesionSala(
            @RequestParam int idUsuario, // Coincide con @Query("id_usuario") de Android si usas idUsuario
            @RequestParam String idSala) {

        Map<String, Object> response = new HashMap<>();
        boolean salaValida = salaRepository.existsById(idSala);

        response.put("status", salaValida ? "success" : "error");
        response.put("expirado", !salaValida);
        return ResponseEntity.ok(response);
    }
}