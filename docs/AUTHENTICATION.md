# Autenticacao, Usuarios e Roles

Este documento resume o que foi implementado na camada de usuarios, autenticacao e autorizacao do BarberConnect.

## Escopo Implementado

- Cadastro de usuario cliente.
- Login com access token JWT.
- Refresh token com rotacao.
- Logout com revogacao de refresh token.
- Roles persistidas em banco.
- Autorizacao por role com `@PreAuthorize`.
- Endpoint `/auth/me`.
- Confirmacao de email com OTP.
- Reenvio de codigo de verificacao.
- Recuperacao de senha com OTP.
- Revogacao de sessoes apos reset de senha.
- Envio de codigos em modo `dev` via console.
- Envio real em modo `prod` via SMTP.

## Entidades

### User

Representa o usuario da aplicacao.

Principais campos relacionados a auth:

- `email`: usado como username no Spring Security.
- `password`: senha criptografada com BCrypt.
- `active`: controla se a conta esta habilitada.
- `emailVerified`: indica se o email foi confirmado.
- `roles`: permissoes do usuario.

`User` implementa `UserDetails`, permitindo integracao direta com Spring Security.

### Role

Representa os papeis de acesso do sistema.

Roles iniciais:

- `ROLE_CLIENT`
- `ROLE_BARBER`
- `ROLE_SHOP_OWNER`

No Spring Security:

- no banco: `ROLE_CLIENT`
- em `hasRole`: `hasRole('CLIENT')`
- em `hasAuthority`: `hasAuthority('ROLE_CLIENT')`

### RefreshToken

Representa uma sessao renovavel do usuario.

Campos principais:

- `tokenHash`: hash SHA-256 do refresh token.
- `user`: usuario dono do token.
- `expiresAt`: data de expiracao.
- `revokedAt`: data de revogacao.
- `createdAt`: data de criacao.

O refresh token puro e retornado ao cliente apenas uma vez. No banco fica salvo somente o hash.

### UserOtp

Representa codigos temporarios de uso unico.

Usado para:

- confirmacao de email;
- recuperacao de senha.

Campos principais:

- `codeHash`: hash SHA-256 do codigo OTP.
- `purpose`: finalidade do codigo.
- `user`: usuario dono do codigo.
- `expiresAt`: data de expiracao.
- `consumedAt`: data em que foi usado ou invalidado.
- `attempts`: quantidade de tentativas.

Ao gerar um novo OTP para o mesmo usuario e mesma finalidade, os OTPs antigos sao consumidos/invalidados.

## Enums

### OtpPurpose

Define a finalidade do OTP:

```java
EMAIL_VERIFICATION
PASSWORD_RESET
```

## Fluxos

### Registro

Endpoint:

```http
POST /auth/register
```

Fluxo:

1. Normaliza o email com `trim()` e lowercase.
2. Verifica se o email ja existe.
3. Criptografa senha com BCrypt.
4. Atribui `ROLE_CLIENT`.
5. Cria usuario com email ainda nao verificado.
6. Gera OTP de verificacao de email.
7. Envia o codigo via `EmailService`.

Em `dev`, o codigo aparece no terminal.

### Verificacao de Email

Endpoint:

```http
POST /auth/verify-email
```

Body:

```json
{
  "email": "user@email.com",
  "code": "123456"
}
```

Fluxo:

1. Busca o usuario pelo email normalizado.
2. Busca o OTP ativo mais recente para `EMAIL_VERIFICATION`.
3. Valida expiracao, consumo e limite de tentativas.
4. Compara hash do codigo recebido.
5. Marca OTP como consumido.
6. Marca `emailVerified = true`.

### Reenvio de Codigo de Verificacao

Endpoint:

```http
POST /auth/resend-verification-code
```

Body:

```json
{
  "email": "user@email.com"
}
```

Fluxo:

1. Busca o usuario pelo email.
2. Se o usuario existir e ainda nao estiver verificado, gera novo OTP.
3. OTPs antigos de verificacao sao invalidados.
4. Novo codigo e enviado pelo `EmailService`.

A resposta e sempre `204 No Content`, para nao expor se o email existe.

### Login

Endpoint:

```http
POST /auth/login
```

Fluxo:

1. Busca usuario pelo email normalizado.
2. Valida senha com BCrypt.
3. Bloqueia login se `emailVerified = false`.
4. Gera access token JWT.
5. Gera refresh token.
6. Retorna ambos ao cliente.

Resposta:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

### Refresh Token

Endpoint:

```http
POST /auth/refresh
```

Body:

```json
{
  "refreshToken": "..."
}
```

Fluxo:

1. Calcula hash SHA-256 do refresh token recebido.
2. Busca o token no banco pelo hash.
3. Valida se nao expirou e nao foi revogado.
4. Revoga o refresh token antigo.
5. Gera novo access token.
6. Gera novo refresh token.
7. Retorna os novos tokens.

Esse modelo usa rotacao de refresh token. Um refresh token usado nao deve ser reutilizado.

### Logout

Endpoint:

```http
POST /auth/logout
```

Body:

```json
{
  "refreshToken": "..."
}
```

