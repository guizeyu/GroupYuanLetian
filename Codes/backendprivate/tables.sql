create table user
(
    id       int auto_increment
        primary key,
    username varchar(255)            not null,
    password varchar(255)            not null,
    email    varchar(255) default '' null,
    constraint user_username_uindex
        unique (username)
);