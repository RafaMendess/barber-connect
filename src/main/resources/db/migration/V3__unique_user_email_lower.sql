CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_email_lower
    ON usuario (LOWER(email));
