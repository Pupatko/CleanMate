-- ============================================================
--  CleanMate – database schema
--  PostgreSQL 15+
--  Run: psql -U cleanmate_user -d cleanmate -f schema.sql
-- ============================================================

-- ── Customers ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    id      VARCHAR(36)  PRIMARY KEY,
    name    VARCHAR(200) NOT NULL,
    email   VARCHAR(200) NOT NULL,
    phone   VARCHAR(50)  NOT NULL,
    notes   TEXT         NOT NULL DEFAULT ''
);

-- ── Employees ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id           VARCHAR(36)  PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(200) NOT NULL,
    phone        VARCHAR(50)  NOT NULL,
    role         VARCHAR(50)  NOT NULL DEFAULT 'CLEANER',
    address      VARCHAR(300) NOT NULL DEFAULT '',
    start_date   DATE,
    notes        TEXT         NOT NULL DEFAULT '',
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    availability VARCHAR(100) NOT NULL DEFAULT 'AVAILABLE',
    password     VARCHAR(200) NOT NULL DEFAULT 'cleanmate'
);

-- ── Apartments ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS apartments (
    id            VARCHAR(36)   PRIMARY KEY,
    address       VARCHAR(300)  NOT NULL,
    customer_id   VARCHAR(36)   NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    customer_name VARCHAR(200)  NOT NULL,
    rooms         INT           NOT NULL DEFAULT 1,
    area          NUMERIC(8,2)  NOT NULL DEFAULT 0,
    note          TEXT          NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS apartment_tasks (
    id           SERIAL       PRIMARY KEY,
    apartment_id VARCHAR(36)  NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    task_name    VARCHAR(300) NOT NULL,
    position     INT          NOT NULL DEFAULT 0
);

-- ── Cleanings ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cleanings (
    id         VARCHAR(36)  PRIMARY KEY,
    date       DATE         NOT NULL,
    check_out  TIME         NOT NULL,
    check_in   TIME         NOT NULL,
    property   VARCHAR(300) NOT NULL,
    customer   VARCHAR(200) NOT NULL DEFAULT '',
    employee   VARCHAR(200) NOT NULL DEFAULT '',
    status     VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    qc_rating  INT          NOT NULL DEFAULT 0,
    qc_note    TEXT         NOT NULL DEFAULT '',

    CONSTRAINT chk_status CHECK (status IN ('NEW','ASSIGNED','IN_PROGRESS','DONE','CANCELLED')),
    CONSTRAINT chk_qc_rating CHECK (qc_rating BETWEEN 0 AND 5)
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_cleanings_date     ON cleanings(date);
CREATE INDEX IF NOT EXISTS idx_cleanings_employee ON cleanings(employee);
CREATE INDEX IF NOT EXISTS idx_cleanings_customer ON cleanings(customer);
CREATE INDEX IF NOT EXISTS idx_cleanings_status   ON cleanings(status);
CREATE INDEX IF NOT EXISTS idx_apartments_customer ON apartments(customer_id);
CREATE INDEX IF NOT EXISTS idx_apt_tasks_apartment ON apartment_tasks(apartment_id);
