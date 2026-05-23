CREATE TABLE IF NOT EXISTS user_otps (
    id BIGSERIAL PRIMARY KEY,
    code_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    usuario_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_otps_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuario(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_user_otps_usuario_purpose
    ON user_otps(usuario_id, purpose);

ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN NOT NULL DEFAULT FALSE;