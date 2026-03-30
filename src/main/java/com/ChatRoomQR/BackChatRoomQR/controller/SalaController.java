package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.Sala;
import com.ChatRoomQR.BackChatRoomQR.model.UsuarioSala;
import com.ChatRoomQR.BackChatRoomQR.repository.SalaRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioSalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    @Autowired private SalaRepository salaRepository;
    @Autowired private UsuarioSalaRepository usuarioSalaRepository;

    // GET /api/salas/mis-salas?id_usuario=X
    @GetMapping("/mis-salas")
    public ResponseEntity<List<Map<String, Object>>> getMisSalas(@RequestParam int id_usuario) {
        List<UsuarioSala> uniones = usuarioSalaRepository.findByIdUsuario(id_usuario);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (UsuarioSala us : uniones) {
            if ("inactivo".equals(us.getEstado())) continue;

            // Excluir si sigue baneado
            if ("expulsado".equals(us.getEstado())) {
                boolean baneoActivo = true;
                if (us.getDuracionExpulsion() != null && us.getDuracionExpulsion() > 0 && us.getFechaExpulsion() != null) {
                    java.time.LocalDateTime finBan = us.getFechaExpulsion().plusMinutes(us.getDuracionExpulsion());
                    baneoActivo = !java.time.LocalDateTime.now().isAfter(finBan);
                }
                if (baneoActivo) continue;
                // Baneo expirado — no mostrar en lista hasta que reentren
                continue;
            }

            salaRepository.findById(us.getIdSala()).ifPresent(sala -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id_sala", sala.getId_sala());
                item.put("nombre", sala.getNombre_sala());
                item.put("latitud", sala.getLatitud() != null ? sala.getLatitud() : 0.0);
                item.put("longitud", sala.getLongitud() != null ? sala.getLongitud() : 0.0);
                item.put("radio_metros", sala.getRadio_metros() != null ? sala.getRadio_metros() : 0.0);
                item.put("tiempo_maximo", sala.getTiempo_maximo() != null ? sala.getTiempo_maximo() : 120);
                item.put("estado", us.getEstado());

                // Calcular minutos restantes
                long minutosRestantes = -1;
                if (sala.getTiempo_maximo() != null && sala.getTiempo_maximo() > 0 && us.getFechaUnion() != null) {
                    java.time.LocalDateTime expiracion = us.getFechaUnion().plusMinutes(sala.getTiempo_maximo());
                    minutosRestantes = java.time.Duration.between(java.time.LocalDateTime.now(), expiracion).toMinutes();
                    if (minutosRestantes < 0) minutosRestantes = 0;
                }
                item.put("minutos_restantes", minutosRestantes);

                resultado.add(item);
            });
        }
        return ResponseEntity.ok(resultado);
    }

    // GET /api/salas/{id_sala}
    @GetMapping("/{id_sala}")
    public ResponseEntity<Map<String, Object>> getSalaInfo(@PathVariable String id_sala) {
        return salaRepository.findById(id_sala).map(sala -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id_sala", sala.getId_sala());
            item.put("nombre", sala.getNombre_sala());
            item.put("latitud", sala.getLatitud() != null ? sala.getLatitud() : 0.0);
            item.put("longitud", sala.getLongitud() != null ? sala.getLongitud() : 0.0);
            item.put("radio_metros", sala.getRadio_metros() != null ? sala.getRadio_metros() : 0.0);
            item.put("tiempo_maximo", sala.getTiempo_maximo() != null ? sala.getTiempo_maximo() : 120);
            return ResponseEntity.ok(item);
        }).orElse(ResponseEntity.notFound().build());
    }

    // POST /api/salas/salir
    @PostMapping("/salir")
    public ResponseEntity<Map<String, Object>> salirDeSala(
            @RequestParam int id_usuario,
            @RequestParam String id_sala) {
        usuarioSalaRepository.findByIdUsuarioAndIdSala(id_usuario, id_sala).ifPresent(us -> {
            us.setEstado("inactivo");
            usuarioSalaRepository.save(us);
        });
        Map<String, Object> r = new HashMap<>();
        r.put("status", "success");
        return ResponseEntity.ok(r);
    }
}
