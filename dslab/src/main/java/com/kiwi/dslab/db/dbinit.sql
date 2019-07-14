create table commodity
(
    id int primary key auto_increment,
    name varchar(18) not null,
    price double not null,
    currency varchar(8) not null,
    inventory int not null
);

create table result
(
    id varchar(36) primary key,
    user_id int not null,
    initiator varchar(8) not null,
    success bool not null,
    paid double not null
);