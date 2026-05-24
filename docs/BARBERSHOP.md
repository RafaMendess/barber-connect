# Barbershop

Este documento resume o que foi implementado na camada de barbearias do BarberConnect.

## Escopo Implementado

- Cadastro de barbearia por usuario autenticado.
- Vinculo da barbearia ao usuario dono.
- Promocao automatica do usuario para `ROLE_SHOP_OWNER` ao criar uma barbearia.
- Listagem de barbearias ativas.
- Busca de barbearia ativa por id.
- Atualizacao parcial de dados da barbearia.
- Exclusao logica de barbearia com `active = false`.
- Autorizacao para update/delete restrita ao dono da barbearia.
- Armazenamento de localizacao como `GEOGRAPHY(POINT, 4326)` usando PostGIS.
- Resposta padronizada com um unico DTO: `BarbershopResponseDto`.
- Mapper dedicado para conversoes DTO, Entity e localizacao.
- Normalizacao centralizada de strings com `StringNormalizer`.

## Entidade

### Barbershop

Representa uma barbearia cadastrada no sistema.

Tabela: `barbearia`

Principais campos:

- `id`: identificador da barbearia.
- `name`: nome da barbearia, mapeado para `nome`.
- `cnpj`: CNPJ da barbearia, unico no banco.
- `phone`: telefone, mapeado para `telefone`.
- `address`: endereco, mapeado para `endereco`.
- `businessHours`: horario de funcionamento, mapeado para `horario_funcionamento`.
- `photoUrl`: URL da foto, mapeado para `foto_url`.
- `location`: ponto geografico PostGIS, mapeado para `localizacao`.
- `active`: controla exclusao logica, mapeado para `ativo`.
- `createAt`: data de criacao, mapeada para `data_criacao`.
- `owner`: usuario dono da barbearia, mapeado por `owner_id`.

## DTOs

### CreateBarbershopRequestDto

Usado no cadastro de uma nova barbearia.

Campos:

- `name`: obrigatorio, maximo 100 caracteres.
- `cnpj`: obrigatorio, maximo 19 caracteres.
- `phone`: opcional, maximo 20 caracteres.
- `address`: obrigatorio, maximo 255 caracteres.
- `businessHours`: opcional, maximo 100 caracteres.
- `photoUrl`: opcional, maximo 255 caracteres.
- `latitude`: obrigatorio, entre `-90.0` e `90.0`.
- `longitude`: obrigatorio, entre `-180.0` e `180.0`.

### UpdateBarbershopRequestDto

Usado para atualizacao parcial.

Todos os campos sao opcionais:

- `name`: maximo 100 caracteres.
- `phone`: maximo 20 caracteres.
- `address`: maximo 255 caracteres.
- `businessHours`: maximo 100 caracteres.
- `photoUrl`: maximo 255 caracteres.
- `latitude`: entre `-90.0` e `90.0`.
- `longitude`: entre `-180.0` e `180.0`.

Regra especifica:

- `latitude` e `longitude` devem ser enviadas juntas.

### BarbershopResponseDto

DTO unico de resposta usado por create, update, busca por id e listagem.

Campos:

- `id`
- `name`
- `cnpj`
- `phone`
- `address`
- `businessHours`
- `photoUrl`
- `latitude`
- `longitude`

## Endpoints

Todos os endpoints exigem usuario autenticado, pois a configuracao global de seguranca protege todas as rotas que nao estao liberadas explicitamente.

Base path:

```http
/barbershop
```

### Criar barbearia

```http
POST /barbershop
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Barbearia Central",
  "cnpj": "12.345.678/0001-90",
  "phone": "(11) 99999-9999",
  "address": "Rua Principal, 123",
  "businessHours": "Seg-Sab 09:00-19:00",
  "photoUrl": "https://example.com/photo.jpg",
  "latitude": -23.55052,
  "longitude": -46.633308
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "name": "Barbearia Central",
  "cnpj": "12.345.678/0001-90",
  "phone": "(11) 99999-9999",
  "address": "Rua Principal, 123",
  "businessHours": "Seg-Sab 09:00-19:00",
  "photoUrl": "https://example.com/photo.jpg",
  "latitude": -23.55052,
  "longitude": -46.633308
}
```

Regras:

- O CNPJ nao pode estar cadastrado.
- O usuario autenticado vira dono da barbearia.
- O usuario recebe a role `ROLE_SHOP_OWNER`.
- A localizacao e persistida como `Point(longitude, latitude)` com SRID `4326`.

### Atualizar barbearia

```http
PATCH /barbershop/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
```

Autorizacao:

- Requer `hasRole('SHOP_OWNER')`.
- O usuario autenticado deve ser o dono da barbearia.

Request:

```json
{
  "name": "Barbearia Central Premium",
  "phone": "(11) 98888-7777",
  "latitude": -23.551,
  "longitude": -46.634
}
```

Resposta `200 OK`:

```json
{
  "id": 1,
  "name": "Barbearia Central Premium",
  "cnpj": "12.345.678/0001-90",
  "phone": "(11) 98888-7777",
  "address": "Rua Principal, 123",
  "businessHours": "Seg-Sab 09:00-19:00",
  "photoUrl": "https://example.com/photo.jpg",
  "latitude": -23.551,
  "longitude": -46.634
}
```

Regras:

- Apenas campos enviados sao alterados.
- `name` e `address`, quando enviados, nao podem ser strings vazias.
- Campos opcionais como `phone`, `businessHours` e `photoUrl` sao convertidos para `null` se vierem vazios.
- `latitude` e `longitude` precisam ser enviadas juntas.
- Barbearias inativas nao podem ser atualizadas.

### Excluir barbearia

```http
DELETE /barbershop/{id}
Authorization: Bearer <access_token>
```

