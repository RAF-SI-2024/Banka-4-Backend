create table interest_rates (
    date_active_from date not null,
    date_active_to date not null,
    fixed_rate numeric(38,2) not null,
    max_amount numeric(38,2) not null,
    min_amount numeric(38,2) not null,
    id uuid not null,
    primary key (id)
);


create table bank_margins (
    margin numeric(38,2),
    id uuid not null,
    type varchar(255) check (type in ('CASH','MORTGAGE','AUTO_LOAN','REFINANCING','STUDENT_LOAN')),
    primary key (id)
);

ALTER TABLE loans RENAME COLUMN interest_rate TO base_interest_rate;
ALTER TABLE loans ADD interest_rate_id UUID;
ALTER TABLE loans ADD CONSTRAINT fk_loans_interest_rate FOREIGN KEY(interest_rate_id) REFERENCES interest_rates (id);
