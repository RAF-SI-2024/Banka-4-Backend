CREATE TABLE authentication_events
(
    id               UUID         NOT NULL,
    event_id         VARCHAR(255) NOT NULL,
    type             VARCHAR(255) NOT NULL,
    did_authenticate BOOLEAN      NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    client_id        VARCHAR(255),
    employee_id      VARCHAR(255),
    CONSTRAINT pk_authentication_events PRIMARY KEY (id)
);

ALTER TABLE authentication_events
    ADD CONSTRAINT uc_authentication_events_client UNIQUE (client_id);

ALTER TABLE authentication_events
    ADD CONSTRAINT uc_authentication_events_employee UNIQUE (employee_id);

ALTER TABLE authentication_events
    ADD CONSTRAINT uc_authentication_events_eventid UNIQUE (event_id);

ALTER TABLE authentication_events
    ADD CONSTRAINT FK_AUTHENTICATION_EVENTS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE authentication_events
    ADD CONSTRAINT FK_AUTHENTICATION_EVENTS_ON_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employees (id);