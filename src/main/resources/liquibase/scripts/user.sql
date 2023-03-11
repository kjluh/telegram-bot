--liquibase formatted sql

-- changeSet plahovA: 1
create table notification_task (
id integer primary key ,
chatId integer,
message TEXT,
dateAndTime DATETIME
);