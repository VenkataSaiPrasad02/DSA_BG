create table problems(
    id bigint primary key auto_increment,
    slug varchar(50) not null unique ,
    title varchar(100)not null,
    statement_md mediumtext not null,
    difficulty enum('EASY','MEDIUM','HARD')not null,
    constraints_text text not null ,
    input_format text not null,
    output_format text not null,
    status enum('DRAFT','PUBLISHED','ARCHIVED')not null default 'DRAFT',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp
                     on update current_timestamp
)