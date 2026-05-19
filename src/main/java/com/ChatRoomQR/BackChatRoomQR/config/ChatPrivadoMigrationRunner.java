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
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'chats_privados'
                          AND column_name = 'es_meta'
                    ) THEN
                        DELETE FROM chats_privados WHERE es_meta = TRUE;
                    END IF;
                END $$;
                """);
        jdbcTemplate.execute("DROP INDEX IF EXISTS ux_chats_privados_meta_pair");
        jdbcTemplate.execute("ALTER TABLE chats_privados DROP COLUMN IF EXISTS es_meta");
        jdbcTemplate.execute("ALTER TABLE chats_privados DROP COLUMN IF EXISTS id_usuario_mayor");
        jdbcTemplate.execute("ALTER TABLE chats_privados DROP COLUMN IF EXISTS id_usuario_menor");
        jdbcTemplate.execute("ALTER TABLE chats_privados DROP COLUMN IF EXISTS id");
        jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'PENDIENTE'");
        jdbcTemplate.execute("UPDATE usuarios SET estado = CASE WHEN is_verified = TRUE THEN 'ACTIVO' ELSE 'PENDIENTE' END WHERE estado IS NULL");
        jdbcTemplate.execute("ALTER TABLE salas ADD COLUMN IF NOT EXISTS estado VARCHAR(32) DEFAULT 'activa'");
        jdbcTemplate.execute("UPDATE salas SET estado = 'activa' WHERE estado IS NULL");
        jdbcTemplate.execute("ALTER TABLE salas DROP COLUMN IF EXISTS tiempo_max_minutos");
        jdbcTemplate.execute("ALTER TABLE salas DROP COLUMN IF EXISTS tiempo_pendiente");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_latitud DOUBLE PRECISION");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_longitud DOUBLE PRECISION");
        jdbcTemplate.execute("ALTER TABLE usuario_sala ADD COLUMN IF NOT EXISTS ultima_ubicacion_at TIMESTAMP");
        jdbcTemplate.execute("DROP TABLE IF EXISTS verification_codes");

        Integer metaTableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'chats_privados_meta'
                """, Integer.class);

        if (metaTableCount == null || metaTableCount == 0) {
            return;
        }

        jdbcTemplate.execute("DROP TABLE chats_privados_meta");
    }
}
