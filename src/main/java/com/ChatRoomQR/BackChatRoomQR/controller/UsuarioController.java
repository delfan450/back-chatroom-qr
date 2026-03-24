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
            @RequestParam boolean acepta_terminos,
            @RequestParam String password,
            @RequestParam(required = false) String foto) {



        Map<String, Object> response = new HashMap<>();
        if (!acepta_terminos) {
            response.put("status", "error");
            response.put("message", "Debes aceptar los términos y condiciones para continuar.");
            return ResponseEntity.badRequest().body(response);
        }
        // 1. Validar Email (que tenga @ y .)
        if (!email.contains("@") || !email.contains(".")) {
            response.put("message", "El formato del email no es válido");
            return ResponseEntity.badRequest().body(response);
        }

// 2. Validar Teléfono (mínimo 9 dígitos)
        if (telefono.length() < 9) {
            response.put("message", "El teléfono debe tener al menos 9 dígitos");
            return ResponseEntity.badRequest().body(response);
        }

// 3. Validar Contraseña (mínimo 6 caracteres por seguridad)
        if (password.length() < 6) {
            response.put("message", "La contraseña debe tener al menos 6 caracteres");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (usuarioRepository.findByEmail(email).isPresent()) {
                response.put("status", "error");
                response.put("message", "El email ya está registrado");
                return ResponseEntity.badRequest().body(response);
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos);

            nuevo.setFechaNacimiento(java.time.LocalDate.parse(fecha_nacimiento));

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

    // 3. OBTENER DATOS (Ruta: /api/usuarios/{id_usuario})
    @GetMapping("/{id_usuario}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable("id_usuario") int id_usuario) {
        return usuarioRepository.findById(id_usuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR PERFIL (Corregido el nombre de la variable Path y el setEdad)
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

            user.setFechaNacimiento(java.time.LocalDate.parse(fecha_nacimiento));
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

    @GetMapping("/terminos-legales")
    public ResponseEntity<Map<String, String>> getTerminos() {
        Map<String, String> terminos = new HashMap<>();
        terminos.put("titulo", "Términos y Condiciones de ChatRoom QR");
        terminos.put("contenido", "Al usar esta app, aceptas que tus mensajes sean visibles " +
                "para los usuarios que entren a la sala después de ti. " +
                "Tus datos están protegidos por la ley de protección de datos...");
        return ResponseEntity.ok(terminos);
    }
}