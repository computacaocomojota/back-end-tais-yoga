# 🧘‍♀️ Taís Yoga | Back-End API (Spring Boot 3 + MySQL)

Este projeto implementa a API REST segura, reativa e escalável para persistência, autenticação JWT, envio de e-mails institucionais e CRUD de agendamentos da plataforma **Espaço Taís Yoga**.

Construído sob os padrões mais modernos de desenvolvimento em **Java 21** e **Spring Boot 3**, o sistema conta com arquitetura em camadas, injeção de dependências limpa, validação rigorosa de dados e prontidão total para nuvem (**Railway** e **Docker**).

---

## ✨ Destaques de Arquitetura & Segurança

- **Segurança de Nível Empresarial (Spring Security 6 + JWT):**
  - Autenticação *stateless* utilizando JSON Web Tokens (JJWT 0.12.6).
  - Segredo JWT 100% externalizado em variáveis de ambiente, prevenindo vazamentos no código fonte.
  - Endpoints de agendamentos e rotas administrativas protegidas com controle rigoroso de acesso (`.authenticated()`).
  - Geração de códigos OTP de recuperação de senha utilizando criptografia de alta entropia (`java.security.SecureRandom`).
- **Qualidade e Otimização de Transações:**
  - Operações de leitura otimizadas no banco de dados através de `@Transactional(readOnly = true)`.
  - Validações de entrada automáticas com **Hibernate Validator / Bean Validation** (`@Valid`, `@NotBlank`, etc.).
  - Higiene nos logs em produção (dados sensíveis registrados apenas em nível `DEBUG`).
  - Injeção de dependência preferencial via construtores e imutabilidade de serviços.
- **Seed Inteligente de Dados (`DataSeeder`):**
  - Criação automática do usuário administrador e carga de dados iniciais de demonstração caso o banco esteja vazio.

---

## 🛠️ Tecnologias Utilizadas

- **Core:** Java 21 LTS, Spring Boot 3.4.1
- **Segurança & Auth:** Spring Security, JWT (JSON Web Tokens)
- **Banco de Dados & ORM:** MySQL 8.0, Spring Data JPA, Hibernate
- **E-mail:** Spring Boot Starter Mail (JavaMailSender via Gmail SMTP)
- **Containerização & Deploy:** Docker multi-stage build, Railway Cloud (`railway.json`), Docker Compose

---

## 🚀 Como Executar Localmente

### 1️⃣ Pré-requisitos
- **Java 21** instalado (ou execute via Docker).
- **Docker & Docker Compose** (para rodar o banco MySQL local).

### 2️⃣ Subir com Docker Compose (API + Banco) — *Mais Rápido*
Na raiz da pasta `back-end`, execute:
```bash
docker compose up --build
```
O servidor iniciará na porta **8080** conectado automaticamente ao MySQL em container.

### 3️⃣ Subir Apenas o Banco localmente e rodar via Maven
Caso prefira rodar a API direto no seu terminal:
```bash
# Sobe apenas o container do MySQL
docker compose up -d mysql-db

# Roda a aplicação Spring Boot
./mvnw spring-boot:run
```

---

## ☁️ Deploy na Nuvem (Railway)

O projeto está configurado com suporte nativo à nuvem **Railway** (`railway.app`) através de build via Dockerfile multi-stage.

### 📋 Passo a Passo para Publicação na Railway:

1. **Crie um Novo Projeto na Railway:**
   - Conecte o repositório do GitHub.
   - Selecione a pasta `back-end` como diretório raiz do serviço.
   - O Railway lerá automaticamente o arquivo `railway.json` e o `Dockerfile`.

2. **Adicione o Banco de Dados MySQL:**
   - Dentro do mesmo projeto na Railway, adicione um serviço **MySQL**.
   - O Spring Boot está configurado para reconhecer automaticamente as variáveis nativas da Railway (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`).

3. **Configure as Variáveis de Ambiente no Serviço da API:**
   Acesse a aba **Variables** do serviço Spring Boot e adicione:
   ```env
   JWT_SECRET=sua-chave-secreta-super-segura-com-no-minimo-64-caracteres
   MAIL_USERNAME=taisyogacontato@gmail.com
   MAIL_PASSWORD=sua-senha-de-app-do-gmail
   PORT=8080
   ```
   > *(Nota: A variável `PORT` é injetada automaticamente pela Railway e o Spring Boot se adapta a ela através de `server.port=${PORT:8080}` no `application.properties`).*

4. **Pronto!** O build será executado e a API estará online em um domínio HTTPS fornecido pela Railway.

---

## 📡 Endpoints REST Principais

### 🔐 Autenticação (`/api/auth`)
| Método | Endpoint | Descrição | Acesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/cadastro` | Cadastra novo usuário e retorna token JWT | Público |
| `POST` | `/api/auth/login` | Autentica usuário com e-mail/senha | Público |
| `POST` | `/api/auth/esqueceu-senha` | Envia código OTP por e-mail | Público |
| `POST` | `/api/auth/verificar-codigo` | Valida o código OTP gerado | Público |

### 🧘‍♀️ Agendamentos (`/api/agendamentos`)
| Método | Endpoint | Descrição | Acesso |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/agendamentos` | Lista os agendamentos do usuário logado | Autenticado |
| `GET` | `/api/agendamentos/{id}` | Busca um agendamento específico | Autenticado |
| `POST` | `/api/agendamentos` | Cria um novo agendamento de aula | Autenticado |
| `PUT` | `/api/agendamentos/{id}` | Atualiza dados ou status de uma reserva | Autenticado |
| `DELETE` | `/api/agendamentos/{id}` | Cancela e remove um agendamento | Autenticado |

---

## 🔒 Proteção de Código e Segurança no Git

Este repositório possui regras estritas de `.gitignore` e `.dockerignore` para garantir que segredos nunca sejam enviados ao GitHub:
- Arquivos `.env` e chaves privadas são estritamente ignorados.
- Diretórios de cache e build (`target/`, `.mvn/`) são excluídos.
- Diretórios de ferramentas de IA (`.agents/`, `.gemini/`) e logs locais não são versionados.
