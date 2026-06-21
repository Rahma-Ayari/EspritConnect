-- CAPTCHA Hybride Esprit — script SQL (MySQL)
-- Hibernate ddl-auto=update crée aussi les tables automatiquement.
-- Ce script sert de référence / déploiement manuel.

CREATE TABLE IF NOT EXISTS captcha_challenge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captcha_type VARCHAR(20) NOT NULL,
    question VARCHAR(500) NOT NULL,
    images TEXT NOT NULL,
    correct_answers TEXT NOT NULL,
    expiration_date DATETIME NOT NULL,
    solved TINYINT(1) NOT NULL DEFAULT 0,
    attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    verified_token VARCHAR(64) NULL,
    consumed TINYINT(1) NOT NULL DEFAULT 0,
    client_ip VARCHAR(45) NULL,
    INDEX idx_captcha_expiration (expiration_date),
    INDEX idx_captcha_verified_token (verified_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS captcha_attempt_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captcha_id BIGINT NULL,
    client_ip VARCHAR(45) NULL,
    success TINYINT(1) NOT NULL,
    failure_reason VARCHAR(255) NULL,
    captcha_type VARCHAR(20) NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_captcha_log_ip (client_ip),
    INDEX idx_captcha_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
