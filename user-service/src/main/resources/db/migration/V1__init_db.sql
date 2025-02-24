create table client_account_links
(
    account_link varchar(255),
    client_id    varchar(255) not null
);

create table clients
(
    date_of_birth   date         not null,
    enabled         boolean      not null,
    permission_bits bigint       not null,
    address         varchar(255) not null,
    email           varchar(255) not null unique,
    first_name      varchar(255) not null,
    gender          varchar(255) not null,
    id              varchar(255) not null,
    last_name       varchar(255) not null,
    password        varchar(255) not null,
    phone           varchar(255) not null,
    primary key (id)
);

create table employees
(
    date_of_birth   date         not null,
    enabled         boolean      not null,
    permission_bits bigint       not null,
    address         varchar(255) not null,
    department      varchar(255) not null unique,
    email           varchar(255) not null unique,
    first_name      varchar(255) not null,
    gender          varchar(255) not null,
    id              varchar(255) not null,
    last_name       varchar(255) not null,
    password        varchar(255) not null,
    phone           varchar(255) not null,
    position        varchar(255) not null unique,
    username        varchar(255) not null unique,
    primary key (id)
);

alter table if exists client_account_links
    add constraint FK99b2myy3p0y1ymb74i3ieourm
    foreign key (client_id)
    references clients;