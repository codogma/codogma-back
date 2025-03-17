# CODOGMA Backend

[![Spring Boot](https://img.shields.io/badge/Spring-Boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Бэкенд-сервис для агрегатора IT-контента с поддержкой статей, open-source
проектов и тематических обсуждений.

## 📌 Основной функционал

- **Публикация контента**: Статьи, проекты, новости с Markdown-поддержкой
- **Поиск и фильтрация**: Полнотекстовый поиск с Hibernate Search + Lucene
- **NLP-анализ**: Автоматическая категоризация контента (OpenNLP)
- **Социальные функции**: Комментарии, лайки, подписки
- **Безопасность**: OAuth2 + JWT аутентификация
- **Реaltime**: Уведомления через WebSocket
- **Email-рассылка**: Подтверждение регистрации, нотификации
- **API документация**: Swagger/OpenAPI 3.0

## 🛠 Технологии и зависимости

- **Core**:
  [Spring Boot 3.4.0](https://spring.io/projects/spring-boot) |
  [Java 21](https://openjdk.org/projects/jdk/21/)

- **Базы данных**:
  [PostgreSQL](https://www.postgresql.org/) |
  [Hibernate ORM](https://hibernate.org/orm/) |
  [Hibernate Search 7.2](https://hibernate.org/search/)

- **Поиск**:
  [Apache Lucene 10](https://lucene.apache.org/) |
  [Lucene Analysis](https://lucene.apache.org/core/10_0_0/analyzers-common/index.html)

- **Безопасность**:
  [Spring Security](https://spring.io/projects/spring-security) |
  [JWT](https://github.com/jwtk/jjwt) 0.12.6

- **NLP**:
  [Apache OpenNLP 2.5.3](https://opennlp.apache.org/)

- **Документация**:
  [SpringDoc OpenAPI 2.7.0](https://springdoc.org/)

- **Утилиты**:
  [Lombok](https://projectlombok.org/) |
  [JSoup](https://jsoup.org/) 1.18.3

Полный список зависимостей: [pom.xml](pom.xml)

## 🚀 Запуск проекта

**Требования**:

- Java 21
- PostgreSQL 15+
- Maven 3.9+

**Шаги**:

1. Клонировать репозиторий:
   ```bash
   git clone https://github.com/codogma/codogma-back.git
   cd codogma-back
   ```

2. Настроить БД (создать базу и пользователя)
3. Создать `.env` файл в корне:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/codogma
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=secret
   JWT_SECRET=your-512-bit-secret
   OAUTH2_CLIENT_ID=your-oauth-client-id
   OAUTH2_CLIENT_SECRET=your-oauth-secret
   SPRING_MAIL_USERNAME=notifications@codogma.com
   SPRING_MAIL_PASSWORD=email-password
   ```
4. Собрать и запустить:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## ⚙ Конфигурация

Основные настройки в `application.yml`:

```yaml
features:
  search:
    index-location: ./lucene-indexes
  nlp:
    model-path: ./nlp-models
```

OAuth2 провайдеры (пример для GitHub):

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${OAUTH2_CLIENT_ID}
            client-secret: ${OAUTH2_CLIENT_SECRET}
            scope: user:email
```

## 📚 API Документация

После запуска доступна по адресу:  
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Примеры эндпоинтов:

- `GET /api/v1/articles?search=spring` – Поиск статей
- `POST /api/v1/comments` – Добавление комментария
- `WS /ws/notifications` – WebSocket для уведомлений

## 📄 Лицензия

Проект распространяется под лицензией [MIT](LICENSE).