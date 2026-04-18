# Task time tracker API

REST-сервис учёта времени сотрудников по задачам на **Java 17 + Spring Boot 3 + MyBatis + PostgreSQL**.

Реализовано:
- создание задачи;
- получение задачи по ID;
- изменение статуса задачи (`NEW`, `IN_PROGRESS`, `DONE`);
- создание записи о затраченном времени;
- получение записей по сотруднику за период;
- JWT Bearer Authentication;
- валидация входных DTO (Bean Validation);
- централизованная обработка ошибок (`@RestControllerAdvice`);
- OpenAPI/Swagger;
- unit-тесты и интеграционный DAO-тест с Testcontainers.

## Стек

- Java 17
- Spring Boot 3.5
- Spring Security (JWT)
- MyBatis
- PostgreSQL + Flyway
- SpringDoc OpenAPI
- JUnit 5 + Mockito
- Testcontainers

## Запуск

### Вариант 1 : сервис + PostgreSQL в Docker

```bash
docker compose up --build -d
```

После запуска:
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Остановка:

```bash
docker compose down
```

### Вариант 2: PostgreSQL в Docker, сервис локально

```bash
docker compose up -d postgres
mvn spring-boot:run
```

## Аутентификация (JWT)

1. Зарегистрировать пользователя: `POST /api/v1/auth/register`
2. Выполнить логин: `POST /api/v1/auth/login`
3. Передавать токен во все бизнес-эндпойнты:
   - `Authorization: Bearer <token>`

## REST-эндпойнты

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/v1/auth/register` | Регистрация пользователя |
| POST | `/api/v1/auth/login` | Получение JWT |
| POST | `/api/v1/tasks` | Создание задачи |
| GET | `/api/v1/tasks/{id}` | Получение задачи по ID |
| PATCH | `/api/v1/tasks/{id}/status` | Изменение статуса задачи |
| POST | `/api/v1/time-records` | Создание записи времени |
| GET | `/api/v1/time-records?employeeId={id}&from={ISO_DATETIME}&to={ISO_DATETIME}` | Записи сотрудника за период |

## Тестирование

Запуск всех тестов:

```bash
mvn test
```

Что проверяется:
- unit-тесты сервисного слоя (Mockito);
- интеграционный DAO-тест `MapperIntegrationTest` (PostgreSQL через Testcontainers).

Важно про Docker и тесты:
- интеграционный тест использует Testcontainers, поэтому ему нужен доступ к Docker daemon;
- если Docker недоступен, интеграционный тест автоматически будет помечен как `skipped`, unit-тесты всё равно выполнятся;
- для полного прохождения всех тестов убедитесь, что Docker запущен и текущий пользователь имеет к нему доступ.

## Коллекция запросов

Готовая коллекция Postman:

`postman/task-time-tracker.postman_collection.json`

Она покрывает все реализованные REST-эндпойнты.

## Переменные окружения

Поддерживаемые переменные:
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `task_tracker`)
- `DB_USER` (default: `task_user`)
- `DB_PASSWORD` (default: `task_password`)
- `JWT_SECRET` (base64, минимум 256 бит)
- `JWT_EXPIRATION_MS` (default: `3600000`)
