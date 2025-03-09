CREATE TABLE cards
(
    id            UUID         NOT NULL,
    card_number   VARCHAR(255) NOT NULL,
    cvv           VARCHAR(255) NOT NULL,
    card_name     VARCHAR(255) NOT NULL,
    card_type     VARCHAR(255) NOT NULL,
    "limit"       DECIMAL,
    card_status   VARCHAR(255),
    account_id    UUID,
    created_at    date,
    expires_at    date,
    user_id       UUID,
    first_name    OID,
    last_name     OID,
    date_of_birth date,
    email         OID,
    phone_number  OID,
    address       OID,
    gender        VARCHAR(255),
    CONSTRAINT pk_cards PRIMARY KEY (id)
);

ALTER TABLE cards
    ADD CONSTRAINT uc_cards_cardnumber UNIQUE (card_number);

ALTER TABLE cards
    ADD CONSTRAINT FK_CARDS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

CREATE TABLE loans
(
    id                    UUID   NOT NULL,
    loan_number           BIGINT NOT NULL,
    amount                DECIMAL,
    repayment_period      INTEGER,
    agreement_date        date,
    due_date              date,
    monthly_installment   DECIMAL,
    next_installment_date date,
    remaining_debt        DECIMAL,
    interest_rate         DECIMAL,
    account_id            UUID,
    status                VARCHAR(255),
    type                  VARCHAR(255),
    interest_type         VARCHAR(255),
    CONSTRAINT pk_loans PRIMARY KEY (id)
);

ALTER TABLE loans
    ADD CONSTRAINT uc_loans_loannumber UNIQUE (loan_number);

ALTER TABLE loans
    ADD CONSTRAINT FK_LOANS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);

CREATE TABLE loan_installments
(
    id                   UUID NOT NULL,
    loan_id              UUID,
    installment_amount   DECIMAL,
    interest_rate_amount DECIMAL,
    currency             VARCHAR(255),
    expected_due_date    date,
    actual_due_date      date,
    payment_status       VARCHAR(255),
    CONSTRAINT pk_loan_installments PRIMARY KEY (id)
);

ALTER TABLE loan_installments
    ADD CONSTRAINT FK_LOAN_INSTALLMENTS_ON_LOAN FOREIGN KEY (loan_id) REFERENCES loans (id);