# Barber Connect

Sistema de gerenciamento para barbearias focado em organização, automação de processos e melhoria da experiência de clientes e administradores.

---

# Sobre o Projeto

O **Barber Connect** é uma plataforma desenvolvida para modernizar a gestão de barbearias, substituindo controles manuais por um sistema digital integrado.

A aplicação permite o gerenciamento completo de:

* Barbearias
* Usuários
* Barbeiros
* Serviços
* Agendamentos
* Disponibilidade de horários
* Pagamentos
* Dashboards administrativos

O objetivo principal é centralizar todas as operações de uma barbearia em um único sistema, proporcionando maior eficiência operacional, organização de dados e melhor experiência para clientes e gestores.

---

# Problema Resolvido

Muitas barbearias ainda utilizam métodos manuais para controle de agendas, cadastro de clientes e gestão de serviços. Isso pode gerar:

* Perda de informações
* Conflitos de horários
* Falta de organização
* Dificuldade no gerenciamento de profissionais
* Baixa escalabilidade do negócio

O Barber Connect foi criado para resolver essas limitações por meio de uma solução digital robusta e escalável.

---

# Objetivo do Sistema

O sistema foi desenvolvido para permitir o gerenciamento centralizado de barbearias, oferecendo recursos administrativos e operacionais.

Entre os principais objetivos estão:

* Automatizar agendamentos
* Organizar a disponibilidade de barbeiros
* Gerenciar clientes e usuários
* Centralizar serviços e pagamentos
* Facilitar o controle administrativo
* Melhorar a experiência dos usuários

---

# Principais Funcionalidades

## Cadastro de Barbearias

Cada estabelecimento pode possuir informações completas, incluindo:

* Nome
* CNPJ
* Telefone
* Endereço
* Horário de funcionamento
* Localização geográfica
* Foto do estabelecimento
* Status de atividade

---

## Gerenciamento de Barbeiros

O sistema permite cadastrar barbeiros vinculados a uma barbearia, contendo:

* Especialidade
* Descrição profissional
* Status de atividade
* Controle de disponibilidade
* Bloqueio de agenda

---

## Cadastro e Controle de Usuários

Os usuários podem possuir diferentes níveis de acesso através de papéis (roles), permitindo diferenciar:

* Clientes
* Barbeiros
* Administradores

O sistema também implementa autenticação segura e gerenciamento de permissões.

---

## Sistema de Agendamento

O módulo de agendamento permite:

* Marcar horários
* Cancelar ou remarcar atendimentos
* Visualizar histórico
* Gerenciar status de agendamento
* Evitar conflitos de horários

Cada agendamento relaciona:

* Cliente
* Barbeiro
* Serviço
* Data e horário
* Observações

---

## Cadastro de Serviços

Cada barbearia pode cadastrar seus próprios serviços, contendo:

* Nome
* Descrição
* Preço
* Tempo estimado
* Associação com barbeiros

---

## Dashboard de Barbearias

Permite a visualização de:

* Lista de barbearias
* Informações detalhadas
* Filtros por localização
* Serviços disponíveis

---

## Dashboard de Agendamentos

Usuários e administradores podem acessar:

* Histórico de atendimentos
* Agendamentos ativos
* Detalhes dos serviços
* Controle de cancelamentos e remarcações

---

## Controle de Pagamentos

O sistema registra pagamentos vinculados aos agendamentos, incluindo:

* Tipo de pagamento
* Status
* Data de pagamento
* Histórico financeiro

---

# Modelagem do Sistema

O Barber Connect foi estruturado utilizando banco de dados relacional, garantindo integridade e consistência dos dados.

## Principais Entidades

* Usuário
* Roles
* Usuario_Roles
* Barbearia
* Barbeiro
* Serviço
* Barbeiro_Servico
* Agendamento
* Pagamento
* Disponibilidade
* Bloqueio_Agenda

Essa modelagem permite o relacionamento entre profissionais, clientes, serviços e agendas, garantindo flexibilidade e escalabilidade.

---

# Regras de Negócio

O sistema foi construído considerando regras essenciais para garantir consistência operacional.

Algumas das principais regras incluem:

* Um usuário pode possuir múltiplos papéis
* Um barbeiro pertence a apenas uma barbearia
* Um barbeiro pode realizar vários serviços
* Um serviço pode estar associado a vários barbeiros
* Um agendamento pertence a cliente, barbeiro e serviço
* Horários devem respeitar disponibilidade e bloqueios
* Não podem existir conflitos de horários
* Cancelamentos possuem limite mínimo de antecedência
* Emails e CNPJs devem ser únicos

---

# Tecnologias Utilizadas

## Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security
* Spring Validation
* Spring Web
* SpringDoc OpenAPI (Swagger)
* Lombok
* Apache Maven

## Banco de Dados

* PostgreSQL
* PostGIS
* pgAdmin

## Infraestrutura

* Docker
* Docker Compose

## Controle de Versão

* Git
* GitHub

---

# Arquitetura

O projeto foi desenvolvido utilizando arquitetura baseada em API REST, separando responsabilidades entre backend, persistência de dados e infraestrutura.

A aplicação foi planejada para oferecer:

* Escalabilidade
* Segurança
* Organização modular
* Facilidade de manutenção
* Integração futura com aplicações mobile e web

---

# Considerações Finais

O Barber Connect busca oferecer uma solução moderna para o gerenciamento de barbearias, permitindo a digitalização de processos administrativos e operacionais.

Sua estrutura foi projetada para garantir flexibilidade, crescimento futuro e melhor experiência para todos os usuários envolvidos no sistema.

---

# Observação

Este README possui como objetivo apresentar a visão geral do projeto.

As instruções de instalação, configuração e execução da aplicação estão disponíveis em outro documento dedicado à configuração do ambiente.
