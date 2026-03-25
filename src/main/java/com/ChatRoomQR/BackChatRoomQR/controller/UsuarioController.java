package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.RolUsuario;
import com.ChatRoomQR.BackChatRoomQR.model.Usuario;
import com.ChatRoomQR.BackChatRoomQR.repository.RolUsuarioRepository;
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

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    // 1. REGISTRO DE USUARIO
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String fecha_nacimiento,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String password,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        if (!email.contains("@") || !email.contains(".")) {
            response.put("message", "El formato del email no es válido");
            return ResponseEntity.badRequest().body(response);
        }

        if (telefono.length() < 9) {
            response.put("message", "El teléfono debe tener al menos 9 dígitos");
            return ResponseEntity.badRequest().body(response);
        }

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
            nuevo.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

            usuarioRepository.save(nuevo);

            // Asignar rol Usuario (id_rol=3) por defecto
            RolUsuario rolUsuario = new RolUsuario();
            rolUsuario.setIdUsuario(nuevo.getId_usuario());
            rolUsuario.setIdRol(3);
            rolUsuarioRepository.save(rolUsuario);

            response.put("status", "success");
            response.put("message", "Usuario registrado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 2. LOGIN DE USUARIO
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String email,
            @RequestParam String password) {

        Map<String, Object> response = new HashMap<>();
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                // Obtener rol de forma segura — nunca falla el login si el rol da error
                String rolNombre = "usuario";
                try {
                    rolNombre = rolUsuarioRepository.findRolNameByIdUsuario(user.getId_usuario())
                            .orElse("usuario").toLowerCase();
                } catch (Exception ignored) {}

                response.put("status", "success");
                response.put("id_usuario", user.getId_usuario());
                response.put("nombre", user.getNombre());
                response.put("rol", rolNombre);
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

    // 3. OBTENER DATOS
    @GetMapping("/{id_usuario}")
    public ResponseEntity<Usuario> getUsuario(@PathVariable("id_usuario") int id_usuario) {
        return usuarioRepository.findById(id_usuario)
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
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        return usuarioRepository.findById(id).map(user -> {
            user.setNombre(nombre);
            user.setApellidos(apellidos);
            user.setFechaNacimiento(java.time.LocalDate.parse(fecha_nacimiento));
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

    // 5. CHECK EMAIL
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> r = new HashMap<>();
        r.put("existe", usuarioRepository.findByEmail(email).isPresent());
        return ResponseEntity.ok(r);
    }

    // 6. BUSCAR POR EMAIL
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarPorEmail(@RequestParam String email) {
        Map<String, Object> r = new HashMap<>();
        Optional<Usuario> opt = usuarioRepository.findByEmail(email);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Usuario u = opt.get();
        r.put("id_usuario", u.getId_usuario());
        r.put("nombre", u.getNombre());
        r.put("apellidos", u.getApellidos());
        r.put("email", u.getEmail());
        r.put("telefono", u.getTelefono());
        r.put("fechaNacimiento", u.getFechaNacimiento());
        String rol = rolUsuarioRepository.findRolNameByIdUsuario(u.getId_usuario()).orElse("usuario");
        r.put("rol", rol);
        return ResponseEntity.ok(r);
    }

    // 7. CAMBIAR ROL
    @PostMapping("/cambiar-rol")
    public ResponseEntity<Map<String, Object>> cambiarRol(
            @RequestParam int id_usuario,
            @RequestParam String nuevo_rol) {
        Map<String, Object> r = new HashMap<>();
        Map<String, Integer> mapa = Map.of("owner", 1, "admin", 2, "usuario", 3);
        int idRol = mapa.getOrDefault(nuevo_rol.toLowerCase(), 3);
        rolUsuarioRepository.findByIdUsuario(id_usuario).ifPresent(ru -> {
            ru.setIdRol(idRol);
            rolUsuarioRepository.save(ru);
        });
        r.put("status", "success");
        return ResponseEntity.ok(r);
    }

    // 8. OBTENER ROL
    @GetMapping("/{id_usuario}/rol")
    public ResponseEntity<Map<String, Object>> getRol(@PathVariable int id_usuario) {
        Map<String, Object> r = new HashMap<>();
        Optional<RolUsuario> ru = rolUsuarioRepository.findByIdUsuario(id_usuario);
        if (ru.isEmpty()) return ResponseEntity.notFound().build();
        String nombre = rolUsuarioRepository.findRolNameByIdUsuario(id_usuario).orElse("usuario");
        r.put("id_rol", ru.get().getIdRol());
        r.put("rol", nombre);
        return ResponseEntity.ok(r);
    }
}
