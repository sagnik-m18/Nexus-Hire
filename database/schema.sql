CREATE DATABASE IF NOT EXISTS nexushire;
USE nexushire;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','EMPLOYER','JOB_SEEKER') NOT NULL,
    resume        TEXT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employer_id INT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    company     VARCHAR(150) NOT NULL,
    location    VARCHAR(100) NOT NULL,
    salary      DECIMAL(12,2) NULL,
    description TEXT NULL,
    status      ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    posted_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS applications (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    job_id       INT NOT NULL,
    seeker_id    INT NOT NULL,
    cover_letter TEXT NULL,
    status       ENUM('APPLIED','UNDER_REVIEW','SHORTLISTED','REJECTED','HIRED') NOT NULL DEFAULT 'APPLIED',
    applied_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (job_id, seeker_id),
    FOREIGN KEY (job_id)    REFERENCES jobs(id)  ON DELETE CASCADE,
    FOREIGN KEY (seeker_id) REFERENCES users(id) ON DELETE CASCADE
);
