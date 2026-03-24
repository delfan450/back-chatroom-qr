package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.Usuario;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. REGISTRO DE USUARIO (Revertido a 'int edad')
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam int edad, // VOLVEMOS A INT (Como lo envía tu Android)
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

            // VOLVEMOS A SETEDAD (Asegúrate que en tu modelo Usuario.java sea 'int edad')
            nuevo.setEdad(edad);

            nuevo.setEmail(email);
            nuevo.setTelefono(telefono);
            nuevo.setFoto(foto);

            // ENCRIPTACIÓN
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

    // 2. LOGIN (Se queda igual)
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
    @PostMapping("/actualizar/{id_usuario}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable("id_usuario") int id_usuario, // Nombre coincidente con la ruta
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam int edad,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        return usuarioRepository.findById(id_usuario).map(user -> {
            user.setNombre(nombre);
            user.setApellidos(apellidos);
            user.setEdad(edad); // Volvemos a usar edad
            user.setEmail(email);
            user.setTelefono(telefono);
            user.setFoto(foto);

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            }

            usuarioRepository.save(user);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
}