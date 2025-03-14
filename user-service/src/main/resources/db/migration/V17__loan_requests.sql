create table loan_requests (
    id uuid not null,
    amount decimal not null,
    currency_id UUID,
    employment_status varchar(255) not null,
    employment_period int not null,
    repayment_period int not null,
    purpose_of_loan varchar(255) not null,
    account_id UUID,
    loan_id UUID,
    monthly_income decimal not null,
    type varchar(255) not null,
    interest_type varchar(255) not null,
    contact_phone varchar(255) not null,
    primary key (id)
);

ALTER TABLE loan_requests
    ADD CONSTRAINT FK_LOAN_REQUESTS_ON_CURRENCY FOREIGN KEY (currency_id) REFERENCES currencies (id);
ALTER TABLE loan_requests
    ADD CONSTRAINT FK_LOAN_REQUESTS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);
ALTER TABLE loan_requests
    ADD CONSTRAINT FK_LOAN_REQUESTS_ON_LOAN FOREIGN KEY (loan_id) REFERENCES loans (id);