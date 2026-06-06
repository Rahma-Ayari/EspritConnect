-- Dev-only: approved ADMIN that can log in (merge-friendly — run manually in phpMyAdmin).
-- Password for both accounts below: password
-- (BCrypt hash matches the literal string "password")

USE EspritConnect2;

-- Option A — promote an account you already registered via POST /api/auth/register
-- (keeps the password you chose at register)
-- UPDATE users
-- SET enabled = 1,
--     inscription_refusee = 0,
--     status = 'ACCEPTEE',
--     role = 'ADMIN'
-- WHERE email = 'dev.admin@esprit.tn';

-- Option B — fix existing dev.admin row (wrong password or still disabled)
UPDATE users
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    enabled = 1,
    inscription_refusee = 0,
    status = 'ACCEPTEE',
    role = 'ADMIN'
WHERE email = 'dev.admin@esprit.tn';

-- Option C — insert if email does not exist yet
INSERT INTO users (
    id,
    nom,
    email,
    password,
    role,
    enabled,
    inscription_refusee,
    status,
    created_at
)
SELECT
    UUID(),
    'Dev Admin',
    'dev.admin@esprit.tn',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN',
    1,
    0,
    'ACCEPTEE',
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dev.admin@esprit.tn');

-- Alternate email (same password)
INSERT INTO users (
    id,
    nom,
    email,
    password,
    role,
    enabled,
    inscription_refusee,
    status,
    created_at
)
SELECT
    UUID(),
    'Dev Admin',
    'admin@esprit.tn',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN',
    1,
    0,
    'ACCEPTEE',
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@esprit.tn');

-- Login: dev.admin@esprit.tn or admin@esprit.tn / password

-- DEV: approve entreprise #1 for job dashboard (post offers + verification)
UPDATE entreprise
SET valide = 1,
    verification_status = 'VERIFIED'
WHERE id_entreprise = 1;
