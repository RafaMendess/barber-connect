# Guia de Configuração do Projeto

Este documento tem como objetivo padronizar a configuração do ambiente de desenvolvimento e orientar todos os integrantes da equipe sobre como trabalhar no projeto.

---

# Tecnologias Utilizadas

* Java 21
* Spring Boot
* PostgreSQL + PostGIS
* Flyway
* Docker / Docker Compose
* PgAdmin
* Maven
* Git / GitHub

---

# Pré-requisitos

Antes de iniciar o projeto, certifique-se de instalar:

## Obrigatórios

* Java JDK 21
* Docker Desktop
* Git
* IDE de preferência (IntelliJ IDEA, VSCode ou Eclipse)

---

# Como Clonar o Projeto

Abra o terminal e execute:

```bash
git clone <URL_DO_REPOSITORIO>
```

Entre na pasta:

```bash
cd nome-do-projeto
```

---

# Como Subir o Banco com Docker Compose

O projeto utiliza Docker para padronizar o banco de dados.

Na raiz do projeto, execute:

```bash
docker-compose up -d
```

Esse comando irá subir:

* PostgreSQL + PostGIS
* PgAdmin

---

# Configuração do Banco

## Banco PostgreSQL

Imagem:

```text
postgis/postgis:15-3.3
```

Configuração:

```text
POSTGRES_DB=barber_db
POSTGRES_USER=user_admin
POSTGRES_PASSWORD=password123
```

Porta:

```text
5432
```

---

## PgAdmin

Imagem:

```text
dpage/pgadmin4:7
```

Acesso:

```text
http://localhost:8888
```

Credenciais:

```text
Email: admin@admin.com
Senha: root
```

---

# Como Conectar o PgAdmin ao Banco

Após acessar o PgAdmin:

1. Clique em "Add New Server"
2. Em "General":

   * Name: BarberConnect

3. Em "Connection":

   * Host: db
   * Port: 5432
   * Database: barber_db
   * Username: user_admin
   * Password: password123

4. Salve a configuração

---

# Como Configurar o Projeto Spring na Máquina

Abra o projeto na IDE.

Verifique se a versão do Java está configurada para Java 21.

---

# Dependências do Flyway

O projeto utiliza Flyway para versionamento e gerenciamento do banco de dados.

Dependência utilizada no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```

---

# O Que é o Flyway

Flyway é uma ferramenta de versionamento de banco de dados.

Ele permite:

* controlar alterações do banco via código
* manter histórico de mudanças
* padronizar ambientes
* evitar diferenças entre bancos de desenvolvedores
* automatizar criação e atualização das tabelas

---

# Configuração do application.yaml

Verifique se o arquivo possui uma configuração semelhante:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/barber_db
    username: user_admin
    password: password123
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate

    show-sql: true

    properties:
      hibernate:
        format_sql: true
```

---

# Importante Sobre o Hibernate

O projeto utiliza:

```yaml
ddl-auto: validate
```

Isso significa que:

* o Hibernate NÃO cria tabelas automaticamente
* o Hibernate apenas valida se as entidades batem com o banco
* toda alteração estrutural do banco deve ser feita via Flyway

---

# Estrutura das Migrations

As migrations do Flyway ficam em:

```text
src/main/resources/db/migration
```

---

# Como Criar uma Migration

Toda alteração no banco deve gerar uma nova migration.

Exemplo:

```text
V1__create_roles_table.sql
V2__create_users_table.sql
V3__add_phone_to_users.sql
```

---

# Padrão de Nome das Migrations

O Flyway utiliza o seguinte padrão:

```text
V<versão>__<descrição>.sql
```

Exemplo:

```text
V1__initial_schema.sql
```

Regras importantes:

* utilizar `V`
* usar número sequencial
* utilizar dois underlines `__`
* descrição separada por underscore `_`

---

# Exemplo de Migration

```sql
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

---

# Como o Flyway Funciona

Ao iniciar a aplicação:

1. O Flyway conecta no banco
2. Verifica quais migrations já foram executadas
3. Executa migrations pendentes
4. Cria/atualiza as tabelas automaticamente
5. O Hibernate valida as entidades

---

# Tabela flyway_schema_history

O Flyway cria automaticamente uma tabela chamada:

```text
flyway_schema_history
```

Ela registra:

* migrations executadas
* versão
* data
* status
* checksum

Essa tabela NÃO deve ser alterada manualmente.

---

# Como Rodar as Migrations

Basta:

1. subir o Docker
2. iniciar o projeto Spring Boot

O Flyway executará automaticamente as migrations pendentes.

---

# Fluxo Correto Para Alterações no Banco

Sempre que modificar entidades ou estrutura do banco:

1. Criar migration nova
2. Adicionar SQL necessário
3. Commitar migration junto da alteração
4. Subir aplicação
5. Flyway executará automaticamente

---

# Exemplo de Fluxo

## Criar entidade User

Criar migration:

```text
V2__create_users_table.sql
```

Adicionar SQL:

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL
);
```