Autorizacao:

- Requer `hasRole('SHOP_OWNER')`.
- O usuario autenticado deve ser o dono da barbearia.

Resposta:

```http
204 No Content
```

Regras:

- A exclusao e logica.
- O registro nao e removido fisicamente do banco.
- O campo `active` e alterado para `false`.
- Barbearias inativas deixam de aparecer nas buscas e listagens.

### Buscar barbearia por id

```http
GET /barbershop/{id}
Authorization: Bearer <access_token>
```

Resposta `200 OK`:

```json
{
  "id": 1,
  "name": "Barbearia Central",
  "cnpj": "12.345.678/0001-90",
  "phone": "(11) 99999-9999",
  "address": "Rua Principal, 123",
  "businessHours": "Seg-Sab 09:00-19:00",
  "photoUrl": "https://example.com/photo.jpg",
  "latitude": -23.55052,
  "longitude": -46.633308
}
```

Regras:

- Retorna apenas barbearias ativas.
- Se a barbearia nao existir ou estiver inativa, retorna `404 Not Found`.

### Listar barbearias

```http
GET /barbershop
Authorization: Bearer <access_token>
```

Resposta `200 OK`:

```json
[
  {
    "id": 1,
    "name": "Barbearia Central",
    "cnpj": "12.345.678/0001-90",
    "phone": "(11) 99999-9999",
    "address": "Rua Principal, 123",
    "businessHours": "Seg-Sab 09:00-19:00",
    "photoUrl": "https://example.com/photo.jpg",
    "latitude": -23.55052,
    "longitude": -46.633308
  }
]
```

Regras:

- Lista apenas barbearias ativas.
- O service usa transacao `readOnly`.

## Repositorio

### BarbershopRepository

Estende `JpaRepository<Barbershop, Long>`.

Metodos customizados:

- `existsByCnpj(String cnpj)`: verifica duplicidade de CNPJ.
- `findByIdAndActiveTrue(Long id)`: busca uma barbearia ativa por id.
- `findAllByActiveTrue()`: lista apenas barbearias ativas.

## Mapper

### BarbershopMapper

Responsavel pelas conversoes da camada de Barbershop.

Metodos principais:

- `toEntity(CreateBarbershopRequestDto dto, User owner)`: cria a entidade `Barbershop` a partir do DTO de criacao.
- `applyUpdate(UpdateBarbershopRequestDto dto, Barbershop barbershop)`: aplica update parcial em uma entidade existente.
- `toResponse(Barbershop barbershop)`: converte entidade para `BarbershopResponseDto`.

Responsabilidades centralizadas no mapper:

- Criacao manual da entidade `Barbershop`.
- Montagem do DTO de resposta.
- Conversao `latitude/longitude` para `Point`.
- Conversao de `Point` para `latitude/longitude`.
- Tratamento de campos opcionais com `trimToNull`.

O mapper nao acessa repositories, nao valida autorizacao e nao executa regras de negocio.

## Util

### StringNormalizer

Centraliza normalizacao simples de strings.

Metodos:

- `trim(String value)`: remove espacos no inicio/fim preservando `null`.
- `trimToNull(String value)`: remove espacos e converte string vazia para `null`.
- `normalizeEmail(String email)`: aplica `trim` e lowercase.

O objetivo e evitar `.trim()` e `.toLowerCase()` espalhados pelos services.

## Regras de Autorizacao

### Criacao

Qualquer usuario autenticado pode criar uma barbearia.

Ao criar:

- A barbearia recebe o usuario autenticado como `owner`.
- O usuario recebe `ROLE_SHOP_OWNER`.

### Atualizacao e exclusao

Exigem:

- Usuario autenticado.
- Role `ROLE_SHOP_OWNER`.
- Usuario autenticado deve ser o `owner` da barbearia.

Se o usuario tiver a role, mas nao for dono da barbearia, o service lanca `AccessDeniedException`, resultando em `403 Forbidden`.

## Tratamento de Erros

Principais respostas esperadas:

- `400 Bad Request`: erro de validacao no body ou path variable invalida.
- `401 Unauthorized`: request sem token valido.
- `403 Forbidden`: usuario autenticado sem permissao para a operacao.
- `404 Not Found`: barbearia inexistente ou inativa.
- `409 Conflict`: regra de negocio violada, como CNPJ duplicado ou latitude sem longitude.

## Banco de Dados e Migrations

Migrations relacionadas:

- `V1__initial_schema.sql`: cria tabela `barbearia` com os campos iniciais.
- `V6__add_owner_in_barbershop.sql`: adiciona `owner_id` e a FK para `usuario(id)`.
- `V7__add_location_column_in_barbeshop.sql`: remove latitude/longitude antigas e adiciona `localizacao GEOGRAPHY(POINT, 4326)`.

Observacoes:

- A aplicacao usa PostGIS para armazenar localizacao.
- No codigo, a coordenada e criada como `Coordinate(longitude, latitude)`.
- Na resposta da API, os valores voltam separados como `latitude` e `longitude`.

## Boas Praticas Aplicadas

- DTO unico de response para reduzir duplicacao.
- DTOs separados para create e update para manter validacoes corretas.
- Controller sem regra de negocio.
- Service com transacoes nos metodos de escrita.
- Consultas com `@Transactional(readOnly = true)`.
- Soft delete para preservar historico.
- Uso de `@AuthenticationPrincipal` para acessar o usuario autenticado.
- Uso de `@PreAuthorize` para proteger operacoes de dono.
- Conversao centralizada entre entidade e DTO no `BarbershopMapper`.
- Conversao centralizada entre latitude/longitude e `Point` no `BarbershopMapper`.
- Normalizacao centralizada de strings no `StringNormalizer`.
