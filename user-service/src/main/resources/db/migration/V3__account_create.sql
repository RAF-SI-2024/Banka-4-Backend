CREATE TABLE currencies
(
    id          UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    symbol      VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL,
    code        VARCHAR(255),
    CONSTRAINT pk_currencies PRIMARY KEY (id)
);

CREATE TABLE activity_codes
(
    id     UUID         NOT NULL,
    code   VARCHAR(255) NOT NULL,
    sector VARCHAR(255) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    CONSTRAINT pk_activity_codes PRIMARY KEY (id)
);

ALTER TABLE activity_codes
    ADD CONSTRAINT uc_activity_codes_code UNIQUE (code);

CREATE TABLE companies
(
    id                UUID         NOT NULL,
    name              VARCHAR(255) NOT NULL,
    tin               VARCHAR(255) NOT NULL,
    crn               VARCHAR(255) NOT NULL,
    address           VARCHAR(255) NOT NULL,
    activity_code_id  UUID,
    majority_owner_id VARCHAR(255),
    CONSTRAINT pk_companies PRIMARY KEY (id)
);

ALTER TABLE companies
    ADD CONSTRAINT uc_companies_crn UNIQUE (crn);

ALTER TABLE companies
    ADD CONSTRAINT uc_companies_name UNIQUE (name);

ALTER TABLE companies
    ADD CONSTRAINT uc_companies_tin UNIQUE (tin);

ALTER TABLE companies
    ADD CONSTRAINT FK_COMPANIES_ON_ACTIVITYCODE FOREIGN KEY (activity_code_id) REFERENCES activity_codes (id);

ALTER TABLE companies
    ADD CONSTRAINT FK_COMPANIES_ON_MAJORITYOWNER FOREIGN KEY (majority_owner_id) REFERENCES clients (id);

CREATE TABLE accounts
(
    id                  UUID         NOT NULL,
    account_number      VARCHAR(255) NOT NULL,
    balance             DECIMAL,
    available_balance   DECIMAL,
    account_maintenance DECIMAL,
    created_date        date,
    expiration_date     date,
    active              BOOLEAN      NOT NULL,
    account_type        VARCHAR(255),
    daily_limit         DECIMAL,
    monthly_limit       DECIMAL,
    employee_id         VARCHAR(255),
    client_id           VARCHAR(255),
    company_id          UUID,
    currency_id         UUID,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

ALTER TABLE accounts
    ADD CONSTRAINT uc_accounts_accountnumber UNIQUE (account_number);

ALTER TABLE accounts
    ADD CONSTRAINT FK_ACCOUNTS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE accounts
    ADD CONSTRAINT FK_ACCOUNTS_ON_COMPANY FOREIGN KEY (company_id) REFERENCES companies (id);

ALTER TABLE accounts
    ADD CONSTRAINT FK_ACCOUNTS_ON_CURRENCY FOREIGN KEY (currency_id) REFERENCES currencies (id);

ALTER TABLE accounts
    ADD CONSTRAINT FK_ACCOUNTS_ON_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employees (id);

DROP TABLE IF EXISTS client_linked_accounts;

CREATE TABLE client_contacts
(
    client_id  VARCHAR(255) NOT NULL,
    contact_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_client_contacts PRIMARY KEY (client_id, contact_id),
    CONSTRAINT fk_client_contacts_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_client_contacts_contact FOREIGN KEY (contact_id) REFERENCES clients (id)
);