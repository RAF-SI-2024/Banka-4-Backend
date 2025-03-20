CREATE TABLE exchanges
(
    id               UUID         NOT NULL,
    exchange_name    VARCHAR(255) NOT NULL,
    exchange_acronym VARCHAR(255) NOT NULL,
    exchangemiccode  VARCHAR(255) NOT NULL,
    polity           VARCHAR(255) NOT NULL,
    currency         VARCHAR(255)     NOT NULL,
    time_zone        VARCHAR(255) NOT NULL,
    open_time        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    close_time       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at       date,
    CONSTRAINT pk_exchanges PRIMARY KEY (id)
);

CREATE TABLE forex_pairs
(
    id             UUID     NOT NULL,
    name            VARCHAR(255),
    base_currency  VARCHAR(255) NOT NULL,
    quote_currency VARCHAR(255) NOT NULL,
    liquidity      VARCHAR(255) NOT NULL,
    CONSTRAINT pk_forex_pairs PRIMARY KEY (id)
);

CREATE TABLE futures
(
    id              UUID     NOT NULL,
    name            VARCHAR(255),
    contract_size   BIGINT  NOT NULL,
    contract_unit   VARCHAR(255) NOT NULL,
    settlement_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_futures PRIMARY KEY (id)
);

CREATE TABLE listing_daily_price_info
(
    id          UUID         NOT NULL,
    ticker      VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    exchange_id UUID         NOT NULL,
    date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_price  DECIMAL      NOT NULL,
    ask_high    DECIMAL      NOT NULL,
    big_low     DECIMAL      NOT NULL,
    change      DECIMAL      NOT NULL,
    volume      INTEGER      NOT NULL,
    CONSTRAINT pk_listing_daily_price_info PRIMARY KEY (id)
);

CREATE TABLE listings
(
    id            UUID         NOT NULL,
    stock_id      UUID,
    forex_pair_id UUID,
    future_id     UUID,
    ticker        VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    exchange_id   UUID,
    last_refresh  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bid           DECIMAL      NOT NULL,
    ask           DECIMAL      NOT NULL,
    active        BOOLEAN      NOT NULL,
    CONSTRAINT pk_listings PRIMARY KEY (id)
);

CREATE TABLE stocks
(
    id                 UUID    NOT NULL,
    name               VARCHAR(255),
    outstanding_shares BIGINT NOT NULL,
    dividend_yield     DECIMAL NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_stocks PRIMARY KEY (id)
);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_EXCHANGE FOREIGN KEY (exchange_id) REFERENCES exchanges (id);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_FOREXPAIR FOREIGN KEY (forex_pair_id) REFERENCES forex_pairs (id);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_FUTURE FOREIGN KEY (future_id) REFERENCES futures (id);

ALTER TABLE listings
    ADD CONSTRAINT FK_LISTINGS_ON_STOCK FOREIGN KEY (stock_id) REFERENCES stocks (id);

ALTER TABLE listing_daily_price_info
    ADD CONSTRAINT FK_LISTING_DAILY_PRICE_INFO_ON_EXCHANGE FOREIGN KEY (exchange_id) REFERENCES exchanges (id);

