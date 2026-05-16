INSERT INTO roles (nome, descricao)
VALUES
    ('ROLE_SHOP_OWNER', 'Dono de barbearia'),
    ('ROLE_BARBER', 'Barbeiros'),
    ('ROLE_CLIENT', 'Usuarios comuns')
ON CONFLICT (nome) DO NOTHING;
