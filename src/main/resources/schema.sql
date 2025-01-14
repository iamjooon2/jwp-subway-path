create table if not exists STATION
(
    id bigint auto_increment not null,
    name varchar(255) not null unique,
    primary key(id)
);

create table if not exists LINE
(
    id bigint auto_increment not null,
    name varchar(255) not null unique,
    color varchar(20) not null,
    primary key(id)
);

create table if not exists SECTION
(
    id bigint auto_increment not null,
    line_id bigint not null,
    source_station_id bigint not null,
    target_station_id bigint not null,
    distance int not null,
    primary key(id),
    foreign key(line_id) references line(id) on delete cascade,
    foreign key(source_station_id) references station(id) on delete cascade,
    foreign key(target_station_id) references station(id) on delete cascade
);
