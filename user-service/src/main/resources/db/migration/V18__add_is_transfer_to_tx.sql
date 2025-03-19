ALTER TABLE transactions
    ADD COLUMN is_transfer boolean NOT NULL DEFAULT false;
