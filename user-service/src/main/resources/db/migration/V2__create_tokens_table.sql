create table tokens
(
    id    varchar(255)        not null,
    token varchar(255) unique not null,
    valid boolean             not null,
    primary key (id)
);