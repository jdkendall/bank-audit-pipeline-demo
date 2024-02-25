create table public.transactions
(
    id        integer generated always as identity,
    uuid      uuid      not null,
    total     bigint    not null,
    timestamp timestamp not null
);

alter table public.transactions
    owner to postgresuser;

create table public.audits
(
    id              integer generated always as identity,
    uuid            uuid      not null,
    source          varchar   not null,
    flagged         boolean   not null,
    audit_timestamp timestamp not null
);

alter table public.audits
    owner to postgresuser;

