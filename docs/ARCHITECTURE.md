# Architektúra

## Rétegek

```
┌─────────────────────────────────────────────────────────────┐
│  Frontend (statikus, a Spring Boot szolgálja ki)           │
│  - Bootstrap 5 (reszponzív)                                 │
│  - jQuery 3.7 (AJAX hívások)                                │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP (JSON)
┌──────────────────────────┴──────────────────────────────────┐
│  Spring Boot 3.2 backend                                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ Controller réteg (REST)                              │   │
│  │ - AuthController, PostController, CommentController  │   │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────┴───────────────────────────┐    │
│  │ Security réteg                                       │   │
│  │ - JwtAuthenticationFilter, JwtService, SecurityConfig│   │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────┴───────────────────────────┐    │
│  │ Service réteg (üzleti logika, @Transactional)        │   │
│  │ - AuthService, PostService, CommentService           │   │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────┴───────────────────────────┐    │
│  │ Repository réteg (Spring Data JPA)                   │   │
│  │ - UserRepository, PostRepository, CommentRepository  │   │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────┴───────────────────────────┐    │
│  │ JPA entitások (Hibernate ORM)                        │   │
│  │ - User, Post, Comment                                │   │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │ JDBC
┌──────────────────────────┴──────────────────────────────────┐
│  PostgreSQL 16                                              │
│  - users, posts, comments                                   │
└─────────────────────────────────────────────────────────────┘
```

## CI/CD folyamat

```
Fejlesztő ──git push──▶ GitHub repó
                              │
                  (SCM polling 1 percenként)
                              │
                              ▼
                        ┌──────────┐
                        │ Jenkins  │
                        └────┬─────┘
                             │
       ┌─────────────────────┼───────────────────────┐
       │                     │                       │
       ▼                     ▼                       ▼
   Checkout            Build + Test            Docker build
                  (./gradlew bootJar)     (blogplatform-app:latest)
                                                  │
                                                  ▼
                                            Régi konténer
                                            leállítása + új
                                            indítása
                                                  │
                                                  ▼
                                    http://localhost:8080
```

## Biztonság

- A jelszavakat BCrypt-tel hash-eljük (`PasswordEncoder` bean).
- A JWT HS384/HS256 aláírású, a kulcs minimum 32 byte (HS256 követelmény).
- A token tartalmazza a `sub` (felhasználónév) és `uid` (felhasználó ID) claim-eket.
- Minden auth-mentes végpont kifejezetten engedélyezve van a `SecurityConfig`-ban.
- A többi végpont autentikációt igényel.
- Stateless session (`SessionCreationPolicy.STATELESS`).

## Tranzakciókezelés

- Az írási műveletek `@Transactional`, az olvasások `@Transactional(readOnly = true)`.
- Az `open-in-view: false` beállítás miatt a tranzakciók a service rétegben záródnak.

## Hibakezelés

- A `GlobalExceptionHandler` egységes JSON választ ad minden kivételre.
- Saját kivételek: `NotFoundException`, `BadRequestException`,
  `UnauthorizedException`, `ForbiddenException`.
- A `MethodArgumentNotValidException` esetén a hibás mezők is visszatérnek.
