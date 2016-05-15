# How to run OutlierDetection code  
## First run project `BayesBaseNew`  
  
1. Make yourDatabaseName_setup in the database server. You can use any of the BayeBase codes to generate the setup database. My code doesn't generate the setup. You can use GeneralBayesBase to generate Setup.
For example if database name is Premier_League_MidStr setup must be in the format of Premier_League_MidStr_setup.  
  
2. What is the individual you want to ground? Is it a Movie, player or a team? You should change a line in the main function of BayesBaseNew/src/Wrapper.Java.
line 127:  as shown below:   
![s](https://cloud.githubusercontent.com/assets/4648756/15276714/19f1dedc-1aa5-11e6-80f8-2c8370b7a352.png)  
  
3. Generate Generic Bayesnet: You can use any BayesBase code to generate Generic Basenet: Or you can use the project BayesBaseNew to generate it by doing followings:
  
  + change databasename in the Config files: for example replace all the Premier_League_MidFielder_Dec2015 in all the config files:  
    + `BayesBaseNew/cfg/groundcfg/scorecomputation.cfg`
    + `BayesBaseNew/cfg/groundcfg/subsetctcomputation.cfg`  `BayesBaseNew/src/config.cfg`
  + In `BayesBaseNew/src/config.cfg` file modify the following lines:   
    Individual=0 and Approach1 = 0 and Approach2 = 0
  + In `BayesBaseNew/cfg/groundcfg/scorecomputation.cfg` change ELD= 0 
  + In `BayesBaseNew/cfg/groundcfg/subsetctcomputation.cfg` make Grounded=0 and ELD=0
  + In the code: in `BayesBaseNew/src/Wrapper.Java` in the function named: PlayerTeamW change the quary to run it for only one individual.
    
  ![t](https://cloud.githubusercontent.com/assets/4648756/15276715/1c9c9e4c-1aa5-11e6-98ee-5b54b22a84c0.png)
    
  + Then it will generate the generic database for you with these names:   
    + ``YourDatabaseName_***_BN``   
    + ``YourDatabaseName_***_CT``  
    + ``YourDatabaseName_***_db``  
  
  then you have to rename these databases to YourDatabaseName_db;YourDatabaseName_BN; YourDatabaseName_CT to reflect that this is infact generic.
  
4. Now you should iterate through all individuals: in two different runs.  
  + First Run:
    + In `BayesBaseNew/src/config.cfg` change Individual=1;Approach1 = 0;Approach2 = 1 and ELD=0
    + In databaseserver make a schema for code to store result in. Schema must have two tables: Score and Path_BayesNet make tables in the result database just like these two tables in `Premier_League_Midfielder_Dec2015_Correlation` Schema: create table score like `Premier_League_Midfielder_Dec2015_CorrelationSchema`.`Score`  
    + Change the ResultdataBase field in `BayesBaseNew/src/config.cfg` to the database name that you created in the previous step.  
    + In `BayesBaseNew/cfg/groundcfg/scorecomputation.cfg` change ELD=0
    + In `BayesBaseNew/cfg/groundcfg/subsetctcomputation.cfg` make Grounded=1 and ELD=0
    + In the code: in `BayesBaseNew/src/Wrapper.Java` in the function named: PlayerTeamW change the quary to run it for all the individuals  
  + Second Run:    
    + In `BayesBaseNew/src/config.cfg` change Individual=1;Approach1 = 0;Approach2 = 0 and ELD=1    
    + In databaseserver make another schema for code to store result in. Just like a database you created for the result of first run, it must have two tables with those formats
    + Change the ResultdataBase field in  `BayesBaseNew/src/config.cfg` to the database name that you created in the previous step.
    + In `BayesBaseNew/cfg/groundcfg/scorecomputation.cfg` change ELD=1
    + In `BayesBaseNew/cfg/groundcfg/subsetctcomputation.cfg` make Grounded=1 and ELD=1  
             
## Then run project `ScoreComputation`   
+ In order to run `ScoreComputation` you have to compelete both runs described above.
+ You have to make a table named `PlayersCalss` with the format of `Premier_League_Midfielder_Dec2015_CorrelationSchema`.`PlayersClass` where you label which data points are outlier and which are not: then in the `ScoreComputation/src/config.cfg`, change all database name to your databasename. OriginalDB should be name of result database of your first run and GeneralDB should be for your second run. OutlierIndex should be the label of outlier class in the PlayersClass.
+ `Threshold` shows number of known outliers in data and `NonOutlier` is the number of non-outlier.
