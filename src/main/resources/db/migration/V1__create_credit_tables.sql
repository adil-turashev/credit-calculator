CREATE TABLE credit_requests
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name       VARCHAR(255),
    phone           VARCHAR(255),
    principal       DECIMAL(19, 2) NOT NULL,
    months          INTEGER,
    monthly_rate    DECIMAL(19, 10),
    monthly_payment DECIMAL(19, 2),
    total_paid      DECIMAL(19, 2),
    requested_at    DATETIME
);

CREATE TABLE credit_schedule
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id     INTEGER NOT NULL,
    month_number   INTEGER,
    interest       DECIMAL(19, 2),
    principal_part DECIMAL(19, 2),
    payment        DECIMAL(19, 2),
    balance_after  DECIMAL(19, 2),
    CONSTRAINT fk_credit_request FOREIGN KEY (request_id) REFERENCES credit_requests (id) ON DELETE CASCADE
);
ALTER TABLE credit_requests
    ADD COLUMN user_uuid VARCHAR(255);
ALTER TABLE credit_schedule ADD COLUMN user_uuid VARCHAR(255);