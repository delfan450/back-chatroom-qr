package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.MensajeGrupal;
import com.ChatRoomQR.BackChatRoomQR.model.Sala;
import com.ChatRoomQR.BackChatRoomQR.model.UsuarioSala;
import com.ChatRoomQR.BackChatRoomQR.repository.MensajeRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.RolUsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioSalaRepository;
import com.ChatRoomQR.BackChatRoomQR.service.PrivateChatCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private SalaRepository salaRepository;
    @Autowired private UsuarioSalaRepository usuarioSalaRepository;
    @Autowired private RolUsuarioRepository rolUsuarioRepository;
    @Autowired private PrivateChatCleanupService privateChatCleanupService;

    // --- INFO DE LA SALA ---
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

    // --- OBTENER MENSAJES ---
    @GetMapping("/mensajes/{id_sala}")
    public ResponseEntity<?> getMensajesGrupal(
            @PathVariable String id_sala,
            @RequestParam int id_usuario) {
        if (!salaRepository.existsById(id_sala)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "No se pueden cargar mensajes de una sala inexistente");
            return ResponseEntity.status(404).body(error);
        }
        Optional<Sala> salaOpt = salaRepository.findById(id_sala);
        ResponseEntity<Map<String, Object>> acceso = validarAccesoSala(id_usuario, id_sala, salaOpt.orElse(null), null, null, true);
        if (acceso != null) {
            return acceso;
        }
        List<MensajeGrupal> mensajes = mensajeRepository.obtenerMensajesPorSala(id_sala);
        for (MensajeGrupal m : mensajes) {
            usuarioRepository.findById(m.getId_usuario()).ifPresent(u -> {
                m.setNombre(u.getNombre());
                m.setNombre_usuario(u.getNombreUsuario());
            });
        }
        return ResponseEntity.ok(mensajes);
    }

    // --- ENVIAR MENSAJE ---
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarMensajeGrupal(
            @RequestParam String id_sala,
            @RequestParam int id_usuario,
            @RequestParam String mensaje,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud) {

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
            Optional<Sala> salaOpt = salaRepository.findById(id_sala);
            ResponseEntity<Map<String, Object>> acceso = validarAccesoSala(id_usuario, id_sala, salaOpt.orElse(null), latitud, longitud, true);
            if (acceso != null) {
                return acceso;
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

    // ---  VERIFICAR SESIÓN ---
    @GetMapping("/verificar-sesion")
    public ResponseEntity<Map<String, Object>> verificarSesionSala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud) {

        Map<String, Object> response = new HashMap<>();

        Optional<Sala> salaOpt = salaRepository.findById(id_sala);
        if (salaOpt.isEmpty()) {
            response.put("status", "error");
            response.put("expirado", true);
            response.put("motivo", "");
            return ResponseEntity.ok(response);
        }

        Optional<UsuarioSala> usOpt = usuarioSalaRepository.findByIdUsuarioAndIdSala(id_usuario, id_sala);
        if (usOpt.isEmpty()) {
            response.put("status", "success");
            response.put("expirado", false);
            response.put("minutos_restantes", -1);
            return ResponseEntity.ok(response);
        }

        UsuarioSala us = usOpt.get();

        // Expulsado por el admin
        if ("expulsado".equals(us.getEstado())) {
            // Comprobar si el baneo temporal ha expirado
            if (us.getDuracionExpulsion() != null && us.getDuracionExpulsion() > 0 && us.getFechaExpulsion() != null) {
                LocalDateTime finBan = us.getFechaExpulsion().plusMinutes(us.getDuracionExpulsion());
                if (LocalDateTime.now().isAfter(finBan)) {
                    // El baneo ha expirado y  dejar que pueda reentrar
                    us.setEstado("inactivo");
                    usuarioSalaRepository.save(us);
                    // Continúa al bloque de éxito
                } else {
                    response.put("status", "error");
                    response.put("expirado", true);
                    response.put("motivo", us.getMotivoExpulsion() != null ? us.getMotivoExpulsion() : "");
                    response.put("minutos_restantes", 0);
                    return ResponseEntity.ok(response);
                }
            } else {
                // Baneo permanente
                response.put("status", "error");
                response.put("expirado", true);
                response.put("motivo", us.getMotivoExpulsion() != null ? us.getMotivoExpulsion() : "");
                response.put("minutos_restantes", 0);
                return ResponseEntity.ok(response);
            }
        }

        // Comprobar si salio voluntariamente o fue marcado como inactivo
        if ("inactivo".equals(us.getEstado())) {
            response.put("status", "error");
            response.put("expirado", true);
            response.put("motivo", "");
            return ResponseEntity.ok(response);
        }

        // Comprobar tiempo máximo de sesión
        Sala sala = salaOpt.get();
        if (sala.getTiempo_maximo() != null && sala.getTiempo_maximo() > 0 && us.getFechaUnion() != null) {
            LocalDateTime expiracion = us.getFechaUnion().plusMinutes(sala.getTiempo_maximo());
            if (LocalDateTime.now().isAfter(expiracion)) {
                us.setEstado("inactivo");
                usuarioSalaRepository.save(us);
                privateChatCleanupService.cerrarChatsDeSalaParaUsuario(id_usuario, id_sala, "Tiempo de sala agotado");
                response.put("status", "error");
                response.put("expirado", true);
                response.put("motivo", "");
                response.put("minutos_restantes", 0);
                return ResponseEntity.status(403).body(response);
            }
            long minutosRestantes = java.time.Duration.between(LocalDateTime.now(), expiracion).toMinutes();
            response.put("minutos_restantes", minutosRestantes);
        } else {
            response.put("minutos_restantes", -1);
        }

        ResponseEntity<Map<String, Object>> acceso = validarAccesoSala(id_usuario, id_sala, sala, latitud, longitud, false);
        if (acceso != null) {
            return acceso;
        }

        response.put("status", "success");
        response.put("expirado", false);
        return ResponseEntity.ok(response);
    }

    // --- UNIRSE A SALA ---
    @PostMapping("/unirse")
    public ResponseEntity<Map<String, Object>> unirseASala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {

        Map<String, Object> response = new HashMap<>();

        if (!salaRepository.existsById(id_sala)) {
            Sala nuevaSala = new Sala();
            nuevaSala.setId_sala(id_sala);
            nuevaSala.setNombre_sala(id_sala);
            nuevaSala.setDescripcion("");
            nuevaSala.setLatitud(0.0);
            nuevaSala.setLongitud(0.0);
            nuevaSala.setRadio_metros(0.0);
            nuevaSala.setTiempo_maximo(120);
            salaRepository.save(nuevaSala);
        }

        Optional<UsuarioSala> existente = usuarioSalaRepository.findByIdUsuarioAndIdSala(id_usuario, id_sala);
        if (existente.isPresent()) {
            UsuarioSala us = existente.get();

            // Comprobar baneado
            if ("expulsado".equals(us.getEstado())) {
                boolean baneoActivo = true;
                if (us.getDuracionExpulsion() != null && us.getDuracionExpulsion() > 0 && us.getFechaExpulsion() != null) {
                    LocalDateTime finBan = us.getFechaExpulsion().plusMinutes(us.getDuracionExpulsion());
                    baneoActivo = !LocalDateTime.now().isAfter(finBan);
                }
                if (baneoActivo) {
                    response.put("status", "expulsado");
                    response.put("motivo", us.getMotivoExpulsion() != null ? us.getMotivoExpulsion() : "");
                    return ResponseEntity.status(403).body(response);
                }
            }

            us.setEstado("activo");
            us.setFechaUnion(LocalDateTime.now());
            usuarioSalaRepository.save(us);
        } else {
            UsuarioSala us = new UsuarioSala();
            us.setIdUsuario(id_usuario);
            us.setIdSala(id_sala);
            us.setEstado("activo");
            us.setFechaUnion(LocalDateTime.now());
            usuarioSalaRepository.save(us);
        }

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // --- EXPULSAR DE SALA --
    @PostMapping("/expulsar")
    public ResponseEntity<Map<String, Object>> expulsarDeChat(
            @RequestParam int id_usuario_admin,
            @RequestParam int id_usuario_expulsado,
            @RequestParam String id_sala,
            @RequestParam String motivo,
            @RequestParam(defaultValue = "0") int duracion_minutos) {

        Map<String, Object> response = new HashMap<>();

        // Verificar que quien expulsa es el admin
        boolean tienePermiso = rolUsuarioRepository.findByIdUsuario(id_usuario_admin)
                .map(r -> r.getIdRol() == 1 || r.getIdRol() == 2)
                .orElse(false);

        if (!tienePermiso) {
            response.put("status", "error");
            response.put("message", "Sin permisos para expulsar");
            return ResponseEntity.status(403).body(response);
        }

        UsuarioSala us = usuarioSalaRepository
                .findByIdUsuarioAndIdSala(id_usuario_expulsado, id_sala)
                .orElseGet(() -> {
                    UsuarioSala nuevo = new UsuarioSala();
                    nuevo.setIdUsuario(id_usuario_expulsado);
                    nuevo.setIdSala(id_sala);
                    return nuevo;
                });

        us.setEstado("expulsado");
        us.setMotivoExpulsion(motivo);
        us.setDuracionExpulsion(duracion_minutos);
        us.setFechaExpulsion(LocalDateTime.now());
        usuarioSalaRepository.save(us);
        privateChatCleanupService.cerrarChatsDeSalaParaUsuario(id_usuario_expulsado, id_sala, motivo);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> validarAccesoSala(
            int idUsuario,
            String idSala,
            Sala sala,
            Double latitud,
            Double longitud,
            boolean exigirUbicacionReciente) {

        Map<String, Object> response = new HashMap<>();
        Optional<UsuarioSala> usOpt = usuarioSalaRepository.findByIdUsuarioAndIdSala(idUsuario, idSala);
        if (usOpt.isEmpty()) {
            response.put("status", "error");
            response.put("expirado", true);
            response.put("motivo", "Sesion de sala no activa");
            return ResponseEntity.status(403).body(response);
        }

        UsuarioSala us = usOpt.get();
        if (!"activo".equalsIgnoreCase(us.getEstado())) {
            response.put("status", "error");
            response.put("expirado", true);
            response.put("motivo", us.getMotivoExpulsion() != null ? us.getMotivoExpulsion() : "Sesion de sala no activa");
            return ResponseEntity.status(403).body(response);
        }

        if (sala == null) {
            return null;
        }

        if (sala.getTiempo_maximo() != null && sala.getTiempo_maximo() > 0 && us.getFechaUnion() != null) {
            LocalDateTime expiracion = us.getFechaUnion().plusMinutes(sala.getTiempo_maximo());
            if (LocalDateTime.now().isAfter(expiracion)) {
                us.setEstado("inactivo");
                usuarioSalaRepository.save(us);
                privateChatCleanupService.cerrarChatsDeSalaParaUsuario(idUsuario, idSala, "Tiempo de sala agotado");
                response.put("status", "error");
                response.put("expirado", true);
                response.put("motivo", "Tiempo de sala agotado");
                response.put("minutos_restantes", 0);
                return ResponseEntity.status(403).body(response);
            }
        }

        if (!tieneGeovalla(sala)) {
            return null;
        }

        if (latitud != null && longitud != null) {
            us.setUltimaLatitud(latitud);
            us.setUltimaLongitud(longitud);
            us.setUltimaUbicacionAt(LocalDateTime.now());
            usuarioSalaRepository.save(us);

            double distancia = distanciaMetros(latitud, longitud, sala.getLatitud(), sala.getLongitud());
            double radio = sala.getRadio_metros() != null ? sala.getRadio_metros() : 0.0;
            if (distancia > radio) {
                us.setEstado("inactivo");
                usuarioSalaRepository.save(us);
                privateChatCleanupService.cerrarChatsDeSalaParaUsuario(idUsuario, idSala, "Fuera de la geovalla");
                response.put("status", "error");
                response.put("expirado", true);
                response.put("motivo", "Fuera de la geovalla");
                return ResponseEntity.status(403).body(response);
            }
            return null;
        }

        if (exigirUbicacionReciente) {
            boolean ubicacionReciente = us.getUltimaUbicacionAt() != null
                    && java.time.Duration.between(us.getUltimaUbicacionAt(), LocalDateTime.now()).getSeconds() <= 15;
            if (!ubicacionReciente) {
                response.put("status", "error");
                response.put("expirado", true);
                response.put("motivo", "Ubicacion requerida");
                return ResponseEntity.status(403).body(response);
            }
        }

        return null;
    }

    private boolean tieneGeovalla(Sala sala) {
        return sala != null
                && sala.getRadio_metros() != null
                && sala.getRadio_metros() > 0
                && sala.getLatitud() != null
                && sala.getLongitud() != null;
    }

    private double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final double radioTierra = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radioTierra * c;
    }
}
