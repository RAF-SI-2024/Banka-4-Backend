create table asset_ownership
(
    id_user         uuid    not null,
    id_asset_id     uuid    not null,
    private_amount  integer not null,
    public_amount   integer not null,
    reserved_amount integer not null,
    primary key (id_user, id_asset_id)
);

alter table if exists asset_ownership
    add constraint fk_listing_daily_price_info_exchanges
        foreign key (id_asset_id)
            references assets;
