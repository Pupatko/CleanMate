-- Migration: add password column to employees

ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS password VARCHAR(200) NOT NULL DEFAULT 'cleanmate';
