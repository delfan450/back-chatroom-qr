package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.AccesoSala;
import com.ChatRoomQR.BackChatRoomQR.model.AccesoSalaId;
import com.ChatRoomQR.BackChatRoomQR.model.MensajeGrupal;
import com.ChatRoomQR.BackChatRoomQR.model.Sala;
import com.ChatRoomQR.BackChatRoomQR.repository.AccesoSalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.MensajeRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository; // Nuevo
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
    private AccesoSalaRepository accesoSalaRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private SalaRepository salaRepository; // <--- Inyectamos para validar locales reales

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
    public ResponseEntity<?> getMensajesGrupal(
            @PathVariable String id_sala,
            @RequestParam int id_usuario) { // Ahora pedimos el ID del usuario que consulta

        // 1. Buscamos cuándo entró este usuario a esta sala
        Optional<AccesoSala> acceso = accesoSalaRepository.findById(new AccesoSalaId(id_usuario, id_sala));

        if (acceso.isEmpty()) {
            // Si no hay registro de entrada, devolvemos lista vacía (Privacidad total)
            return ResponseEntity.ok(new ArrayList<>());
        }

        LocalDateTime fechaFiltro = acceso.get().getFecha_entrada();

        // 2. Buscamos mensajes filtrando por fecha
        List<MensajeGrupal> mensajes = mensajeRepository.obtenerMensajesDesdeFecha(id_sala, fechaFiltro);

        // 3. Tu bucle de nombres (el que ya tenías)
        for (MensajeGrupal m : mensajes) {
            usuarioRepository.findById(m.getId_usuario()).ifPresent(u -> m.setNombre(u.getNombre()));
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
            // VALIDACIÓN CRÍTICA: ¿Existe el local y el usuario?
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

    // --- 4. VERIFICAR SESIÓN (Lógica dinámica) ---
    @GetMapping("/verificar-sesion")
    public ResponseEntity<Map<String, Object>> verificarSesionSala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {

        Map<String, Object> response = new HashMap<>();

        // Aquí podrías añadir lógica de tiempo real.
        // Por ahora, validamos al menos que la sala exista.
        boolean salaValida = salaRepository.existsById(id_sala);

        response.put("status", salaValida ? "success" : "error");
        response.put("expirado", !salaValida);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entrar-sala")
    public ResponseEntity<Map<String, Object>> registrarEntrada(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {

        Map<String, Object> response = new HashMap<>();

        // 1. Validar que la sala existe
        if (!salaRepository.existsById(id_sala)) {
            response.put("status", "error");
            response.put("message", "Este código QR no pertenece a ninguna sala válida");
            return ResponseEntity.status(404).body(response);
        }

        // 2. Guardar o actualizar la fecha de entrada
        AccesoSala acceso = new AccesoSala();
        acceso.setId_usuario(id_usuario);
        acceso.setId_sala(id_sala);
        acceso.setFecha_entrada(java.time.LocalDateTime.now());

        accesoSalaRepository.save(acceso);

        response.put("status", "success");
        response.put("message", "Has entrado a la sala: " + id_sala);
        return ResponseEntity.ok(response);
    }
}