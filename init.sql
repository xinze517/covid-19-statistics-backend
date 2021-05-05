create table confirmed_global
(
    id                bigint auto_increment comment '主键' primary key,
    Province_or_State varchar(255) null comment '省份',
    Country_or_Region varchar(255) not null comment '国家',
    date              date         not null comment '日期',
    count             int          not null comment '人数'
);
create table deaths_global
(
    id                bigint auto_increment comment '主键' primary key,
    Province_or_State varchar(255) null comment '省份',
    Country_or_Region varchar(255) not null comment '国家',
    date              date         not null comment '日期',
    count             int          not null comment '人数'
);
create table recovered_global
(
    id                bigint auto_increment comment '主键' primary key,
    Province_or_State varchar(255) not null comment '省份',
    Country_or_Region varchar(255) not null comment '国家',
    date              date         not null comment '日期',
    count             int          not null comment '人数'
);
