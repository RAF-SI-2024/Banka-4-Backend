DROP TABLE client_contacts;

CREATE TABLE client_contacts
(
    id             UUID         NOT NULL,
    client_id      UUID         NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    nickname       VARCHAR(255) NOT NULL,
    deleted        BOOLEAN      NOT NULL,
    CONSTRAINT pk_client_contacts PRIMARY KEY (id)
);

ALTER TABLE client_contacts
    ADD CONSTRAINT FK_CLIENT_CONTACTS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);