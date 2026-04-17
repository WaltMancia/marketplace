# 🛒 Marketplace Multi-vendor

Plataforma completa de comercio electrónico con arquitectura de microservicios.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)](https://spring.io)
[![React](https://img.shields.io/badge/React-18-blue)](https://reactjs.org)
[![Docker](https://img.shields.io/badge/Docker-ready-blue)](https://docker.com)

## 🌐 Demo en vivo
- **Frontend:** https://marketplace-demo.vercel.app
- **API Gateway:** https://marketplace-gateway.onrender.com

### Usuarios demo
| Email | Contraseña | Rol |
|-------|-----------|-----|
| admin@marketplace.com | admin1234 | Admin |
| vendedor@marketplace.com | vendedor1234 | Vendedor |
| comprador@marketplace.com | comprador1234 | Comprador |

## 🏗️ Arquitectura
Frontend (React) → API Gateway (8080)
├── user-service    (8081) + PostgreSQL
├── product-service (8082) + PostgreSQL + RabbitMQ
├── order-service   (8083) + PostgreSQL + RabbitMQ
└── notification-service (8084) + RabbitMQ + Email

## ⚙️ Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security |
| Gateway | Spring Cloud Gateway |
| ORM | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL 16 (una por servicio) |
| Mensajería | RabbitMQ (Saga Pattern) |
| Testing | JUnit 5, Mockito, TestContainers |
| Frontend | React 18, Vite, Zustand, Tailwind CSS |
| Contenedores | Docker + Docker Compose |

## 🚀 Levantar con Docker

```bash
git clone https://github.com/tu-usuario/marketplace.git
cd marketplace
cp .env.example .env
# Edita .env con tus valores

docker-compose up --build
```

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost |
| API Gateway | http://localhost:8080 |
| RabbitMQ UI | http://localhost:15672 |

## 🔧 Desarrollo local

```bash
# 1. Levanta la infraestructura
docker-compose up -d user-db product-db order-db rabbitmq

# 2. Arranca cada servicio desde IntelliJ o:
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run

# 3. Frontend
cd frontend && npm run dev
```

## 🧪 Tests

```bash
cd user-service && mvn test
cd product-service && mvn test
cd order-service && mvn test
```