Fluxo:

1. Calcula hash do refresh token.
2. Busca o token.
3. Se existir, marca `revokedAt`.

O access token atual continua valido ate expirar, mas nao sera possivel renovar a sessao com o refresh token revogado.

### Usuario Atual

Endpoint:

```http
GET /auth/me
```

Requer access token valido no header:

```http
Authorization: Bearer <accessToken>
```

Retorna dados basicos do usuario autenticado e suas roles.

### Recuperacao de Senha

Endpoint para solicitar codigo:

```http
POST /auth/forgot-password
```

Body:

```json
{
  "email": "user@email.com"
}
```

Fluxo:

1. Normaliza o email.
2. Se o usuario existir, gera OTP com finalidade `PASSWORD_RESET`.
3. Invalida OTPs antigos da mesma finalidade.
4. Envia codigo pelo `EmailService`.
5. Retorna sempre `204 No Content`.

Endpoint para redefinir senha:

```http
POST /auth/reset-password
```

Body:

```json
{
  "email": "user@email.com",
  "code": "123456",
  "newPassword": "novaSenha123"
}
```

Fluxo:

1. Busca usuario.
2. Valida OTP de `PASSWORD_RESET`.
3. Criptografa nova senha com BCrypt.
4. Revoga todos os refresh tokens ativos do usuario.

Revogar os refresh tokens apos reset de senha derruba sessoes antigas.

## Autorizacao

O `SecurityConfig` define regras globais:

- endpoints publicos de auth;
- endpoints publicos do Swagger;
- qualquer outro endpoint exige autenticacao.

Regras especificas de role devem ficar nos controllers ou services com `@PreAuthorize`.

Exemplos:

```java
@PreAuthorize("hasRole('CLIENT')")
```

```java
@PreAuthorize("hasAnyRole('BARBER', 'SHOP_OWNER')")
```

## Configuracoes

### JWT

```yaml
jwt:
  secret: ${JWT_SECRET:12345678901234567890123456789012}
  expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```

- `JWT_SECRET`: segredo usado para assinar JWT.
- Deve ter no minimo 32 bytes.
- O fallback atual e apenas para desenvolvimento local.

### Refresh Token

```yaml
refresh-token:
  expiration-days: ${REFRESH_TOKEN_EXPIRATION_DAYS:7}
```

Define por quantos dias o refresh token e valido.

### OTP

```yaml
otp:
  expiration-minutes: ${OTP_EXPIRATION_MINUTES:10}
  max-attempts: ${OTP_MAX_ATTEMPTS:5}
```

- `OTP_EXPIRATION_MINUTES`: tempo de validade do codigo.
- `OTP_MAX_ATTEMPTS`: quantidade maxima de tentativas.

### Profiles

```yaml
spring:
  profiles:
    active: dev
```

Profiles usados:

- `dev`: imprime codigos no terminal.
- `prod`: envia email real via SMTP.

Em producao, sobrescrever com variavel de ambiente:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
```

## Email

### Dev

`ConsoleEmailService` e usado com:

```java
@Profile("dev")
```

Ele imprime os codigos no terminal.

### Prod

`SmtpEmailService` e usado com:

```java
@Profile("prod")
```

Ele usa `JavaMailSender` do Spring Mail.

Configuracoes:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${MAIL_SMTP_AUTH:false}
          starttls:
            enable: ${MAIL_SMTP_STARTTLS_ENABLE:false}

app:
  mail:
    from: ${MAIL_FROM:no-reply@barberconnect.local}
```

Exemplo de variaveis para SMTP:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="seu-email@gmail.com"
$env:MAIL_PASSWORD="senha-de-app"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS_ENABLE="true"
$env:MAIL_FROM="seu-email@gmail.com"
```

## Migrations

Migrations relacionadas:

- `V1__initial_schema.sql`: schema inicial.
- `V2__seed_roles.sql`: seed das roles iniciais.
- `V3__unique_user_email_lower.sql`: unicidade de email ignorando maiusculas/minusculas.
- `V4__create_refresh_tokens.sql`: tabela de refresh tokens.
- `V5__create_user_otps.sql`: tabela de OTPs e campo de email verificado.

## Seguranca

Pontos ja cobertos:

- Senhas com BCrypt.
- JWT assinado com segredo configuravel.
- Refresh token salvo como hash.
- OTP salvo como hash.
- Rotacao de refresh token.
- Revogacao de refresh token no logout.
- Revogacao de refresh tokens apos reset de senha.
- Bloqueio de login para email nao verificado.
- Respostas neutras em fluxos sensiveis como forgot password e resend verification.
- Configuracoes sensiveis via variaveis de ambiente.

## Melhorias Futuras

Itens que podem ser adicionados depois, sem bloquear os proximos dominios:

- Rate limit em login, OTP e reset de senha.
- Testes automatizados para fluxos de auth.
- Templates HTML para emails.
- Logs/auditoria de eventos sensiveis.
- Separar `application-dev.yaml` e `application-prod.yaml`.
- Cookies HttpOnly para refresh token se houver frontend web.
- Politica de senha mais forte.
