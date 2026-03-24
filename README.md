# 🚀 Bank Dugongo Auth Service

> Authentication microservice for Bank Dugongo, built with Java & Spring Boot.

---

## ✨ Features
- 📝 User registration
- 🔐 User login with JWT authentication
- ✅ Token validation
- 👤 User info retrieval
- 🗑️ Soft delete for users

## 📦 API Endpoints
See the OpenAPI spec in [`swagger.yaml`](swagger.yaml) for full details.

### Main Endpoints
- `POST /auth` — Register a new user
- `POST /auth/login` — User login (returns JWT)
- `GET /auth/validate` — Validate JWT token
- `GET /auth/me` — Get current user info
- `PATCH /auth` — Update user info
- `DELETE /auth` — Soft delete user

---

## 🗂️ Project Structure
```
src/main/java/com/bank_dugongo/auth_service/
├── AuthApplication.java         # Main Spring Boot app
├── controllers/                 # REST controllers
│   └── AuthController.java
├── dto/                        # Data Transfer Objects
│   ├── AuthResponseDTO.java
│   ├── LoginRequestDTO.java
│   ├── PatchUserRequestDTO.java
│   ├── RegisterRequestDTO.java
│   └── UserInfoDTO.java
├── exceptions/                 # Custom exceptions & handlers
│   ├── GlobalExceptionHandler.java
│   ├── InactiveUserException.java
│   ├── InvalidCredentialsException.java
│   ├── UserAlreadyExistsException.java
│   └── UserNotFoundException.java
├── models/                     # JPA entities (Mongo collections)
│   ├── User.java
│   └── Customer.java
├── repositories/               # Spring Data repositories
│   ├── UserRepository.java
│   └── CustomerRepository.java
├── security/                   # JWT & security config
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── services/                   # Business logic
    └── AuthService.java
```

---

## 🍃 Mongo Collections (Entities)
- **users**
  - `id`: Integer
  - `username`: String (unique)
  - `passwordHash`: String
  - `customerId`: Integer
  - `isActive`: Boolean
  - `lastLogin`: LocalDateTime
  - `createdAt`: LocalDateTime
- **customers**
  - `id`: Integer
  - `age`: Integer
  - `name`: String
  - `lastName`: String
  - `documentType`: Integer
  - `documentNumber`: String (unique)
  - `phone`: String
  - `email`: String (unique)
  - `riskProfile`: String
  - `creditScore`: Integer
  - `incomeFrequency`: String
  - `monthlyIncome`: BigDecimal
  - `createdAt`: LocalDateTime

---

## 🛠️ Dependencies
- `org.springframework.boot:spring-boot-starter-webmvc` — Web API
- `org.springframework.boot:spring-boot-starter-data-jpa` — Data access
- `org.postgresql:postgresql` — PostgreSQL driver
- `org.springframework.boot:spring-boot-starter-security` — Security
- `io.jsonwebtoken:jjwt-api/impl/jackson` — JWT tokens
- `org.springframework.boot:spring-boot-starter-validation` — Validation
- `org.projectlombok:lombok` — Boilerplate reduction
- `me.paulschwarz:spring-dotenv` — .env support
- `org.springframework.boot:spring-boot-starter-webmvc-test` — Web tests
- `org.junit.platform:junit-platform-launcher` — Testing

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Gradle

### Run the Service
```sh
./gradlew bootRun
```

Service runs on port 8080 by default.

### Run Tests
```sh
./gradlew test
```

---

## 📄 License
MIT License
