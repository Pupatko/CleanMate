-- Migration: add password column to employees
-- Run once against live DB before deploying this version.

ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS password VARCHAR(200) NOT NULL DEFAULT 'cleanmate';
