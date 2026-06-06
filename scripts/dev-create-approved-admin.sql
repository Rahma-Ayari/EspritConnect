-- Dev-only: approved ADMIN that can log in (merge-friendly — run manually in phpMyAdmin).
-- Password for both accounts below: password
-- (BCrypt hash matches the literal string "password")

USE EspritConnect2;

-- VERIFY ADMIN STATUS AND PASSWORD
-- Check if admin exists with correct status:
SELECT id, email, enabled, status, inscription_refusee, role 
FROM users 
WHERE email = 'admin@esprit.tn';

-- If admin exists but needs fixing, run:
UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    enabled = 1,
    inscription_refusee = 0,
    status = 'ACCEPTEE',
    role = 'ADMIN'
WHERE email = 'admin@esprit.tn';

-- Verify column exists (run if getting column errors):
-- ALTER TABLE users ADD COLUMN inscription_refusee bit(1) NOT NULL DEFAULT 0;

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