Depois:

* subir aplicação
* Flyway executa migration
* Hibernate valida entidade

---

# Nunca Fazer

## Não utilizar

```yaml
ddl-auto: update
```

Isso pode causar:

* inconsistência entre ambientes
* alterações automáticas inesperadas
* perda de controle do schema

---

## Não alterar tabelas manualmente no PgAdmin

Toda alteração estrutural deve ser feita via migration.

Errado:

* criar tabela manualmente
* adicionar coluna manualmente
* alterar constraint manualmente

Certo:

* criar migration
* versionar alteração
* deixar Flyway aplicar

---

# Como Rodar o Projeto Spring

## Pela IDE

Localize a classe principal do Spring Boot:

```text
NomeProjetoApplication.java
```

Clique em Run.

---

## Pelo Terminal

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw spring-boot:run
```

---

# Como Verificar se a API Está Rodando

Acesse:

```text
http://localhost:8080
```

Caso existam endpoints configurados, testar pelo navegador ou Postman.

---

# Estrutura Recomendada do Projeto

```text
src/main/java
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── config
 ├── exception
```

---

# Como Adicionar Dependências no pom.xml

Caso seja necessário instalar novas bibliotecas, elas devem ser adicionadas dentro do arquivo:

```text
pom.xml
```

Exemplo:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Após salvar o arquivo:

## IntelliJ

* Clique em "Reload Maven Project"

## Terminal

```bash
./mvnw clean install
```

---

# Comandos Básicos do Git

## Verificar status

```bash
git status
```

---

## Atualizar branch local

```bash
git pull
```

---

## Criar nova branch

```bash
git checkout -b nome-da-branch
```

Exemplo:

```bash
git checkout -b feature/create-client-endpoint
```

---

## Trocar de branch

```bash
git checkout nome-da-branch
```

---

## Adicionar alterações

```bash
git add .
```

---

## Criar commit

```bash
git commit -m "Descrição clara da alteração"
```

Exemplo:

```bash
git commit -m "Create client entity and repository"
```

---

## Enviar alterações

```bash
git push origin nome-da-branch
```

---

# Boas Práticas de Git

## Nunca trabalhar diretamente na branch main

Sempre criar branch separada.

Exemplo:

```text
feature/client-endpoint
feature/barbershop-entity
feature/appointment-validation
```

---

## Fazer commits pequenos

Evitar commits muito grandes.

Boa prática:

```text
1 tarefa = 1 commit
```

---

## Nomear commits de forma clara

Bom:

```text
Add barber entity
Create appointment validation
Add CNPJ validation
```

Ruim:

```text
fix
update
coisas
```

---

## Sempre dar pull antes de começar

Antes de iniciar qualquer tarefa:

```bash
git pull
```

---

## Atualizar branch com frequência

Evitar ficar muitos dias sem sincronizar.

---

## Nunca subir arquivos desnecessários

Evitar subir:

* arquivos temporários
* logs
* arquivos de IDE

Verificar sempre o `.gitignore`.

---

# Fluxo Recomendado de Trabalho

1. Atualizar projeto

```bash
git pull
```

2. Criar branch

```bash
git checkout -b feature/nome-da-tarefa
```

3. Desenvolver

4. Criar migration caso altere o banco

```text
Vx__descricao_da_alteracao.sql
```

5. Commitar

```bash
git add .
git commit -m "Descrição da alteração"
```

6. Subir branch

```bash
git push origin nome-da-branch
```

7. Criar Pull Request

---

# Observações Importantes

* Sempre manter o banco rodando antes de iniciar o backend
* Não alterar estrutura do banco sem migration
* Toda mudança estrutural deve passar pelo Flyway
* Evitar modificar entidades sem avisar
* Manter consistência nos nomes
* Priorizar organização e clareza do código

---

# Objetivo do Documento

Padronizar o ambiente e evitar problemas de configuração entre os membros da equipe.

Isso reduz erros, acelera o desenvolvimento e melhora a colaboração no projeto.