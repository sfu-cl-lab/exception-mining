DROP SCHEMA IF EXISTS @database_work@; 
create schema @database_work@;

USE @database_work@;
SET storage_engine=INNODB;

create table 1Nodes as select * from @database_setup@.1Nodes;
create table 2Nodes as select * from @database_setup@.2Nodes;
create table RNodes as select * from @database_setup@.RNodes;
create table PVariables as select * from @database_setup@.PVariables;
create table ForeignKeyColumns as select * from  @database_setup@.ForeignKeyColumns;

create table Groundings like @database_setup@.Groundings; 
insert into Groundings select * from @database_setup@.Groundings;

USE @database_setup@;
