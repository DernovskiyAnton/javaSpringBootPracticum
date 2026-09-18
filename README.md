# praktikumSpringBoot

Бэкенд приложения-блога (посты и комментарии) на Spring Boot. Приложение собирается в executable jar и запускается во встроенном сервлет-контейнере (Tomcat).

## Стек

- Java 21
- Spring Boot 4.1 (Web MVC, JDBC, Validation, H2 console)
- Gradle
- H2 (embedded, in-memory)
- JUnit 5, Mockito, Spring Boot Test (`@JdbcTest`, `@WebMvcTest`, `@SpringBootTest`)

## Сборка

```bash
./gradlew build
```

Соберёт исполняемый jar в `build/libs/praktikumSpringBoot-0.0.1-SNAPSHOT.jar`.

## Запуск тестов

```bash
./gradlew test
```

## Запуск приложения

```bash
./gradlew bootRun
```

или через собранный jar:

```bash
java -jar build/libs/praktikumSpringBoot-0.0.1-SNAPSHOT.jar
```

Приложение стартует на `http://localhost:8080`. При старте автоматически создаётся схема БД (`src/main/resources/schema.sql`) и наполняется тестовыми данными.

Консоль H2 доступна на `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:blogdb`, пользователь: `sa`).

## API

### Посты

| Метод | Путь                        | Описание                       |
|-------|-----------------------------|---------------------------------|
| GET   | `/api/posts`                | Список постов (`search`, `pageNumber`, `pageSize`) |
| GET   | `/api/posts/{id}`           | Пост по id                     |
| POST  | `/api/posts`                | Создать пост                   |
| PUT   | `/api/posts/{id}`           | Обновить пост                  |
| DELETE| `/api/posts/{id}`           | Удалить пост                   |
| POST  | `/api/posts/{id}/likes`     | Поставить лайк                 |
| PUT   | `/api/posts/{id}/image`     | Загрузить изображение (multipart, поле `image`) |
| GET   | `/api/posts/{id}/image`     | Получить изображение           |

### Комментарии

| Метод | Путь                                      | Описание                |
|-------|-------------------------------------------|--------------------------|
| GET   | `/api/posts/{postId}/comments`            | Список комментариев к посту |
| GET   | `/api/posts/{postId}/comments/{id}`       | Комментарий по id        |
| POST  | `/api/posts/{postId}/comments`            | Создать комментарий      |
| PUT   | `/api/posts/{postId}/comments/{id}`       | Обновить комментарий     |
| DELETE| `/api/posts/{postId}/comments/{id}`       | Удалить комментарий      |

## Тесты

- `service` — юнит-тесты бизнес-логики (JUnit 5 + Mockito, без Spring-контекста).
- `repository` — DAO-тесты на `@JdbcTest` с общим `@Import` репозиториев (переиспользуемый Spring-контекст для обоих тестовых классов).
- `controller` — MVC-тесты на `@WebMvcTest` с `@MockitoBean` для сервисного слоя.
- `integration` — сквозные интеграционные тесты на `@SpringBootTest(webEnvironment = RANDOM_PORT)`, поднимающие всё приложение целиком и обращающиеся к нему по HTTP.
