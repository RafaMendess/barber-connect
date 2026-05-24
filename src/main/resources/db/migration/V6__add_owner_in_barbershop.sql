ALTER TABLE barbearia
    ADD COLUMN IF NOT EXISTS owner_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_barbearia_owner'
    ) THEN
        ALTER TABLE barbearia
            ADD CONSTRAINT fk_barbearia_owner
                FOREIGN KEY (owner_id)
                    REFERENCES usuario(id);
    END IF;
END $$;
