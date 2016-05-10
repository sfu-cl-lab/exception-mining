
SET storage_engine=INNODB;





create table 1Nodes as select * from Premier_League_MidFielder_Dec2015_setup.1Nodes;
create table 2Nodes as select * from Premier_League_MidFielder_Dec2015_setup.2Nodes;
create table RNodes as select * from Premier_League_MidFielder_Dec2015_setup.RNodes;
create table PVariables as select * from Premier_League_MidFielder_Dec2015_setup.PVariables;
create table EntityTables as select * from Premier_League_MidFielder_Dec2015_setup.EntityTables;
create table AttributeColumns as select * from Premier_League_MidFielder_Dec2015_setup.AttributeColumns;
create table TernaryRelations as select * from Premier_League_MidFielder_Dec2015_setup.TernaryRelations;
create table RelationTables as select * from Premier_League_MidFielder_Dec2015_setup.RelationTables;
create table NoPKeys as select * from  Premier_League_MidFielder_Dec2015_setup.NoPKeys;
create table ForeignKeyColumns as select * from  Premier_League_MidFielder_Dec2015_setup.ForeignKeyColumns;
create table ForeignKeys_pvars as select * from  Premier_League_MidFielder_Dec2015_setup.ForeignKeys_pvars;
create table InputColumns as select * from  Premier_League_MidFielder_Dec2015_setup.InputColumns;
create table Attribute_Value as select * from  Premier_League_MidFielder_Dec2015_setup.Attribute_Value;

create table Groundings like Premier_League_MidFielder_Dec2015_setup.Groundings; 
insert into Groundings select * from Premier_League_MidFielder_Dec2015_setup.Groundings;





