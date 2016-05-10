USE @database_work@;
SET storage_engine=INNODB;

CREATE TABLE ADT_PVariables_Select_List AS SELECT pvid, CONCAT('count(*)', ' as "MULT"') AS Entries FROM
    PVariables 
UNION SELECT 
    pvid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries
FROM
    1Nodes
        NATURAL JOIN
    PVariables;
    
CREATE TABLE ADT_PVariables_From_List AS SELECT pvid,
    CONCAT('@database_data@.', TABLE_NAME, ' AS ', pvid) AS Entries FROM
    PVariables;

CREATE TABLE ADT_PVariables_GroupBy_List AS SELECT pvid, 1nid AS Entries FROM
    1Nodes
        NATURAL JOIN
    PVariables;
    
CREATE TABLE Rnodes_join_columnname_list AS SELECT DISTINCT rnid,
    CONCAT(2nid, ' VARCHAR(5) DEFAULT ', ' "N/A" ') AS Entries FROM
    2Nodes
        NATURAL JOIN
    RNodes;
    
CREATE TABLE RNodes_1Nodes AS SELECT rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid1 AS pvid FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid1 
UNION SELECT 
    rnid, TABLE_NAME, 1nid, COLUMN_NAME, pvid2 AS pvid
FROM
    RNodes,
    1Nodes
WHERE
    1Nodes.pvid = RNodes.pvid2;
    
CREATE TABLE RNodes_GroupBy_List AS SELECT DISTINCT rnid, 1nid AS Entries FROM
    RNodes_1Nodes 
UNION DISTINCT SELECT DISTINCT
    rnid, 2nid AS Entries
FROM
    2Nodes
        NATURAL JOIN
    RNodes 
UNION DISTINCT SELECT 
    rnid, rnid
FROM
    RNodes;
    
CREATE TABLE RNodes_Select_List AS select rnid, CONCAT('count(*)', ' AS "MULT"') AS Entries FROM
    RNodes 
UNION SELECT DISTINCT
    rnid,
    CONCAT(pvid, '.', COLUMN_NAME, ' AS ', 1nid) AS Entries
FROM
    RNodes_1Nodes 
UNION DISTINCT select 
    temp.rnid, temp.Entries
FROM
    (SELECT DISTINCT
        rnid,
            CONCAT(rnid, '.', COLUMN_NAME, ' AS ', 2nid) AS Entries
    FROM
        2Nodes
    NATURAL JOIN RNodes
    ORDER BY RNodes.rnid , COLUMN_NAME) AS temp 
UNION DISTINCT SELECT 
    rnid, rnid AS Entries
FROM
    RNodes;

CREATE TABLE RNodes_pvars AS SELECT DISTINCT rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid1 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME1
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME 
UNION SELECT DISTINCT
    rnid,
    pvid,
    PVariables.TABLE_NAME,
    ForeignKeyColumns.COLUMN_NAME,
    ForeignKeyColumns.REFERENCED_COLUMN_NAME
FROM
    ForeignKeyColumns,
    RNodes,
    PVariables
WHERE
    pvid2 = pvid
        AND ForeignKeyColumns.TABLE_NAME = RNodes.TABLE_NAME
        AND ForeignKeyColumns.COLUMN_NAME = RNodes.COLUMN_NAME2
        AND ForeignKeyColumns.REFERENCED_TABLE_NAME = PVariables.TABLE_NAME;
    
CREATE TABLE RNodes_Where_List AS SELECT rnid,
    CONCAT(rnid,
            '.',
            COLUMN_NAME,
            ' = ',
            pvid,
            '.',
            REFERENCED_COLUMN_NAME) AS Entries FROM
    RNodes_pvars;
    
CREATE TABLE RNodes_From_List AS SELECT DISTINCT rnid,
    CONCAT('@database_data@.', TABLE_NAME, ' AS ', pvid) AS Entries FROM
    RNodes_pvars 
UNION DISTINCT SELECT DISTINCT
    rnid,
    CONCAT('@database_data@.', TABLE_NAME, ' AS ', rnid) AS Entries
FROM
    RNodes 
UNION DISTINCT SELECT DISTINCT
    rnid,
    CONCAT('(SELECT "T" AS ',
            rnid,
            ') AS ',
            CONCAT('`temp_', REPLACE(rnid, '`', ''), '`')) AS Entries
