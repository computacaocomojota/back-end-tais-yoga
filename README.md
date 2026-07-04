# Taís Yoga | Back-End (Spring Boot + MySQL)

Este projeto implementa a API REST para persistência, autenticação JWT e CRUD do projeto **Taís Yoga**, utilizando **Spring Boot 3**, **Spring Security**, **JJWT**, **Spring Data JPA**, **Hibernate Validation** e **MySQL 8**.

## 📋 Pré-requisitos
- **Java 21 ou superior**
- **Docker & Docker Compose** (para subir o MySQL localmente) ou uma instância MySQL na porta 3306

---

## 🚀 Como Executar

### 1️⃣ Subir o Banco de Dados MySQL (via Docker)
Na pasta `back-end`, execute:
```bash
docker compose up -d
```
O banco `taisyoga_db` será criado na porta `3306` com usuário `taisyoga_user` e senha `taisyoga_password`.

### 2️⃣ Executar a Aplicação Spring Boot
Execute via Maven Wrapper incluído no projeto:
```bash
./mvnw spring-boot:run
```
O servidor iniciará na porta **8080**.

> 💡 **Dica:** O projeto possui carga inicial automática (`DataSeeder`) que cria um usuário padrão para login (`admin@taisyoga.com.br` / senha: `123456`) e os 3 agendamentos iniciais caso as tabelas estejam vazias!

---

## 🛠️ Endpoints REST Disponíveis

### 🔐 Autenticação (`/api/auth`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/auth/cadastro` | Cadastra novo usuário e retorna token JWT |
| `POST` | `/api/auth/login` | Autentica usuário com e-mail/senha e retorna token JWT |
| `POST` | `/api/auth/esqueceu-senha` | Solicitação de redefinição de senha |

### 🧘‍♀️ Agendamentos (`/api/agendamentos`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/agendamentos` | Lista todos os agendamentos |
| `GET` | `/api/agendamentos/{id}` | Busca um agendamento pelo ID |
| `POST` | `/api/agendamentos` | Cria e persiste um novo agendamento |
| `PUT` | `/api/agendamentos/{id}` | Atualiza dados ou status de um agendamento |
| `DELETE` | `/api/agendamentos/{id}` | Remove um agendamento do banco de dados |
# back-end-tais-yoga
