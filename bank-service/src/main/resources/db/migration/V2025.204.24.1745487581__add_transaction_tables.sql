create table outbox
(
    delivered                                         boolean      not null,
    last_send_time                                    timestamp(6) with time zone,
    message_key_destination                           bigint       not null,
    message_key_idempotence_key_routing_number        bigint       not null,
    message_body                                      text         not null,
    message_key_idempotence_key_locally_generated_key varchar(255) not null,
    primary key (message_key_destination, message_key_idempotence_key_routing_number,
                 message_key_idempotence_key_locally_generated_key)
);

create table inbox
(
    key_routing_number        bigint       not null,
    key_locally_generated_key varchar(255) not null,
    response_body             text,
    primary key (key_routing_number, key_locally_generated_key)
);

create table active_tx
(
    needed_votes      integer      not null,
    votes_are_yes     boolean      not null,
    votes_cast        integer      not null,
    id_routing_number bigint       not null,
    id_id             varchar(255) not null,
    tx_object         text         not null,
    primary key (id_routing_number, id_id)
);

alter table options
    add column foreign_id_id varchar(255);
alter table options
    add column foreign_id_routing_number bigint;
