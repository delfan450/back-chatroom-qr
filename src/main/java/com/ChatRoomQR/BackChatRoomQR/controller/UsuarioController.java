package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.Usuario;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. REGISTRO DE USUARIO
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String fecha_nacimiento, // CAMBIO: Recibimos String "YYYY-MM-DD"
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String password,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (usuarioRepository.findByEmail(email).isPresent()) {
                response.put("status", "error");
                response.put("message", "El email ya está registrado");
                return ResponseEntity.badRequest().body(response);
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos);

            // CAMBIO: Guardamos la fecha real enviada desde el DatePicker de Android
            nuevo.setFecha_nacimiento(LocalDate.parse(fecha_nacimiento));

            nuevo.setEmail(email);
            nuevo.setTelefono(telefono);
            nuevo.setFoto(foto);

            // ENCRIPTACIÓN: BCrypt profesional
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            nuevo.setPassword(hashedPassword);

            usuarioRepository.save(nuevo);

            response.put("status", "success");
            response.put("message", "Usuario registrado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 2. LOGIN DE USUARIO (Se queda igual, ya es perfecto)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password) {

        Map<String, Object> response = new HashMap<>();
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                response.put("status", "success");
                response.put("id_usuario", user.getId_usuario());
                response.put("nombre", user.getNombre());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Contraseña incorrecta");
                return ResponseEntity.status(401).body(response);
            }
        } else {
            response.put("status", "error");
            response.put("message", "Usuario no encontrado");
            return ResponseEntity.status(404).body(response);
        }
    }

    // 3. OBTENER DATOS (Se queda igual)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR PERFIL
    @PostMapping("/actualizar/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String fecha_nacimiento,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam(required = false) String password, // Ahora es opcional
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        return usuarioRepository.findById(id).map(user -> {
            user.setNombre(nombre);
            user.setApellidos(apellidos);
            user.setFecha_nacimiento(LocalDate.parse(fecha_nacimiento));
            user.setEmail(email);
            user.setTelefono(telefono);
            user.setFoto(foto);

            // SOLO encriptamos y guardamos si el usuario escribió algo en el campo password
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            }
            // Si viene vacío, no tocamos user.setPassword(), así mantiene la anterior.

            usuarioRepository.save(user);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
}