# Blogplatform

Egy egyszerű blogplatform Spring Boot backenddel, vanilla Bootstrap + jQuery frontenddel,
PostgreSQL adatbázissal és teljes Docker-alapú fejlesztői / CI/CD környezettel (Jenkins).

---

## Tartalomjegyzék

1. [Funkciók](#funkciók)
2. [Architektúra](#architektúra)
3. [Telepítés és indítás](#telepítés-és-indítás)
4. [Konfiguráció](#konfiguráció)
5. [Jenkins CI/CD használata](#jenkins-cicd-használata)
6. [REST API végpontok](#rest-api-végpontok)
7. [Adatbázis séma](#adatbázis-séma)
8. [Fejlesztés (builder konténer)](#fejlesztés-builder-konténer)
9. [Tesztek](#tesztek)
10. [Projekt struktúra](#projekt-struktúra)

---

## Funkciók

- Bejegyzések listázása lapozással és megtekintése
- Kommentek hozzáadása bejegyzésekhez
- Felhasználói regisztráció és bejelentkezés (JWT)
- Saját bejegyzések létrehozása, szerkesztése és törlése
- Reszponzív felület (Bootstrap 5)
- REST API JSON-nal (több mint 2 publikus végpont)
- Spring Data JPA (Hibernate ORM) PostgreSQL fölött
- Unit + integrációs tesztek (JUnit 5, Mockito, MockMvc)
- Konténerizáció: Docker + docker-compose
- CI/CD: Jenkins pipeline GitHub repó polling-gal

## Architektúra

```
┌────────────────────────────────────────────────────────────────────┐
│                          docker-compose                            │
│                                                                    │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌────────────────┐   │
│  │ postgres │   │ builder  │   │ jenkins  │   │ app (blog-app) │   │
│  │  :5432   │   │ JDK 21   │   │ LTS+JDK21│   │  Spring Boot   │   │
│  │          │   │ + Gradle │   │ + Docker │   │  :8080         │   │
│  └────┬─────┘   └────┬─────┘   └────┬─────┘   └────────┬───────┘   │
│       │              │              │                  │           │
│       └──────────────┴──── blognet ─┴──────────────────┘           │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

- **postgres** – PostgreSQL 16, séma az első induláskor a `db/schema.sql`-ből
- **builder** – Java 21 + Gradle, a `/workspace` mappa a repó (interaktív fejlesztéshez)
- **jenkins** – Jenkins LTS, Docker CLI-vel, host docker.sock-on indítja az app konténert
- **app (blog-app)** – a Jenkins által épített és indított Spring Boot JAR

## Telepítés és indítás

### Előfeltételek

- Docker 24+
- Docker Compose v2
- Linux/macOS/Windows (WSL2)

### Indítás

```bash
git clone <a-repó-url>
cd blogplatform

# Teljes környezet felhúzása
docker compose up -d --build
```

Ezzel elindul:

| Szolgáltatás | URL                           |
| ------------ | ----------------------------- |
| Jenkins      | http://localhost:8081         |
| PostgreSQL   | localhost:5432                |
| Builder      | `docker compose exec builder bash` |

Az `app` (blog-app) konténer akkor jön létre, amikor a Jenkins lefuttatja az első
buildet (lásd lent). A sikeres build után az alkalmazás a **http://localhost:8080**-on
érhető el.

### Leállítás

```bash
docker compose down              # konténerek leállítása
docker compose down -v           # adatbázis és Jenkins home is törlődik
```

## Konfiguráció

A konfigurációs értékek környezeti változókkal állíthatók. Hozz létre egy `.env`
fájlt a projekt gyökerében (opcionális):

```env
# Jenkins admin jelszó (default: admin)
JENKINS_ADMIN_PASSWORD=valami-erosebb-jelszo

# A Jenkins ezt a repót klónozza buildkor.
# Default: https://github.com/attila-buczko-6g-technology/gde.git
GIT_REPO_URL=https://github.com/attila-buczko-6g-technology/gde.git
GIT_BRANCH=*/main
```

Az alkalmazás környezeti változói (`docker run` vagy compose):

| Változó       | Default                                       | Leírás                          |
| ------------- | --------------------------------------------- | ------------------------------- |
| `DB_URL`      | `jdbc:postgresql://postgres:5432/blogdb`      | JDBC URL                        |
| `DB_USER`     | `bloguser`                                    | Adatbázis felhasználó           |
| `DB_PASSWORD` | `blogpass`                                    | Adatbázis jelszó                |
| `JWT_SECRET`  | beépített default (cseréld!)                  | JWT aláíró kulcs (min. 32 byte) |
| `SQL_INIT_MODE` | `never`                                     | Spring SQL init mód             |

## Jenkins CI/CD használata

1. Indítsd el a környezetet: `docker compose up -d --build`
2. Nyisd meg: http://localhost:8081 → belépés `admin` / `admin` (vagy `JENKINS_ADMIN_PASSWORD`)
3. A `blogplatform-pipeline` job már létre van hozva (Configuration-as-Code-ból).
4. A pipeline percenként ellenőrzi a GitHub repót. Manuálisan is futtatható: **Build Now**.

### Pipeline lépések

| # | Stage              | Mit csinál                                                       |
| - | ------------------ | ---------------------------------------------------------------- |
| 1 | Checkout           | Legfrissebb commit a megadott GitHub branch-ből                 |
| 2 | Build & Test       | `./gradlew clean test bootJar` egy temurin JDK 21 konténerben    |
| 3 | Docker image build | `blogplatform-app:latest` image építése `docker/app/Dockerfile`-ból |
| 4 | Deploy             | Régi `blog-app` konténer leállítása + új indítása a blognet hálózaton |

A pipeline forrása: [`Jenkinsfile`](Jenkinsfile).
A Jenkins konfigurációja: [`docker/jenkins/casc/jenkins.yaml`](docker/jenkins/casc/jenkins.yaml).

### GitHub repó használata

A Jenkins default-ként a [https://github.com/attila-buczko-6g-technology/gde.git](https://github.com/attila-buczko-6g-technology/gde.git) repót
klónozza a `main` branch-ről. A repó URL-jét és a branch-et a `.env`-ben
(vagy compose env-ben) felülírhatod a `GIT_REPO_URL` és `GIT_BRANCH` változókkal.

## REST API végpontok

Alap URL: `http://localhost:8080`

Az autentikációt igénylő hívásoknál a JWT token-t küldd:

```
Authorization: Bearer <token>
```

### Autentikáció

| Metódus | Útvonal              | Auth | Leírás                                |
| ------- | -------------------- | ---- | ------------------------------------- |
| POST    | `/api/auth/register` | ❌    | Új felhasználó regisztrációja         |
| POST    | `/api/auth/login`    | ❌    | Bejelentkezés, JWT token kiállítása   |

**Példa – regisztráció:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"anna","email":"anna@example.com","password":"jelszo123"}'
```

Válasz:

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "username": "anna",
  "userId": 2
}
```

### Bejegyzések

| Metódus | Útvonal             | Auth | Leírás                                |
| ------- | ------------------- | ---- | ------------------------------------- |
| GET     | `/api/posts`        | ❌    | Bejegyzések listázása (lapozással)    |
| GET     | `/api/posts/{id}`   | ❌    | Egy bejegyzés részletei               |
| POST    | `/api/posts`        | ✅    | Új bejegyzés létrehozása              |
| PUT     | `/api/posts/{id}`   | ✅    | Saját bejegyzés szerkesztése          |
| DELETE  | `/api/posts/{id}`   | ✅    | Saját bejegyzés törlése               |

**Példa – bejegyzés létrehozása:**

```bash
TOKEN=eyJhbGciOiJIUzM4NCJ9...

curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Első bejegyzés","content":"Helló világ!"}'
```

### Kommentek

| Metódus | Útvonal                          | Auth | Leírás                              |
| ------- | -------------------------------- | ---- | ----------------------------------- |
| GET     | `/api/posts/{postId}/comments`   | ❌    | Egy bejegyzés kommentjei            |
| POST    | `/api/posts/{postId}/comments`   | ✅    | Új komment hozzáadása               |

## Adatbázis séma

Az SQL script: [`db/schema.sql`](db/schema.sql)

| Tábla    | Oszlopok                                                                                  |
| -------- | ----------------------------------------------------------------------------------------- |
| users    | `id` (PK), `username` UQ, `email` UQ, `password_hash`, `created_at`                       |
| posts    | `id` (PK), `title`, `content`, `author_id` FK→users, `created_at`, `updated_at`           |
| comments | `id` (PK), `content`, `post_id` FK→posts, `author_id` FK→users, `created_at`              |

Az alkalmazás `spring.jpa.hibernate.ddl-auto=validate` módban fut éles környezetben, tehát
a séma az SQL scriptből jön létre, a JPA csak validálja.

## Fejlesztés (builder konténer)

Interaktív Gradle/Java fejlesztői környezet:

```bash
# Lépj be a builder konténerbe
docker compose exec builder bash

# A repó automatikusan a /workspace-ben van mountolva
cd /workspace/backend

# Tesztek futtatása
./gradlew test

# JAR build
./gradlew bootJar

# Helyi futtatás (a postgres elérhető a 'postgres' hostnéven)
./gradlew bootRun
```

## Tesztek

A `backend/src/test/java` alatt találhatóak.

- `service/AuthServiceTest` – unit tesztek a regisztrációhoz / belépéshez (Mockito)
- `service/PostServiceTest` – unit tesztek a bejegyzés-kezeléshez
- `controller/PostApiIntegrationTest` – Spring Boot integrációs teszt: regisztráció → bejegyzés → listázás

Futtatás:

```bash
cd backend
./gradlew test
```

A jelentés a `backend/build/reports/tests/test/index.html` fájlban érhető el.

## Projekt struktúra

```
blogplatform/
├── backend/                       Gradle Spring Boot projekt
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle/wrapper/            Gradle Wrapper
│   ├── gradlew, gradlew.bat
│   └── src/
│       ├── main/
│       │   ├── java/hu/blogplatform/
│       │   │   ├── BlogplatformApplication.java
│       │   │   ├── config/SecurityConfig.java
│       │   │   ├── controller/    AuthController, PostController, CommentController
│       │   │   ├── dto/           AuthDtos, PostDtos, CommentDtos
│       │   │   ├── entity/        User, Post, Comment (JPA)
│       │   │   ├── exception/     GlobalExceptionHandler, ApiExceptions
│       │   │   ├── repository/    UserRepository, PostRepository, CommentRepository
│       │   │   ├── security/      JwtService, JwtAuthenticationFilter, CustomUserDetailsService
│       │   │   └── service/       AuthService, PostService, CommentService
│       │   └── resources/
│       │       ├── application.yml
│       │       └── static/        index.html, css/, js/  (Bootstrap 5 + jQuery)
│       └── test/                  JUnit 5 tesztek
├── db/
│   └── schema.sql                 PostgreSQL séma init
├── docker/
│   ├── app/Dockerfile             A runtime image (eclipse-temurin:21-jre)
│   ├── builder/Dockerfile         Fejlesztői build környezet (JDK 21 + Gradle)
│   └── jenkins/
│       ├── Dockerfile             Jenkins LTS + Docker CLI
│       ├── plugins.txt
│       └── casc/jenkins.yaml      Configuration-as-Code
├── docs/                          További dokumentáció
├── Jenkinsfile                    CI/CD pipeline
├── docker-compose.yml
└── README.md
```

---

## Megfelelési mátrix (követelmények)

| Követelmény                                          | Megvalósítás                                              |
| ---------------------------------------------------- | --------------------------------------------------------- |
| Legalább 2 API végpont                               | 9 publikus végpont az `AuthController`, `PostController`, `CommentController` osztályokban |
| Kliensoldal reszponzív                               | Bootstrap 5 grid + saját responsive CSS                   |
| Relációs adattárolás                                 | PostgreSQL 16 + JPA/Hibernate                             |
| Legalább 2 unit/integrációs teszt                    | 10 teszt összesen (8 unit + 2 integrációs)                |
| Markdown dokumentáció (architektúra, konfig, végpontok) | Ez a `README.md` + `docs/`                              |
| Konténerizáció                                       | Docker + docker-compose, 4 service                        |
| Autentikáció                                         | JWT (jjwt 0.12) + Spring Security                         |
| ORM rendszer                                         | Spring Data JPA / Hibernate                               |
| CI/CD integráció                                     | Jenkins pipeline GitHub polling-gal + Configuration-as-Code |
