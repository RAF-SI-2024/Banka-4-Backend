CREATE TABLE user_to_totp_secrets
(
    id          UUID         NOT NULL,
    secret      VARCHAR(255) NOT NULL,
    client_id   UUID,
    employee_id UUID,
    is_active   BOOLEAN      NOT NULL,
    CONSTRAINT pk_user_to_totp_secrets PRIMARY KEY (id)
);

ALTER TABLE user_to_totp_secrets
    ADD CONSTRAINT uc_user_to_totp_secrets_client UNIQUE (client_id);

ALTER TABLE user_to_totp_secrets
    ADD CONSTRAINT uc_user_to_totp_secrets_employee UNIQUE (employee_id);

ALTER TABLE user_to_totp_secrets
    ADD CONSTRAINT uc_user_to_totp_secrets_secret UNIQUE (secret);

ALTER TABLE user_to_totp_secrets
    ADD CONSTRAINT FK_USER_TO_TOTP_SECRETS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE user_to_totp_secrets
    ADD CONSTRAINT FK_USER_TO_TOTP_SECRETS_ON_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employees (id);