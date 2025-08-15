create table urls
(
    id          bigserial primary key,
    short_code  varchar(10) not null unique,
    long_url    text        not null,
    created_at  timestamp with time zone default current_timestamp,
    click_count bigint
);

create index idx_short_code on urls (short_code);

create extension pg_stat_statements;