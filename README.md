# Task Scheduler Microservices

> A reactive, secure, and scalable task scheduling platform built with Spring Boot, WebFlux, Kafka, Redis, PostgreSQL, MongoDB, and Keycloak.

---

## 📦 Overview

**Task Scheduler** is a microservices-based system for managing, assigning, and notifying users about tasks in real time.
All services are built with **Spring WebFlux**, secured using **Keycloak** (OAuth2 + Google login), and communicate through **Apache Kafka**.

---

## 🏛️ Architecture 
![architecture_diagram_large](https://github.com/user-attachments/assets/d6da84c7-02ed-4271-9808-a07c6b78257c)
### Description:

* **User Service**
  REST API for retrieving information about any user or the currently authenticated user.
  Queries Keycloak to validate tokens and check permissions.

* **Task Service**
  Performs CRUD operations for tasks and assigns them to users.
  Uses reactive PostgreSQL (R2DBC) and Redis cache.
  Produces task events to Kafka (2 topics with 2 partitions each).

* **Notifications Service**
  Kafka consumer using two threads per topic.
  Stores notifications in MongoDB (reactive).
  Sends real-time updates via WebSocket (manual implementation, no STOMP).
  Filters messages based on user UID.
  Secures connections via Keycloak token validation.

---

## 🛠️ Technology Stack

| Component               | Technology                     |
| ----------------------- | ------------------------------ |
| Reactive Framework      | Spring Boot 3.x + WebFlux      |
| Auth & SSO              | Keycloak (OAuth2 + Google)     |
| Data Storage            | PostgreSQL (R2DBC), MongoDB    |
| Caching                 | Redis                          |
| Messaging               | Apache Kafka (2 topics × 2 p.) |
| Real‑time Notifications | WebSocket (manual)             |
| API Docs                | OpenAPI / Swagger              |

---

## 🚀 Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/dywinarpython/task.git
   cd task
   ```

2. **Branch**
   Work on the `master` branch (merge feature branches into `master`).

3. **Environment**

    * Docker & Docker Compose
    * Create Keycloak realm, clients, and roles as defined in `docker-compose.yml`
    * **Important:** create the real `task-service` client in Keycloak and configure roles and mappers for future microservice management (e.g., via Grafana).

4. **Start the services**

   ```bash
   docker compose up -d
   ```

5. **Access**

    * Keycloak Admin UI: `http://localhost:8080`
    * User Service API: `http://localhost:8000/api/users`
    * Task Service API: `http://localhost:8001/api/tasks`
    * Notifications API: `http://localhost:8001/api/notifications`
    * Notifications WebSocket: `ws://localhost:8002/ws/notifications` (token must be passed via `Authorization: Bearer <token>` header)

---

## 🎯 Future Plans

* Frontend client application.
* Monitoring and logging via Victoria Metrics and Grafana.
* Integrate Nginx as API Gateway.
* Centralized configuration via Spring Cloud Config.
* Resource monitoring dashboards (Prometheus, Grafana).

---

# Планировщик задач 

> Реактивная, безопасная и масштабируемая платформа для планирования задач на базе Spring Boot, WebFlux, Kafka, Redis, PostgreSQL, MongoDB и Keycloak.

---

## 📦 Обзор

**Task Scheduler** — система микросервисов для создания, назначения и уведомления пользователей о задачах в реальном времени.
Все сервисы построены на **Spring WebFlux**, защищены **Keycloak** (OAuth2 + вход через Google), и взаимодействуют через **Apache Kafka**.

---

## 🏛️ Архитектура 
![architecture_diagram_large_ru](https://github.com/user-attachments/assets/da9cc7a8-ed5d-4a52-a537-9efb74bcb932)

### Описание:

* **User Service**
  REST API для получения информации о пользователях (любом или текущем).
  Запрашивает Keycloak для проверки прав (валидация OAuth2 токена).

* **Task Service**
  CRUD для задач, назначение задач пользователям.
  Использует реактивный PostgreSQL (R2DBC) и Redis кэш.
  Публикует события в Kafka (2 топика, по 2 партиции).

* **Notifications Service**
  Консьюмер Kafka с двумя потоками на топик.
  Хранит уведомления в MongoDB (реактивный).
  Отправляет уведомления по WebSocket (без STOMP).
  Фильтрует сообщения по UID пользователя.
  Валидирует токены Keycloak.

---

## 🛠️ Технологический стек

| Компонент                      | Технология                       |
| ------------------------------ | -------------------------------- |
| Реактивный фреймворк           | Spring Boot 3.x + WebFlux        |
| Аутентификация и SSO           | Keycloak (OAuth2 + Google)       |
| Хранилища данных               | PostgreSQL (R2DBC), MongoDB      |
| Кэширование                    | Redis                            |
| Очередь сообщений              | Apache Kafka (2 топика × 2 пар.) |
| Уведомления в реальном времени | WebSocket (ручной)               |
| Документация API               | OpenAPI / Swagger                |

---

## 🚀 Запуск

1. **Клонировать репозиторий**

   ```bash
   git clone https://github.com/dywinarpython/task.git
   cd task
   ```

2. **Ветка**
   Использовать `master` (слияние фич-веток в `master`).

3. **Окружение**

    * Docker и Docker Compose
    * Создать реал, клиенты и роли Keycloak согласно `docker-compose.yml`
    * **Важно:** создать реального клиента `task-service` в Keycloak, настроить роли и мапперы для дальнейшего управления микросервисами (например, через Grafana).

4. **Запуск сервисов**

   ```bash
   docker compose up -d
   ```

5. **Доступ**

    * Админка Keycloak: `http://localhost:8080`
    * API User Service: `http://localhost:8000/api/users`
    * API Task Service: `http://localhost:8001/api/tasks`
    * API Notifications: `http://localhost:8001/api/notifications`
    * WebSocket Notifications: `ws://localhost:8002/ws/notifications` (токен передавать в заголовке `Authorization: Bearer <token>`)

---

## 🎯 Планы

* Клиентское приложение.
* Мониторинг и логирование с Victoria Metrics и Grafana.
* Интеграция Nginx как API Gateway.
* Централизованное управление конфигурациями (Spring Cloud Config).
* Мониторинг ресурсов (Prometheus, Grafana).

---
