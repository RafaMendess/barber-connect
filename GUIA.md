# Guia de Configuração do Projeto

Este documento tem como objetivo padronizar a configuração do ambiente de desenvolvimento e orientar todos os integrantes da equipe sobre como trabalhar no projeto.

---

# Tecnologias Utilizadas

* Java 21
* Spring Boot
* PostgreSQL + PostGIS
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

## Configuração do application.properties

Verifique se o arquivo possui uma configuração semelhante:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/barber_db
spring.datasource.username=user_admin
spring.datasource.password=password123
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

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

4. Commitar

```bash
git add .
git commit -m "Descrição da alteração"
```

5. Subir branch

```bash
git push origin nome-da-branch
```

6. Criar Pull Request

---

# Observações Importantes

* Sempre manter o banco rodando antes de iniciar o backend
* Não alterar estrutura do banco sem alinhar com o grupo
* Evitar modificar entidades sem avisar
* Manter consistência nos nomes
* Priorizar organização e clareza do código

---

# Objetivo do Documento

Padronizar o ambiente e evitar problemas de configuração entre os membros da equipe.

Isso reduz erros, acelera o desenvolvimento e melhora a colaboração no projeto.
