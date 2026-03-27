package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.Notificacion;
import com.ChatRoomQR.BackChatRoomQR.repository.NotificacionRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/obtener")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(
            @RequestParam int id_usuario) {

        List<Notificacion> notificaciones = notificacionRepository.findByIdUsuarioReceptor(id_usuario);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> obtenerNotificacionesNoLeidas(
            @RequestParam int id_usuario) {

        List<Notificacion> notificaciones = notificacionRepository.findNotLeidas(id_usuario);
        return ResponseEntity.ok(notificaciones);
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearNotificacion(
            @RequestParam int id_usuario_receptor,
            @RequestParam int id_usuario_remitente,
            @RequestParam String tipo_notificacion,
            @RequestParam String contenido) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_receptor)) {
            response.put("status", "error");
            response.put("message", "Usuario receptor no existe");
            return ResponseEntity.badRequest().body(response);
        }

        if (!usuarioRepository.existsById(id_usuario_remitente)) {
            response.put("status", "error");
            response.put("message", "Usuario remitente no existe");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Notificacion notificacion = new Notificacion();
            notificacion.setIdUsuarioReceptor(id_usuario_receptor);
            notificacion.setIdUsuarioRemitente(id_usuario_remitente);
            notificacion.setTipoNotificacion(tipo_notificacion.toLowerCase());
            notificacion.setContenido(contenido);
            notificacion.setFechaCreacion(LocalDateTime.now());
            notificacion.setLeida(false);

            notificacionRepository.save(notificacion);

            response.put("status", "success");
            response.put("message", "Notificación creada");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al crear notificación: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/marcar-leida/{id}")
    public ResponseEntity<Map<String, Object>> marcarComoLeida(
            @PathVariable int id) {

        Map<String, Object> response = new HashMap<>();

        try {
            Notificacion notificacion = notificacionRepository.findById(id).orElse(null);
            if (notificacion == null) {
                response.put("status", "error");
                response.put("message", "Notificación no encontrada");
                return ResponseEntity.badRequest().body(response);
            }

            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);

            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
