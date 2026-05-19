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
    // Devuelve ID deterministico del chat y, si viene de una sala, asocia su geovalla.
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

        if (hasText(id_sala_origen)) {
            if (!salaRepository.existsById(id_sala_origen)) {
                response.put("status", "error");
                response.put("message", "La sala origen no existe");
                return ResponseEntity.badRequest().body(response);
            }

            ChatPrivado meta = obtenerOCrearMeta(id_usuario_1, id_usuario_2);
            meta.setIdSalaOrigen(id_sala_origen);
            meta.setEliminado(false);
            meta.setMotivoEliminacion(null);
            meta.setFechaEliminacion(null);
            if (!hasText(meta.getEstado())) {
                meta.setEstado("PENDIENTE");
            }
            chatPrivadoRepository.save(meta);
        }

        int id_chat_privado = getIdChatDeterministico(id_usuario_1, id_usuario_2);

        response.put("status", "success");
        response.put("id_chat_privado", id_chat_privado);
        buscarMeta(id_usuario_1, id_usuario_2).ifPresent(meta -> response.put("estado", normalizarEstado(meta.getEstado())));
        return ResponseEntity.ok(response);
    }

    // GET /api/chat/privado/info?id_usuario_1=X&id_usuario_2=Y
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfoChat(
            @RequestParam int id_usuario_1,
            @RequestParam int id_usuario_2) {

        Map<String, Object> response = new HashMap<>();
        Optional<ChatPrivado> metaOpt = buscarMeta(id_usuario_1, id_usuario_2);

        response.put("status", "success");
        response.put("id_chat_privado", getIdChatDeterministico(id_usuario_1, id_usuario_2));

        if (metaOpt.isEmpty()) {
            response.put("eliminado", false);
            response.put("id_sala_origen", "");
            response.put("estado", "VACIO");
            return ResponseEntity.ok(response);
        }

        ChatPrivado meta = metaOpt.get();
        response.put("eliminado", Boolean.TRUE.equals(meta.getEliminado()));
        response.put("estado", normalizarEstado(meta.getEstado()));
        response.put("motivo_eliminacion", meta.getMotivoEliminacion());
        response.put("id_sala_origen", meta.getIdSalaOrigen() != null ? meta.getIdSalaOrigen() : "");

        if (hasText(meta.getIdSalaOrigen())) {
            salaRepository.findById(meta.getIdSalaOrigen()).ifPresent(sala -> anadirInfoSala(response, sala));
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

        if (hasText(id_sala_origen)) {
            ChatPrivado meta = obtenerOCrearMeta(id_usuario_1, id_usuario_2);
            if (!hasText(meta.getIdSalaOrigen())) {
                meta.setIdSalaOrigen(id_sala_origen);
            }
            if (!hasText(meta.getEstado())) {
                meta.setEstado("PENDIENTE");
            }
            chatPrivadoRepository.save(meta);
        }

        ChatPrivado meta = obtenerOCrearMeta(id_usuario_1, id_usuario_2);
        if (esControlAceptado(mensaje)) {
            meta.setEstado("ACEPTADO");
            chatPrivadoRepository.save(meta);
        } else if (esControlRechazado(mensaje)) {
            meta.setEstado("RECHAZADO");
            chatPrivadoRepository.save(meta);
        } else if (!hasText(meta.getEstado()) || "VACIO".equalsIgnoreCase(meta.getEstado())) {
            meta.setEstado("PENDIENTE");
            chatPrivadoRepository.save(meta);
        }

        int idReceptor = (id_usuario_emisor == id_usuario_1) ? id_usuario_2 : id_usuario_1;

        ChatPrivado m = new ChatPrivado();
        m.setIdEmisor(id_usuario_emisor);
        m.setIdReceptor(idReceptor);
        m.setMensaje(mensaje);
        m.setFechaHora(LocalDateTime.now());
        m.setLeida(false);
        m.setEsMeta(false);
        m.setEstado(normalizarEstado(meta.getEstado()));
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

        ChatPrivado meta = obtenerOCrearMeta(id_usuario_1, id_usuario_2);
        if (hasText(id_sala_origen)) {
            meta.setIdSalaOrigen(id_sala_origen);
        }
        meta.setEliminado(true);
        meta.setMotivoEliminacion(hasText(motivo) ? motivo : "Chat privado eliminado por geovalla");
        meta.setFechaEliminacion(LocalDateTime.now());
        chatPrivadoRepository.save(meta);

        chatPrivadoRepository.deleteMensajesEntre(id_usuario_1, id_usuario_2);

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
            item.put("id_mensaje", mensaje.getId());
            item.put("contenido", mensaje.getMensaje());
            buscarMeta(id_usuario, remitenteId)
                    .ifPresentOrElse(
                            meta -> item.put("estado", normalizarEstado(meta.getEstado())),
                            () -> item.put("estado", "PENDIENTE")
                    );
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

    private ChatPrivado obtenerOCrearMeta(int idUsuario1, int idUsuario2) {
        int minId = Math.min(idUsuario1, idUsuario2);
        int maxId = Math.max(idUsuario1, idUsuario2);
        return chatPrivadoRepository
                .findMetaByPair(minId, maxId)
                .orElseGet(() -> {
                    ChatPrivado meta = new ChatPrivado();
                    meta.setIdUsuarioMenor(minId);
                    meta.setIdUsuarioMayor(maxId);
                    meta.setIdEmisor(minId);
                    meta.setIdReceptor(maxId);
                    meta.setMensaje("[[PRIVATE_CHAT_META]]");
                    meta.setFechaHora(LocalDateTime.now());
                    meta.setLeida(true);
                    meta.setEsMeta(true);
                    meta.setEliminado(false);
                    meta.setEstado("PENDIENTE");
                    return meta;
                });
    }

    private Optional<ChatPrivado> buscarMeta(int idUsuario1, int idUsuario2) {
        int minId = Math.min(idUsuario1, idUsuario2);
        int maxId = Math.max(idUsuario1, idUsuario2);
        return chatPrivadoRepository.findMetaByPair(minId, maxId);
    }

    private boolean chatEstaEliminado(int idUsuario1, int idUsuario2) {
        return buscarMeta(idUsuario1, idUsuario2)
                .map(meta -> Boolean.TRUE.equals(meta.getEliminado()))
                .orElse(false);
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

