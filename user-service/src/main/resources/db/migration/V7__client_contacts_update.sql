DROP TABLE client_contacts;

CREATE TABLE client_contacts
(
    account_id UUID         NOT NULL,
    client_id  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_client_contacts PRIMARY KEY (account_id, client_id)
);

ALTER TABLE client_contacts
    ADD CONSTRAINT fk_clicon_on_account FOREIGN KEY (account_id) REFERENCES accounts (id);

ALTER TABLE client_contacts
    ADD CONSTRAINT fk_clicon_on_client FOREIGN KEY (client_id) REFERENCES clients (id);
