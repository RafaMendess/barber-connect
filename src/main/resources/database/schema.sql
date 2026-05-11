CREATE TABLE IF NOT EXISTS usuario (
                         id BIGSERIAL PRIMARY KEY,
                         nome VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         senha VARCHAR(255) NOT NULL,
                         telefone VARCHAR(20),
                         cpf VARCHAR(11) UNIQUE,
                         data_nascimento DATE,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         data_criacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS roles  (
                       id BIGSERIAL PRIMARY KEY,
                       nome VARCHAR(50) NOT NULL UNIQUE,
                       descricao VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS barbearia  (
                           id BIGSERIAL PRIMARY KEY,
                           nome VARCHAR(100) NOT NULL,
                           cnpj VARCHAR(19) NOT NULL UNIQUE,
                           telefone VARCHAR(20),
                           endereco VARCHAR(255) NOT NULL,
                           horario_funcionamento VARCHAR(100),
                           foto_url VARCHAR(255),
                           latitude NUMERIC(9,6),
                           longitude NUMERIC(9,6),
                           ativo BOOLEAN NOT NULL DEFAULT TRUE,
                           data_criacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS usuario_roles  (
                               usuario_id BIGINT NOT NULL,
                               role_id BIGINT NOT NULL,

                               PRIMARY KEY (usuario_id, role_id),

                               CONSTRAINT fk_usuario_roles_usuario
                                   FOREIGN KEY (usuario_id)
                                       REFERENCES usuario(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_usuario_roles_role
                                   FOREIGN KEY (role_id)
                                       REFERENCES roles(id)
                                       ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS barbeiro  (
                          id BIGSERIAL PRIMARY KEY,
                          usuario_id BIGINT NOT NULL UNIQUE,
                          barbearia_id BIGINT NOT NULL,
                          especialidade VARCHAR(255),
                          descricao TEXT,
                          ativo BOOLEAN NOT NULL DEFAULT TRUE,
                          data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_barbeiro_usuario
                              FOREIGN KEY (usuario_id)
                                  REFERENCES usuario(id),

                          CONSTRAINT fk_barbeiro_barbearia
                              FOREIGN KEY (barbearia_id)
                                  REFERENCES barbearia(id)
);

CREATE TABLE IF NOT EXISTS servico  (
                         id BIGSERIAL PRIMARY KEY,
                         nome VARCHAR(100) NOT NULL,
                         descricao TEXT,
                         preco NUMERIC(10,2) NOT NULL,
                         duracao_estimada INTEGER NOT NULL,
                         barbearia_id BIGINT NOT NULL,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,
                         data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_servico_barbearia
                             FOREIGN KEY (barbearia_id)
                                 REFERENCES barbearia(id)
);

CREATE TABLE IF NOT EXISTS barbeiro_servico  (
                                  barbeiro_id BIGINT NOT NULL,
                                  servico_id BIGINT NOT NULL,
                                  data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                                  PRIMARY KEY (barbeiro_id, servico_id),

                                  CONSTRAINT fk_barbeiro_servico_barbeiro
                                      FOREIGN KEY (barbeiro_id)
                                          REFERENCES barbeiro(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_barbeiro_servico_servico
                                      FOREIGN KEY (servico_id)
                                          REFERENCES servico(id)
                                          ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS disponibilidade  (
                                 id BIGSERIAL PRIMARY KEY,
                                 barbeiro_id BIGINT NOT NULL,
                                 dia_semana SMALLINT NOT NULL,
                                 horario_inicio TIME NOT NULL,
                                 horario_fim TIME NOT NULL,
                                 ativo BOOLEAN NOT NULL DEFAULT TRUE,
                                 data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_disponibilidade_barbeiro
                                     FOREIGN KEY (barbeiro_id)
                                         REFERENCES barbeiro(id)
                                         ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bloqueio_agenda  (
                                 id BIGSERIAL PRIMARY KEY,
                                 barbeiro_id BIGINT NOT NULL,
                                 inicio TIMESTAMP NOT NULL,
                                 fim TIMESTAMP NOT NULL,
                                 motivo VARCHAR(255),
                                 ativo BOOLEAN NOT NULL DEFAULT TRUE,
                                 data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                                 CONSTRAINT fk_bloqueio_barbeiro
                                     FOREIGN KEY (barbeiro_id)
                                         REFERENCES barbeiro(id)
                                         ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS agendamento  (
                             id BIGSERIAL PRIMARY KEY,
                             data_agendamento TIMESTAMP NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             observacao TEXT,
                             cliente_id BIGINT NOT NULL,
                             barbeiro_id BIGINT NOT NULL,
                             servico_id BIGINT NOT NULL,
                             data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_agendamento_cliente
                                 FOREIGN KEY (cliente_id)
                                     REFERENCES usuario(id),

                             CONSTRAINT fk_agendamento_barbeiro
                                 FOREIGN KEY (barbeiro_id)
                                     REFERENCES barbeiro(id),

                             CONSTRAINT fk_agendamento_servico
                                 FOREIGN KEY (servico_id)
                                     REFERENCES servico(id)
);

CREATE TABLE IF NOT EXISTS pagamento  (
                           id BIGSERIAL PRIMARY KEY,
                           agendamento_id BIGINT NOT NULL UNIQUE,
                           tipo VARCHAR(45) NOT NULL,
                           status VARCHAR(45) NOT NULL,
                           data_pagamento TIMESTAMP,
                           data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_pagamento_agendamento
                               FOREIGN KEY (agendamento_id)
                                   REFERENCES agendamento(id)
                                   ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_agendamento_cliente
    ON agendamento(cliente_id);

CREATE INDEX IF NOT EXISTS idx_agendamento_barbeiro
    ON agendamento(barbeiro_id);

CREATE INDEX IF NOT EXISTS idx_agendamento_data
    ON agendamento(data_agendamento);

CREATE INDEX IF NOT EXISTS idx_servico_barbearia
    ON servico(barbearia_id);

CREATE INDEX IF NOT EXISTS idx_barbeiro_barbearia
    ON barbeiro(barbearia_id);

CREATE INDEX IF NOT EXISTS idx_disponibilidade_barbeiro
    ON disponibilidade(barbeiro_id);

CREATE INDEX IF NOT EXISTS idx_bloqueio_barbeiro
    ON bloqueio_agenda(barbeiro_id);