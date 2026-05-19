# BookingService

Веб-приложение для бронирования аудиторий. Гости могут просматривать список аудиторий, зарегистрированные пользователи - создавать и отменять свои брони, администратор - управлять аудиториями, оборудованием и бронированиями.

## Технологии

- Java 17
- Spring Boot 4
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf
- PostgreSQL
- Maven Wrapper

## Роли пользователей

- Гость: может открыть главную страницу, просматривать список аудиторий и страницы входа/регистрации.
- Пользователь: может войти в систему, создавать бронирования, смотреть свои брони и отменять их.
- Администратор: может управлять аудиториями, оборудованием и бронированиями через разделы `/admin/**`.

## Требования

- JDK 17 или новее
- PostgreSQL 14 или новее
- Windows PowerShell, Git Bash или другой терминал

Проверка Java:

```powershell
java -version
```

## Запуск PostgreSQL

Приложение по умолчанию подключается к базе:

- host: `localhost`
- port: `5432`
- database: `booking_db`
- user: `postgres`
- password: `postgres`

Создайте базу данных:

```sql
CREATE DATABASE booking_db;
```

Если пользователь `postgres` использует другой пароль, передайте его через переменную окружения `DB_PASSWORD`.

## Переменные окружения

Все переменные необязательные, потому что в `application.properties` заданы значения по умолчанию.

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/booking_db` | JDBC URL базы данных |
| `DB_USERNAME` | `postgres` | пользователь PostgreSQL |
| `DB_PASSWORD` | `postgres` | пароль PostgreSQL |
| `JPA_DDL_AUTO` | `update` | режим генерации схемы Hibernate |
| `JPA_SHOW_SQL` | `false` | вывод SQL-запросов в лог |
| `APP_ADMIN_ENABLED` | `true` | создавать администратора при старте |
| `APP_ADMIN_EMAIL` | `admin@example.com` | email администратора |
| `APP_ADMIN_PASSWORD` | `admin12345` | пароль администратора |
| `APP_ADMIN_FIRST_NAME` | `System` | имя администратора |
| `APP_ADMIN_LAST_NAME` | `Admin` | фамилия администратора |
| `APP_ADMIN_PHONE` | пусто | телефон администратора |

Пример запуска с собственными значениями в PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/booking_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:APP_ADMIN_EMAIL="admin@example.com"
$env:APP_ADMIN_PASSWORD="admin12345"
.\mvnw.cmd spring-boot:run
```

## Данные администратора

При первом старте приложение автоматически создает администратора, если `APP_ADMIN_ENABLED=true`.

- email: `admin@example.com`
- password: `admin12345`

Если пользователь с таким email уже существует, сервис добавит ему роль администратора. Пароль существующего пользователя при этом не перезаписывается.

## Запуск приложения

Перейдите в папку Maven-проекта:

```powershell
cd java-team-project
```

Запустите приложение:

```powershell
.\mvnw.cmd spring-boot:run
```

После старта приложение доступно по адресу:

```text
http://localhost:8080
```

Если при запуске появляется ошибка PostgreSQL `SQLState: 28P01` или сообщение о неверном пароле пользователя `postgres`, значит пароль локального PostgreSQL отличается от значения по умолчанию. Укажите актуальные данные через `DB_USERNAME` и `DB_PASSWORD` перед запуском.

Основные страницы:

- `/` - главная страница
- `/rooms` - список аудиторий
- `/register` - регистрация пользователя
- `/login` - вход
- `/bookings/my` - мои бронирования
- `/admin/rooms` - управление аудиториями
- `/admin/equipment` - управление оборудованием
- `/admin/bookings` - управление бронированиями

Публичный API просмотра аудиторий:

- `GET /api/rooms`
- `GET /api/rooms/{id}`

Административные API-операции требуют роль `ADMIN`.

## Тесты

Тесты используют H2 in-memory базу с режимом совместимости PostgreSQL, поэтому локальный PostgreSQL для тестов не нужен.

```powershell
cd java-team-project
.\mvnw.cmd test
```

## Чистый запуск

Перед финальной проверкой можно удалить собранные артефакты и пересобрать проект:

```powershell
cd java-team-project
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Для полностью чистой проверки базы удалите старую базу `booking_db` или создайте новую пустую базу и передайте ее в `DB_URL`.

## Чеклист демонстрации

1. Запустить PostgreSQL и убедиться, что база `booking_db` создана.
2. Запустить приложение командой `.\mvnw.cmd spring-boot:run`.
3. Открыть `http://localhost:8080` и проверить главную страницу.
4. Открыть `/rooms` и показать публичный список аудиторий.
5. Зарегистрировать обычного пользователя через `/register`.
6. Войти обычным пользователем и создать бронь через страницу аудитории или `/bookings/new`.
7. Открыть `/bookings/my` и показать список своих бронирований.
8. Выйти из пользователя.
9. Войти администратором: `admin@example.com / admin12345`.
10. Открыть `/admin/rooms`, создать или отредактировать аудиторию.
11. Открыть `/admin/equipment`, создать или отредактировать оборудование.
12. Открыть `/admin/bookings`, проверить список бронирований и отмену бронирования.
13. Проверить, что обычному пользователю недоступны страницы `/admin/**`.
14. Остановить приложение сочетанием `Ctrl+C`.
