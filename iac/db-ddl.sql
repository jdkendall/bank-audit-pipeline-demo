create table if not exists transactions
(
    id        integer generated always as identity,
    uuid      uuid      not null,
    total     bigint    not null,
    timestamp timestamp not null
);

create table if not exists audits
(
    id              integer generated always as identity,
    uuid            uuid      not null,
    source          varchar   not null,
    flagged         boolean   not null,
    audit_timestamp timestamp not null
);
