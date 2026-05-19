# BookingService

Веб-приложение для бронирования аудиторий. Гости могут просматривать список аудиторий и календарь, зарегистрированные пользователи могут создавать и отменять свои брони, администратор управляет аудиториями, оборудованием и бронированиями.

## Технологии

- Java 17
- Spring Boot 4
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf
- PostgreSQL
- Maven Wrapper

## Требования

- JDK 17 или новее
- PostgreSQL 14 или новее
- Windows PowerShell, Git Bash или другой терминал

Проверка Java:

```powershell
java -version
```

## PostgreSQL

По умолчанию приложение подключается к базе:

- host: `localhost`
- port: `5432`
- database: `booking_db`
- user: `postgres`
- password: значение из `DB_PASSWORD`, если задано, иначе значение из `application.properties`

Создайте базу данных:

```sql
CREATE DATABASE booking_db;
```

## Переменные окружения

Большинство переменных необязательные: в `application.properties` заданы значения по умолчанию.

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/booking_db` | JDBC URL базы данных |
| `DB_USERNAME` | `postgres` | пользователь PostgreSQL |
| `DB_PASSWORD` | значение из `application.properties` | пароль PostgreSQL |
| `JPA_DDL_AUTO` | `update` | режим генерации схемы Hibernate |
| `JPA_SHOW_SQL` | `false` | вывод SQL-запросов в лог |
| `APP_ADMIN_ENABLED` | `true` | создавать или обновлять администратора при старте |
| `APP_ADMIN_EMAIL` | `admin@example.com` | email администратора |
| `APP_ADMIN_PASSWORD` | `admin12345` | пароль администратора |
| `APP_ADMIN_FIRST_NAME` | `System` | имя администратора |
| `APP_ADMIN_LAST_NAME` | `Admin` | фамилия администратора |
| `APP_ADMIN_PHONE` | пусто | телефон администратора |
| `APP_SEED_ENABLED` | `true` | создавать демо-аудитории и оборудование при старте |

Пример запуска с собственными значениями:

```powershell
cd java-team-project
$env:DB_URL="jdbc:postgresql://localhost:5432/booking_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:APP_ADMIN_EMAIL="admin@example.com"
$env:APP_ADMIN_PASSWORD="admin12345"
$env:APP_SEED_ENABLED="true"
.\mvnw.cmd spring-boot:run
```

## Демо-данные

При старте `DemoDataInitializer` создаёт базовый набор оборудования и аудиторий, если `APP_SEED_ENABLED=true`.

Создаются:

- оборудование: `Projector`, `Whiteboard`, `Video conference`, `Computers`;
- аудитории: `101`, `203`, `305`, `410`.

Если запись уже существует, инициализатор не создаёт дубль. Для чистой базы это удобно для демонстрации. Для ручной проверки без демо-данных запустите приложение так:

```powershell
$env:APP_SEED_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

## Google OAuth

Google OAuth вынесен в профиль `google` и файл `src/main/resources/application-google.properties`.

Чтобы включить вход через Google:

1. Создайте OAuth Client в Google Cloud Console.
2. Добавьте redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

3. Запустите приложение с профилем `google` и переменными клиента:

```powershell
cd java-team-project
$env:SPRING_PROFILES_ACTIVE="google"
$env:GOOGLE_CLIENT_ID="your-google-client-id"
$env:GOOGLE_CLIENT_SECRET="your-google-client-secret"
.\mvnw.cmd spring-boot:run
```

При первом входе через Google приложение создаёт пользователя по email, выдаёт ему роль `USER` и заполняет профиль именем и фамилией из Google-аккаунта. Если пользователь с таким email уже есть, приложение использует существующую запись и добавляет роль `USER`, если её не было.

## Данные администратора

При старте приложение автоматически создаёт администратора, если `APP_ADMIN_ENABLED=true`.

- email: `admin@example.com`
- password: `admin12345`

Если пользователь с таким email уже существует, сервис добавит ему роль администратора. Пароль существующего пользователя при этом не перезаписывается.

## Статусы брони

Сейчас используются статусы:

| Статус | Значение |
| --- | --- |
| `APPROVED` | активная подтверждённая бронь |
| `CANCELED` | бронь отменена пользователем или администратором |
| `REJECTED` | зарезервирован для сценариев отклонения, в текущем UI почти не используется |

Новая бронь сразу создаётся со статусом `APPROVED`. Отдельного статуса ожидания подтверждения нет.

Для проверки занятости аудитории учитываются только брони со статусом `APPROVED`. Это значит:

- `APPROVED` блокирует аудиторию на выбранный интервал;
- `CANCELED` и `REJECTED` не блокируют аудиторию;
- календарь показывает только активные занятые интервалы, то есть `APPROVED`;
- пользователь и администратор могут отменить только активную бронь со статусом `APPROVED`;
- при отмене администратором требуется комментарий администратора.

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

Основные страницы:

- `/` - главная страница
- `/rooms` - список аудиторий
- `/calendar` - календарь бронирований
- `/register` - регистрация пользователя
- `/login` - вход
- `/bookings/my` - мои бронирования
- `/admin/rooms` - управление аудиториями
- `/admin/equipment` - управление оборудованием
- `/admin/bookings` - управление бронированиями

Публичный API:

- `GET /api/rooms`
- `GET /api/rooms/{id}`
- `GET /api/calendar/bookings`

Административные API-операции требуют роль `ADMIN`.

## Ошибки

Для HTML-страниц настроены шаблоны:

- `templates/error/404.html`
- `templates/error/500.html`

Для REST/AJAX-запросов ошибки возвращаются в JSON-формате:

```json
{
  "success": false,
  "message": "Описание ошибки"
}
```

## Тесты

Тесты используют H2 in-memory базу с режимом совместимости PostgreSQL, поэтому локальный PostgreSQL для тестов не нужен.

```powershell
cd java-team-project
.\mvnw.cmd test
```

## Чеклист демонстрации

1. Запустить PostgreSQL и убедиться, что база `booking_db` создана.
2. Запустить приложение командой `.\mvnw.cmd spring-boot:run`.
3. Открыть `http://localhost:8080`.
4. Открыть `/rooms` и проверить публичный список аудиторий.
5. Открыть `/calendar` и проверить календарь активных броней.
6. Зарегистрировать обычного пользователя через `/register`.
7. Войти обычным пользователем и создать бронь через `/bookings/new`.
8. Открыть `/bookings/my` и проверить список своих бронирований.
9. Отменить свою активную бронь.
10. Войти администратором: `admin@example.com / admin12345`.
11. Открыть `/admin/rooms`, создать или отредактировать аудиторию.
12. Открыть `/admin/equipment`, создать или отредактировать оборудование.
13. Открыть `/admin/bookings`, проверить фильтры и отмену брони администратором с комментарием.
14. Проверить, что обычному пользователю недоступны страницы `/admin/**`.
15. Остановить приложение сочетанием `Ctrl+C`.
