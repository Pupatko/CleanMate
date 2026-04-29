-- Migration: standardise availability values to English enums
-- Run once against the live database after deploying the code changes.

UPDATE employees SET availability = 'AVAILABLE' WHERE availability = 'Dostupný';
UPDATE employees SET availability = 'OFF_DUTY'  WHERE availability = 'Na dovolenke';
UPDATE employees SET availability = 'INACTIVE'  WHERE availability = 'Neaktívny';

-- Also fix the schema default so new rows get the English value
ALTER TABLE employees
    ALTER COLUMN availability SET DEFAULT 'AVAILABLE';
