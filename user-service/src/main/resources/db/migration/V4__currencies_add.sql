-- V2__Insert_currencies.sql

-- Make sure the extension is installed if you're using gen_random_uuid():
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('12249478-bc1c-4ef5-874e-551253fbf32a',
        'Serbian Dinar',
        'RSD',
        'Official currency of Serbia',
        TRUE,
        'RSD');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('a9f88350-160d-46cb-9071-12c548a69382',
        'Euro',
        '€',
        'Official currency of the eurozone',
        TRUE,
        'EUR');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('8e04bd18-261c-414a-93a7-aa6260c7bbf2',
        'US Dollar',
        '$',
        'Official currency of the United States',
        TRUE,
        'USD');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('70dd2e0c-6a6f-4a01-82c6-cd0697565169',
        'Swiss Franc',
        'CHF',
        'Official currency of Switzerland and Liechtenstein',
        TRUE,
        'CHF');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('8a2cefa3-a41c-4d20-857d-ca6b49dc122f',
        'Japanese Yen',
        '¥',
        'Official currency of Japan',
        TRUE,
        'JPY');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('6bb0e062-4f19-4ee7-87cf-8aafccc7123b',
        'Australian Dollar',
        'A$',
        'Official currency of Australia',
        TRUE,
        'AUD');

INSERT INTO currencies (id, name, symbol, description, active, code)
VALUES ('8264b599-56a1-4e7f-8055-bdf82c48f529',
        'Canadian Dollar',
        'C$',
        'Official currency of Canada',
        TRUE,
        'CAD');
