-- =====================================================================
-- Blogplatform adatbázis séma (PostgreSQL)
-- =====================================================================
-- Ez a script létrehozza a felhasználók, bejegyzések és kommentek
-- tábláit, valamint a szükséges indexeket és idegen kulcsokat.
-- A szkriptet a PostgreSQL konténer az első indításkor automatikusan
-- végrehajtja a /docker-entrypoint-initdb.d/ mappából.
-- =====================================================================

-- Felhasználók
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);

-- Bejegyzések
CREATE TABLE IF NOT EXISTS posts (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    author_id   BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_posts_author_id  ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

-- Kommentek
CREATE TABLE IF NOT EXISTS comments (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT      NOT NULL,
    post_id     BIGINT    NOT NULL,
    author_id   BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)   REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id    ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id  ON comments(author_id);

-- =====================================================================
-- Példa kezdeti adatok (opcionális, fejlesztéshez)
-- =====================================================================
-- A jelszó-hash a "jelszo123" bcrypt változata.
INSERT INTO users (username, email, password_hash) VALUES
    ('admin', 'admin@blog.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT (username) DO NOTHING;

INSERT INTO posts (title, content, author_id)
SELECT 'Üdvözlünk a blogon!',
       'Ez az első bejegyzés a blogplatformon. Jó olvasást!',
       u.id
FROM users u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Üdvözlünk a blogon!');
