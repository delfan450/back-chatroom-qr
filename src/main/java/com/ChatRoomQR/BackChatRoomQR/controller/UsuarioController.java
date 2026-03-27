package com.ChatRoomQR.BackChatRoomQR.controller;

import com.ChatRoomQR.BackChatRoomQR.model.RolUsuario;
import com.ChatRoomQR.BackChatRoomQR.model.Usuario;
import com.ChatRoomQR.BackChatRoomQR.repository.RolUsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.repository.UsuarioRepository;
import com.ChatRoomQR.BackChatRoomQR.service.EmailService;
import com.ChatRoomQR.BackChatRoomQR.service.GoogleOAuthService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    // 1. INICIAR REGISTRO (Step 1: Enviar código de verificación)
    @PostMapping("/iniciar-registro")
    public ResponseEntity<Map<String, Object>> iniciarRegistro(
            @RequestParam String email) {

        Map<String, Object> response = new HashMap<>();

        if (!email.contains("@") || !email.contains(".")) {
            response.put("status", "error");
            response.put("message", "El formato del email no es válido");
            return ResponseEntity.badRequest().body(response);
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            response.put("status", "error");
            response.put("message", "El email ya está registrado");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Generar código de 6 dígitos
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));

            // Crear usuario temporal sin verificar
            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setIsVerified(false);
            nuevo.setVerificationCode(codigo);
            nuevo.setCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
            usuarioRepository.save(nuevo);

            // Enviar código por email
            emailService.enviarCodigoVerificacion(email, codigo);

            response.put("status", "success");
            response.put("message", "Código de verificación enviado al email");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 2. VERIFICAR CÓDIGO Y COMPLETAR REGISTRO (Step 2: Completar datos después de verificar código)
    @PostMapping("/verificar-codigo")
    public ResponseEntity<Map<String, Object>> verificarCodigo(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String nombre_usuario,
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String fecha_nacimiento,
            @RequestParam String telefono,
            @RequestParam String password,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email no encontrado");
            return ResponseEntity.badRequest().body(response);
        }

        // Verificar que nombre_usuario sea único
        if (usuarioRepository.findByNombreUsuario(nombre_usuario).isPresent()) {
            response.put("status", "error");
            response.put("message", "El nombre de usuario ya existe");
            return ResponseEntity.badRequest().body(response);
        }

        Usuario usuario = userOpt.get();

        // Verificar código
        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            response.put("status", "error");
            response.put("message", "Código de verificación inválido");
            return ResponseEntity.badRequest().body(response);
        }

        // Verificar si el código ha expirado
        if (usuario.getCodeExpiresAt() != null && LocalDateTime.now().isAfter(usuario.getCodeExpiresAt())) {
            response.put("status", "error");
            response.put("message", "El código de verificación ha expirado");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Completar datos del usuario
            usuario.setNombreUsuario(nombre_usuario);
            usuario.setNombre(nombre);
            usuario.setApellidos(apellidos);
            usuario.setFechaNacimiento(java.time.LocalDate.parse(fecha_nacimiento));
            usuario.setTelefono(telefono);
            usuario.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            usuario.setFoto(foto);
            usuario.setIsVerified(true);
            usuario.setVerificationCode(null);
            usuario.setCodeExpiresAt(null);

            Usuario saved = usuarioRepository.save(usuario);

            // Asignar rol Usuario (id_rol=3) si no existe
            if (rolUsuarioRepository.findByIdUsuario(saved.getId_usuario()).isEmpty()) {
                RolUsuario rolUsuario = new RolUsuario();
                rolUsuario.setIdUsuario(saved.getId_usuario());
                rolUsuario.setIdRol(3);
                rolUsuarioRepository.save(rolUsuario);
            }

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

            // Verificar que el usuario está verificado
            if (!Boolean.TRUE.equals(user.getIsVerified())) {
                response.put("status", "error");
                response.put("message", "El email aún no ha sido verificado. Por favor, revisa tu bandeja de entrada.");
                return ResponseEntity.status(403).body(response);
            }

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

    // 4. CHECK EMAIL
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> r = new HashMap<>();
        r.put("existe", usuarioRepository.findByEmail(email).isPresent());
        return ResponseEntity.ok(r);
    }

    // 4.5 SUGERIR NOMBRES DE USUARIO
    @GetMapping("/sugerir-nombre-usuario")
    public ResponseEntity<Map<String, Object>> sugerirNombreUsuario(@RequestParam String base) {
        Map<String, Object> r = new HashMap<>();

        boolean disponible = usuarioRepository.findByNombreUsuario(base).isEmpty();
        r.put("disponible", disponible);

        if (!disponible) {
            List<String> sugerencias = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String sugerencia = base + "_" + i;
                if (usuarioRepository.findByNombreUsuario(sugerencia).isEmpty()) {
                    sugerencias.add(sugerencia);
                }
            }
            r.put("sugerencias", sugerencias);
        } else {
            r.put("sugerencias", new ArrayList<>());
        }

        return ResponseEntity.ok(r);
    }

    // 6. BUSCAR POR EMAIL
    @GetMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscarPorEmail(@RequestParam String email) {
        // 1. Buscamos una lista de usuarios que contengan ese texto (Ignora mayúsculas/minúsculas)
        List<Usuario> usuarios = usuarioRepository.findByEmailContainingIgnoreCase(email);

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 si no hay nadie
        }

        // 2. Transformamos la lista de Usuarios en una lista de Mapas con sus roles
        List<Map<String, Object>> respuesta = usuarios.stream().map(u -> {
            Map<String, Object> r = new HashMap<>();
            r.put("id_usuario", u.getId_usuario());
            r.put("nombre", u.getNombre());
            r.put("apellidos", u.getApellidos());
            r.put("email", u.getEmail());
            r.put("telefono", u.getTelefono());
            r.put("fechaNacimiento", u.getFechaNacimiento());

            // Buscamos el rol de cada uno
            String rol = rolUsuarioRepository.findRolNameByIdUsuario(u.getId_usuario()).orElse("usuario");
            r.put("rol", rol);

            return r;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
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


    // 10. VERIFICAR GOOGLE OAUTH
    @PostMapping("/verificar-google")
    public ResponseEntity<Map<String, Object>> verificarGoogle(
            @RequestParam String id_token,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String foto) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar token de Google
            Map<String, String> tokenInfo = googleOAuthService.validateTokenAndGetInfo(id_token);
            if (tokenInfo == null) {
                response.put("status", "error");
                response.put("message", "Token de Google inválido");
                return ResponseEntity.badRequest().body(response);
            }

            String email = tokenInfo.get("email");
            String googleId = tokenInfo.get("google_id");

            // Buscar usuario existente
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isPresent()) {
                // Usuario existe, actualizar si es necesario
                Usuario usuario = usuarioOpt.get();
                usuario.setGoogleId(googleId);
                usuario.setIsVerified(true);
                if (nombre != null) usuario.setNombre(nombre);
                if (apellidos != null) usuario.setApellidos(apellidos);
                if (telefono != null) usuario.setTelefono(telefono);
                if (foto != null) usuario.setFoto(foto);
                usuarioRepository.save(usuario);

                String rolNombre = "usuario";
                try {
                    rolNombre = rolUsuarioRepository.findRolNameByIdUsuario(usuario.getId_usuario())
                            .orElse("usuario").toLowerCase();
                } catch (Exception ignored) {}

                response.put("status", "success");
                response.put("message", "Login con Google exitoso");
                response.put("id_usuario", usuario.getId_usuario());
                response.put("nombre", usuario.getNombre());
                response.put("rol", rolNombre);
            } else {
                // Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setGoogleId(googleId);
                nuevoUsuario.setIsVerified(true);
                nuevoUsuario.setNombre(nombre != null ? nombre : tokenInfo.getOrDefault("nombre", "Usuario"));
                nuevoUsuario.setApellidos(apellidos != null ? apellidos : tokenInfo.getOrDefault("apellidos", ""));
                nuevoUsuario.setTelefono(telefono != null ? telefono : "");
                nuevoUsuario.setFoto(foto != null ? foto : tokenInfo.get("foto"));
                nuevoUsuario.setPassword(""); // Sin contraseña para usuarios OAuth

                Usuario saved = usuarioRepository.save(nuevoUsuario);

                // Asignar rol Usuario (id_rol=3) por defecto
                RolUsuario rolUsuario = new RolUsuario();
                rolUsuario.setIdUsuario(saved.getId_usuario());
                rolUsuario.setIdRol(3);
                rolUsuarioRepository.save(rolUsuario);

                response.put("status", "success");
                response.put("message", "Usuario creado y verificado con Google");
                response.put("id_usuario", saved.getId_usuario());
                response.put("nombre", saved.getNombre());
                response.put("rol", "usuario");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 9. REENVIAR VERIFICACIÓN - Genera un código en la tabla usuarios y lo envía por email
    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<Map<String, Object>> reenviarVerificacion(
            @RequestParam String email) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "El email no está registrado");
                return ResponseEntity.badRequest().body(response);
            }

            Usuario usuario = usuarioOpt.get();
            if (Boolean.TRUE.equals(usuario.getIsVerified())) {
                response.put("status", "info");
                response.put("message", "Este email ya está verificado. Puedes iniciar sesión.");
                return ResponseEntity.ok(response);
            }

            // Generar código de 6 dígitos y guardarlo en la tabla usuarios
            String codigo = String.format("%06d", (int)(Math.random() * 1000000));
            usuario.setVerificationCode(codigo);
            usuario.setCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
            usuarioRepository.save(usuario);

            emailService.enviarCodigoVerificacion(email, codigo);

            response.put("status", "success");
            response.put("message", "Código de verificación enviado al email");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
