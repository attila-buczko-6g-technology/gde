# API specifikáció

Az alkalmazás REST API-jának részletes leírása.

Alap URL: `http://localhost:8080`

Content-Type: `application/json` minden POST/PUT-nál.

JWT token: `Authorization: Bearer <token>` az autentikált végpontoknál.

---

## Autentikáció

### POST /api/auth/register

Új felhasználó regisztrációja.

**Request body:**
```json
{
  "username": "string (3-50 karakter, kötelező, egyedi)",
  "email":    "string (email formátum, kötelező, egyedi)",
  "password": "string (6-100 karakter, kötelező)"
}
```

**Response 200 OK:**
```json
{
  "token":    "eyJhbGciOiJIUzM4NCJ9...",
  "username": "anna",
  "userId":   2
}
```

**Hibák:**
- `400` – validációs hiba vagy foglalt felhasználónév/email

### POST /api/auth/login

Bejelentkezés meglévő felhasználóval.

**Request body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response 200 OK:** azonos, mint a regisztrációnál.

**Hibák:**
- `401` – hibás felhasználónév vagy jelszó

---

## Bejegyzések (Posts)

### GET /api/posts

Bejegyzések listázása, legújabb előre.

**Query paraméterek:**
- `page` (int, default 0) – lap sorszáma
- `size` (int, default 10) – lap mérete

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Példa cím",
      "content": "Példa tartalom...",
      "authorUsername": "admin",
      "authorId": 1,
      "createdAt": "2026-05-15T08:00:00Z",
      "updatedAt": "2026-05-15T08:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### GET /api/posts/{id}

Egy bejegyzés részletei.

**Response 200 OK:** egy `PostResponse` objektum.
**Hiba:** `404` – nem létező azonosító.

### POST /api/posts  (auth)

Új bejegyzés létrehozása.

**Request body:**
```json
{
  "title":   "string (1-200 karakter, kötelező)",
  "content": "string (kötelező)"
}
```

**Response 200 OK:** a létrehozott `PostResponse`.

### PUT /api/posts/{id}  (auth)

Saját bejegyzés szerkesztése. **403** ha nem te vagy a szerző.

### DELETE /api/posts/{id}  (auth)

Saját bejegyzés törlése. **204 No Content** sikeres törléskor.

---

## Kommentek

### GET /api/posts/{postId}/comments

Kommentek listázása egy bejegyzéshez, időrendben.

**Response 200 OK:**
```json
[
  {
    "id": 5,
    "content": "Tetszett a bejegyzés!",
    "authorUsername": "bela",
    "authorId": 3,
    "postId": 1,
    "createdAt": "2026-05-15T09:00:00Z"
  }
]
```

### POST /api/posts/{postId}/comments  (auth)

Új komment hozzáadása.

**Request body:**
```json
{
  "content": "string (1-2000 karakter, kötelező)"
}
```

**Response 200 OK:** a létrehozott `CommentResponse`.

---

## Hibaformátum

Minden hiba egységes JSON formában érkezik:

```json
{
  "timestamp": "2026-05-15T08:00:00Z",
  "status": 404,
  "error":  "Not Found",
  "message": "Bejegyzés nem található: 99"
}
```

Validációs hiba esetén egy `fields` objektum is van benne a hibás mezőkkel.
