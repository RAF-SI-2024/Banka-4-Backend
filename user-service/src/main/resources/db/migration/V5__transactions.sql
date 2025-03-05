CREATE TABLE transactions
(
    id                UUID              NOT NULL PRIMARY KEY,
    transaction_number VARCHAR(255)      NOT NULL UNIQUE,
    from_account_id    UUID              NOT NULL,
    to_account_id      UUID              NOT NULL,
    from_amount        DECIMAL(19, 4)    NOT NULL,
    from_currency_id   UUID              NOT NULL,
    to_amount          DECIMAL(19, 4)    NOT NULL,
    to_currency_id     UUID              NOT NULL,
    fee_amount         DECIMAL(19, 4),
    fee_currency_id    UUID,
    recipient          VARCHAR(255)      NOT NULL,
    payment_code       VARCHAR(3),
    reference_number   VARCHAR(50),
    payment_purpose    VARCHAR(500)     NOT NULL,
    payment_date_time  TIMESTAMP         NOT NULL
);

-- Add foreign key constraints
ALTER TABLE transactions
    ADD CONSTRAINT fk_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_from_currency FOREIGN KEY (from_currency_id) REFERENCES currencies(id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_to_currency FOREIGN KEY (to_currency_id) REFERENCES currencies(id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_fee_currency FOREIGN KEY (fee_currency_id) REFERENCES currencies(id);

