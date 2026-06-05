ALTER TABLE disponibilidade
    ADD CONSTRAINT chk_disponibilidade_dia_semana
        CHECK (dia_semana BETWEEN 1 AND 7);

ALTER TABLE disponibilidade
    ADD CONSTRAINT chk_disponibilidade_horario
        CHECK (horario_inicio < horario_fim);

CREATE UNIQUE INDEX IF NOT EXISTS ux_disponibilidade_ativa_slot
    ON disponibilidade (barbeiro_id, dia_semana, horario_inicio, horario_fim)
    WHERE ativo = TRUE;
