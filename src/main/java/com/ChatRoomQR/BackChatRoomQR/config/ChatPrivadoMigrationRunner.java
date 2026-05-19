package com.ChatRoomQR.BackChatRoomQR.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatPrivadoMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ChatPrivadoMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE chats_privados ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'PENDIENTE'");
        jdbcTemplate.execute("UPDATE chats_privados SET estado = 'PENDIENTE' WHERE estado IS NULL");
        jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'PENDIENTE'");
        jdbcTemplate.execute("UPDATE usuarios SET estado = CASE WHEN is_verified = TRUE THEN 'ACTIVO' ELSE 'PENDIENTE' END WHERE estado IS NULL");
        jdbcTemplate.execute("ALTER TABLE salas ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'activa'");
        jdbcTemplate.execute("UPDATE salas SET estado = 'activa' WHERE estado IS NULL");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_latitud DOUBLE PRECISION");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_longitud DOUBLE PRECISION");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_ubicacion_at TIMESTAMP");

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_chats_privados_meta_pair
                ON chats_privados (id_usuario_menor, id_usuario_mayor)
                WHERE es_meta = TRUE
                """);

        Integer metaTableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'chats_privados_meta'
                """, Integer.class);

        if (metaTableCount == null || metaTableCount == 0) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO chats_privados (
                    id_emisor,
                    id_receptor,
                    id_usuario_menor,
                    id_usuario_mayor,
                    id_sala_origen,
                    es_meta,
                    eliminado,
                    motivo_eliminacion,
                    fecha_eliminacion,
                    mensaje,
                    fecha_hora,
                    leida
                )
                SELECT
                    meta.id_usuario_menor,
                    meta.id_usuario_mayor,
                    meta.id_usuario_menor,
                    meta.id_usuario_mayor,
                    meta.id_sala_origen,
                    TRUE,
                    COALESCE(meta.eliminado, FALSE),
                    meta.motivo_eliminacion,
                    meta.fecha_eliminacion,
                    '[[PRIVATE_CHAT_META]]',
                    COALESCE(meta.fecha_eliminacion, CURRENT_TIMESTAMP),
                    TRUE
                FROM chats_privados_meta meta
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM chats_privados cp
                    WHERE cp.es_meta = TRUE
                      AND cp.id_usuario_menor = meta.id_usuario_menor
                      AND cp.id_usuario_mayor = meta.id_usuario_mayor
                )
                """);

        jdbcTemplate.execute("DROP TABLE chats_privados_meta");
    }
}
