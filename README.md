# Limits Service (homework9)

Микросервис лимитов на Spring Boot 3.2 с интеграцией Apache Kafka и email-уведомлениями.

При каждой операции с лимитом (резерв / списание / отмена) продюсер публикует событие в Kafka-топик `limit-events`, консьюмер читает его и через `NotificationService` (spring-boot-starter-mail) отправляет письмо на почту.

## Стек

- Java 17, Spring Boot 3.2.5
- PostgreSQL + Liquibase (таблицы `limits`, `limit_reservations`)
- Apache Kafka (KRaft, без ZooKeeper) + Kafka UI + Mailpit — через docker-compose
- spring-kafka (продюсер + @KafkaListener консьюмер)
- spring-boot-starter-mail (JavaMailSender)

## Предварительные требования

- Docker (Docker Desktop / Colima) с запущенным движком
- Java 17+
- PostgreSQL на `localhost:5438` (БД `suroegina`, пользователь `suroegina`)

## Запуск Kafka (docker-compose)

```bash
docker compose up -d
docker compose ps        # kafka, kafka-ui, mailpit должны быть Up
```

Порты/интерфейсы:

| Сервис    | Адрес                     | Назначение                          |
|-----------|---------------------------|-------------------------------------|
| Kafka     | `localhost:9092`          | брокер (KRaft, один узел)           |
| Kafka UI  | http://localhost:8081     | визуализация топиков и сообщений    |
| Mailpit   | http://localhost:8025     | локальный просмотр писем (SMTP 1025)|

## Запуск приложения

### Настройка почты (обязательно)

Письма уходят через SMTP Яндекса. Пароль приложения задаётся переменной окружения `MAIL_PASSWORD` (сам пароль в git не хранится):

- IDEA: Run → Edit Configurations → Environment variables → `MAIL_PASSWORD=ваш_пароль_приложения`
- Терминал: `export MAIL_PASSWORD=ваш_пароль_приложения`

Как получить пароль приложения Яндекса: https://id.yandex.ru/security/app-passwords → создать пароль с типом «Почта». Обычный пароль от ящика для SMTP не подходит.

Отправитель/получатель настраиваются в `application.yml` → `app.notification.from` / `app.notification.recipient`.

### Запуск

```bash
# IDEA: открыть LimitServiceApplication → Run
# или терминал:
mvn spring-boot:run
```

## REST API

Базовый путь `/api/v1/limits`.

```bash
# Текущий лимит пользователя
curl http://localhost:8080/api/v1/limits/42

# Резерв лимита (вернёт reservationId)
curl -X POST http://localhost:8080/api/v1/limits/42/reservations \
     -H "Content-Type: application/json" -d '{"amount": 100.00}'

# Подтверждение резерва (списание)
curl -X POST http://localhost:8080/api/v1/limits/reservations/{reservationId}/confirm

# Отмена резерва (возврат лимита; только для статуса RESERVED)
curl -X POST http://localhost:8080/api/v1/limits/reservations/{reservationId}/cancel
```

Отменить можно только резерв в статусе `RESERVED`; у подтверждённого (`CONFIRMED`) будет `409 INVALID_STATE`.

## Как это работает

```
REST-запрос
  └─> LimitService (Postgres: резерв/списание/отмена)
        └─> LimitEventProducer → Kafka топик limit-events (JSON)
              └─> LimitEventConsumer (@KafkaListener)
                    └─> NotificationService → email (JavaMailSender)
```

Каждое событие — это `LimitEvent(reservationId, userId, amount, status, limitValue, timestamp)`. Статусы: `RESERVED`, `CONFIRMED`, `CANCELLED`.

## Проверка результата

- Kafka UI (http://localhost:8081) → топик `limit-events` — JSON-сообщения о каждом событии.
- Письмо приходит на адрес из `app.notification.recipient`.
- Для локальной отладки email без реального SMTP: заменить `spring.mail.*` на `host: localhost`, `port: 1025` (Mailpit) и смотреть письма на http://localhost:8025.
