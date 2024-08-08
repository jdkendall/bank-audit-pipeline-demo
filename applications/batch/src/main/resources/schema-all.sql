create table if not exists batch_job_instance
(
    job_instance_id BIGINT       not null primary key,
    version         BIGINT,
    job_name        VARCHAR(100) not null,
    job_key         VARCHAR(32)  not null,
    constraint job_inst_un unique (job_name, job_key)
);

create table if not exists batch_job_execution
(
    job_execution_id BIGINT    not null primary key,
    version          BIGINT,
    job_instance_id  BIGINT    not null,
    create_time      TIMESTAMP not null,
    start_time       TIMESTAMP default null,
    end_time         TIMESTAMP default null,
    status           VARCHAR(10),
    exit_code        VARCHAR(2500),
    exit_message     VARCHAR(2500),
    last_updated     TIMESTAMP,
    constraint job_inst_exec_fk foreign key (job_instance_id)
        references batch_job_instance (job_instance_id)
);

create table if not exists batch_job_execution_params
(
    job_execution_id BIGINT       not null,
    parameter_name   VARCHAR(100) not null,
    parameter_type   VARCHAR(100) not null,
    parameter_value  VARCHAR(2500),
    identifying      CHAR(1)      not null,
    constraint job_exec_params_fk foreign key (job_execution_id)
        references batch_job_execution (job_execution_id)
);

create table if not exists batch_step_execution
(
    step_execution_id  BIGINT       not null primary key,
    version            BIGINT       not null,
    step_name          VARCHAR(100) not null,
    job_execution_id   BIGINT       not null,
    create_time        TIMESTAMP    not null,
    start_time         TIMESTAMP default null,
    end_time           TIMESTAMP default null,
    status             VARCHAR(10),
    commit_count       BIGINT,
    read_count         BIGINT,
    filter_count       BIGINT,
    write_count        BIGINT,
    read_skip_count    BIGINT,
    write_skip_count   BIGINT,
    process_skip_count BIGINT,
    rollback_count     BIGINT,
    exit_code          VARCHAR(2500),
    exit_message       VARCHAR(2500),
    last_updated       TIMESTAMP,
    constraint job_exec_step_fk foreign key (job_execution_id)
        references batch_job_execution (job_execution_id)
);

create table if not exists batch_step_execution_context
(
    step_execution_id  BIGINT        not null primary key,
    short_context      VARCHAR(2500) not null,
    serialized_context TEXT,
    constraint step_exec_ctx_fk foreign key (step_execution_id)
        references batch_step_execution (step_execution_id)
);

create table if not exists batch_job_execution_context
(
    job_execution_id   BIGINT        not null primary key,
    short_context      VARCHAR(2500) not null,
    serialized_context TEXT,
    constraint job_exec_ctx_fk foreign key (job_execution_id)
        references batch_job_execution (job_execution_id)
);

create sequence if not exists batch_step_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence if not exists batch_job_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence if not exists batch_job_seq maxvalue 9223372036854775807 no cycle;

create table if not exists accounts
(
    id          integer generated always as identity primary key,
    account_num varchar not null,
    routing_num varchar not null,
    constraint account_num_routing_num_unique unique (account_num, routing_num)
);

create table if not exists transactions
(
    id          integer generated always as identity,
    uuid        uuid      not null,
    src_account integer references accounts (id),
    dst_account integer references accounts (id),
    total       bigint    not null,
    timestamp   timestamp not null
);

create table if not exists audits
(
    id              integer generated always as identity,
    uuid            uuid      not null,
    source          varchar   not null,
    flagged         boolean   not null,
    audit_timestamp timestamp not null
);

-- Insert 100 different account/routing number combinations
insert into accounts (account_num, routing_num)
values ('631489275', '987654321'),
       ('742590386', '987654321'),
       ('853601497', '987654321'),
       ('853601497', '123456789'),
       ('810418283', '123456789'),
       ('850281410', '123456789'),
       ('185051832', '123456789'),
       ('604380238', '123456789'),
       ('680346834', '123456789'),
       ('964712508', '987654321'),
       ('075823619', '987654321'),
       ('186934720', '987654321'),
       ('297045831', '987654321'),
       ('308156942', '987654321'),
       ('419267053', '987654321'),
       ('520378164', '987654321'),
       ('631489275', '456789012'),
       ('742590386', '456789012'),
       ('853601497', '456789012'),
       ('964712508', '456789012'),
       ('075823619', '456789012'),
       ('186934720', '456789012'),
       ('297045831', '456789012'),
       ('308156942', '456789012'),
       ('419267053', '456789012'),
       ('520378164', '456789012'),
       ('631489275', '789012345'),
       ('742590386', '789012345'),
       ('853601497', '789012345'),
       ('964712508', '789012345'),
       ('075823619', '789012345'),
       ('186934720', '789012345'),
       ('297045831', '789012345'),
       ('308156942', '789012345')
on conflict (account_num, routing_num) do nothing;
