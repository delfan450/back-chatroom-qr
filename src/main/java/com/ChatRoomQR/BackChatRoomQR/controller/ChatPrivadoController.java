package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.ChatPrivado;
import com.ChatRoomQR.BackChatRoomQR.model.Sala;
import com.ChatRoomQR.BackChatRoomQR.repository.ChatPrivadoRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.service.PrivateChatCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private PrivateChatCleanupService privateChatCleanupService;

    // POST /api/chat/privado/crear?id_usuario_1=X&id_usuario_2=Y&id_sala_origen=GENERAL
    // Devuelve ID deterministico de conversacion; los datos reales viven en los mensajes.
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearChat(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2,
            @RequestParam(required = false) String id_sala_origen) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_1) || !usuarioRepository.existsById(id_usuario_2)) {
            response.put("status", "error");
            response.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(response);
        }

        if (hasText(id_sala_origen) && !salaRepository.existsById(id_sala_origen)) {
            response.put("status", "error");
            response.put("message", "La sala origen no existe");
            return ResponseEntity.badRequest().body(response);
        }

        int id_chat_privado = getIdChatDeterministico(id_usuario_1, id_usuario_2);

        response.put("status", "success");
        response.put("id_chat_privado", id_chat_privado);
        response.put("estado", estadoConversacion(id_usuario_1, id_usuario_2));
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/info?id_usuario_1=X&id_usuario_2=Y
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfoChat(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2) {

        Map<String, Object> response = new HashMap<>();
        Optional<ChatPrivado> ultimoOpt = ultimoMensaje(id_usuario_1, id_usuario_2);

        response.put("status", "success");
        response.put("id_chat_privado", getIdChatDeterministico(id_usuario_1, id_usuario_2));

        if (ultimoOpt.isEmpty()) {
            response.put("eliminado", false);
            response.put("id_sala_origen", "");
            response.put("estado", "VACIO");
            return ResponseEntity.ok(response);
        }

        ChatPrivado ultimo = ultimoOpt.get();
        response.put("eliminado", Boolean.TRUE.equals(ultimo.getEliminado()));
        response.put("estado", normalizarEstado(ultimo.getEstado()));
        response.put("motivo_eliminacion", ultimo.getMotivoEliminacion());
        response.put("id_sala_origen", ultimo.getIdSalaOrigen() != null ? ultimo.getIdSalaOrigen() : "");

        if (hasText(ultimo.getIdSalaOrigen())) {
            salaRepository.findById(ultimo.getIdSalaOrigen()).ifPresent(sala -> anadirInfoSala(response, sala));
        }

        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/mensajes?id_usuario_1=X&id_usuario_2=Y
    @GetMapping("/mensajes")
    public ResponseEntity<?> getMensajes(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2) {

        if (chatEstaEliminado(id_usuario_1, id_usuario_2)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "deleted");
            response.put("message", "Chat privado eliminado");
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }

        List<ChatPrivado> mensajes = chatPrivadoRepository.findMensajes(id_usuario_1, id_usuario_2);

        for (ChatPrivado m : mensajes) {
            usuarioRepository.findById(m.getIdEmisor()).ifPresent(u -> {
                m.setNombre(u.getNombre());
                m.setNombre_usuario(u.getNombreUsuario());
            });
        }

        return ResponseEntity.ok(mensajes);
    }

    // POST /api/chat/privado/enviar?id_usuario_1=X&id_usuario_2=Y&id_usuario_emisor=Z&mensaje=W&id_sala_origen=GENERAL
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2,
            @RequestParam int id_usuario_emisor,
            @RequestParam String mensaje,
            @RequestParam(required = false) String id_sala_origen) {

        Map<String, Object> response = new HashMap<>();

        if (!usuarioRepository.existsById(id_usuario_1) || !usuarioRepository.existsById(id_usuario_2)) {
            response.put("status", "error");
            response.put("message", "Uno o ambos usuarios no existen");
            return ResponseEntity.badRequest().body(response);
        }

        if (chatEstaEliminado(id_usuario_1, id_usuario_2)) {
            response.put("status", "deleted");
            response.put("message", "Chat privado eliminado");
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }

        Optional<ChatPrivado> ultimoOpt = ultimoMensaje(id_usuario_1, id_usuario_2);
        String estado = ultimoOpt.map(m -> normalizarEstado(m.getEstado())).orElse("PENDIENTE");
        String salaOrigen = hasText(id_sala_origen)
                ? id_sala_origen
                : ultimoOpt.map(ChatPrivado::getIdSalaOrigen).orElse(null);

        if (esControlAceptado(mensaje)) {
            chatPrivadoRepository.updateEstadoConversacion(id_usuario_1, id_usuario_2, "ACEPTADO");
            response.put("status", "success");
            response.put("estado", "ACEPTADO");
            return ResponseEntity.ok(response);
        }
        if (esControlRechazado(mensaje)) {
            chatPrivadoRepository.updateEstadoConversacion(id_usuario_1, id_usuario_2, "RECHAZADO");
            response.put("status", "success");
            response.put("estado", "RECHAZADO");
            return ResponseEntity.ok(response);
        }

        int idReceptor = (id_usuario_emisor == id_usuario_1) ? id_usuario_2 : id_usuario_1;

        ChatPrivado m = new ChatPrivado();
        m.setIdEmisor(id_usuario_emisor);
        m.setIdReceptor(idReceptor);
        m.setIdSalaOrigen(salaOrigen);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        m.setLeida(false);
        m.setEliminado(false);
        m.setEstado(estado);
        chatPrivadoRepository.save(m);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // POST /api/chat/privado/eliminar?id_usuario_1=X&id_usuario_2=Y&id_sala_origen=GENERAL&motivo=...
    @PostMapping("/eliminar")
    public ResponseEntity<Map<String, Object>> eliminarChat(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2,
            @RequestParam(required = false) String id_sala_origen,
            @RequestParam(required = false) String motivo) {

        List<ChatPrivado> mensajes = chatPrivadoRepository.findConversacion(id_usuario_1, id_usuario_2);
        for (ChatPrivado mensaje : mensajes) {
            if (hasText(id_sala_origen)) {
                mensaje.setIdSalaOrigen(id_sala_origen);
            }
            mensaje.setEliminado(true);
            mensaje.setMotivoEliminacion(hasText(motivo) ? motivo : "Chat privado eliminado por geovalla");
            mensaje.setFechaEliminacion(LocalDateTime.now());
            chatPrivadoRepository.save(mensaje);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Chat privado eliminado");
        return ResponseEntity.ok(response);
    }


    // POST /api/chat/privado/eliminar-por-sala?id_usuario=X&id_sala_origen=GENERAL&motivo=...
    @PostMapping("/eliminar-por-sala")
    public ResponseEntity<Map<String, Object>> eliminarChatsPorSala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala_origen,
            @RequestParam(required = false) String motivo) {

        Map<String, Object> response = new HashMap<>();

        if (!hasText(id_sala_origen)) {
            response.put("status", "error");
            response.put("message", "La sala origen es obligatoria");
            return ResponseEntity.badRequest().body(response);
        }

        int eliminados = privateChatCleanupService.cerrarChatsDeSalaParaUsuario(
                id_usuario,
                id_sala_origen,
                hasText(motivo) ? motivo : "Chat privado eliminado por salida de la geovalla"
        );

        response.put("status", "success");
        response.put("eliminados", eliminados);
        response.put("message", "Chats privados de la sala eliminados");
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/resumen-notificaciones?id_usuario=X
    @GetMapping("/resumen-notificaciones")
    public ResponseEntity<List<Map<String, Object>>> getResumenNotificaciones(@RequestParam int id_usuario) {
        List<ChatPrivado> noLeidos = chatPrivadoRepository.findResumenNoLeidos(id_usuario);
        List<Map<String, Object>> response = new ArrayList<>();

        for (ChatPrivado mensaje : noLeidos) {
            Map<String, Object> item = new HashMap<>();
            int remitenteId = mensaje.getIdEmisor();
            item.put("remitente_id", remitenteId);
            item.put("id_mensaje", mensaje.getIdChatPrivado());
            item.put("contenido", mensaje.getMensaje());
            item.put("estado", estadoConversacion(id_usuario, remitenteId));
            usuarioRepository.findById(remitenteId).ifPresent(u -> {
                item.put("nombre", u.getNombre());
                item.put("nombre_usuario", u.getNombreUsuario());
            });
            response.add(item);
        }

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

    private Optional<ChatPrivado> ultimoMensaje(int idUsuario1, int idUsuario2) {
        return chatPrivadoRepository.findConversacion(idUsuario1, idUsuario2).stream().findFirst();
    }

    private boolean chatEstaEliminado(int idUsuario1, int idUsuario2) {
        return ultimoMensaje(idUsuario1, idUsuario2)
                .map(mensaje -> Boolean.TRUE.equals(mensaje.getEliminado()))
                .orElse(false);
    }

    private String estadoConversacion(int idUsuario1, int idUsuario2) {
        return ultimoMensaje(idUsuario1, idUsuario2)
                .map(mensaje -> normalizarEstado(mensaje.getEstado()))
                .orElse("VACIO");
    }

    private int getIdChatDeterministico(int idUsuario1, int idUsuario2) {
        int minId = Math.min(idUsuario1, idUsuario2);
        int maxId = Math.max(idUsuario1, idUsuario2);
        return minId * 10000 + maxId;
    }

    private void anadirInfoSala(Map<String, Object> response, Sala sala) {
        response.put("latitud", sala.getLatitud() != null ? sala.getLatitud() : 0.0);
        response.put("longitud", sala.getLongitud() != null ? sala.getLongitud() : 0.0);
        response.put("radio_metros", sala.getRadio_metros() != null ? sala.getRadio_metros() : 0.0);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean esControlAceptado(String message) {
        return "[[PRIVATE_CHAT_STATUS:ACCEPTED]]".equals(message);
    }

    private boolean esControlRechazado(String message) {
        return "[[PRIVATE_CHAT_STATUS:REJECTED]]".equals(message);
    }

    private String normalizarEstado(String estado) {
        return hasText(estado) ? estado.toUpperCase(Locale.ROOT) : "PENDIENTE";
    }
}