FROM
    RNodes;
    
CREATE TABLE ADT_RNodes_1Nodes_Select_List AS SELECT rnid,
    CONCAT('SUM(`',
            REPLACE(rnid, '`', ''),
            '_counts`.`MULT`)',
            ' AS "MULT"') AS Entries FROM
    RNodes 
UNION SELECT DISTINCT
    rnid, 1nid AS Entries
FROM
    RNodes_1Nodes;
    
CREATE TABLE ADT_RNodes_1Nodes_FROM_List AS SELECT rnid,
    CONCAT('`', REPLACE(rnid, '`', ''), '_counts`') AS Entries FROM
    RNodes;
    
CREATE TABLE ADT_RNodes_1Nodes_GroupBY_List AS SELECT DISTINCT rnid, 1nid AS Entries FROM
    RNodes_1Nodes;
    
CREATE TABLE ADT_RNodes_Star_Select_List AS SELECT DISTINCT rnid, 1nid AS Entries FROM
    RNodes_1Nodes;
    
CREATE TABLE ADT_RNodes_Star_From_List AS SELECT DISTINCT rnid,
    CONCAT('`', REPLACE(pvid, '`', ''), '_counts`') AS Entries FROM
    RNodes_pvars;
    
CREATE TABLE RChain_pvars AS SELECT DISTINCT lattice_membership.name AS rchain, pvid FROM
    lattice_membership,
    RNodes_pvars
WHERE
    RNodes_pvars.rnid = lattice_membership.member;
    
CREATE TABLE ADT_RChain_Star_Select_List AS SELECT DISTINCT lattice_rel.child AS rchain,
    lattice_rel.removed AS rnid,
    RNodes_GroupBy_List.Entries FROM
    lattice_rel,
    lattice_membership,
    RNodes_GroupBy_List
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND lattice_membership.name = lattice_rel.parent
        AND RNodes_GroupBy_List.rnid = lattice_membership.member 
UNION SELECT DISTINCT
    lattice_rel.child AS rchain,
    lattice_rel.removed AS rnid,
    1Nodes.1nid AS Entries
FROM
    lattice_rel,
    RNodes_pvars,
    1Nodes
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.removed
        AND RNodes_pvars.pvid = 1Nodes.pvid
        AND 1Nodes.pvid NOT IN (SELECT 
            pvid
        FROM
            RChain_pvars
        WHERE
            RChain_pvars.rchain = lattice_rel.parent) 
UNION SELECT DISTINCT
    lattice_rel.removed AS rchain,
    lattice_rel.removed AS rnid,
    1Nodes.1nid AS Entries
FROM
    lattice_rel,
    RNodes_pvars,
    1Nodes
WHERE
    lattice_rel.parent = 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.removed
        AND RNodes_pvars.pvid = 1Nodes.pvid;
        
CREATE TABLE ADT_RChain_Star_From_List AS SELECT DISTINCT lattice_rel.child AS rchain,
    lattice_rel.removed AS rnid,
    CONCAT('`',
            REPLACE(lattice_rel.parent, '`', ''),
            '_CT`') AS Entries FROM
    lattice_rel
WHERE
    lattice_rel.parent <> 'EmptySet' 
UNION SELECT DISTINCT
    lattice_rel.child AS rchain,
    lattice_rel.removed AS rnid,
    CONCAT('`',
            REPLACE(RNodes_pvars.pvid, '`', ''),
            '_counts`') AS Entries
FROM
    lattice_rel,
    RNodes_pvars
WHERE
    lattice_rel.parent <> 'EmptySet'
        AND RNodes_pvars.rnid = lattice_rel.removed
        AND RNodes_pvars.pvid NOT IN (SELECT 
            pvid
        FROM
            RChain_pvars
        WHERE
            RChain_pvars.rchain = lattice_rel.parent);

CREATE TABLE ADT_RChain_Star_Where_List AS SELECT DISTINCT lattice_rel.child AS rchain,
    lattice_rel.removed AS rnid,
    CONCAT(lattice_membership.member, ' = "T"') AS Entries FROM
    lattice_rel,
    lattice_membership
WHERE
    lattice_rel.child = lattice_membership.name
        AND lattice_membership.member > lattice_rel.removed
        AND lattice_rel.parent <> 'EmptySet';