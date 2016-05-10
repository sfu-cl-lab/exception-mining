import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import com.mysql.jdbc.Connection;
// for team or player you need to change the line of inserting into groundings

public class Delete {
	static String opt1,opt2,indi,cont,neg,kld;
	static String databaseName, databaseName2, databaseName3;
	static String databaseName4="Premier_League_MidFielder_Dec2015";//   Premier_League_2011
	static Connection con4;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress,app1, app2;
	static String 	Cdbaddress,Cdbusername,Cdbpassword,Cdbschema,Cdbcounts,Cdbcondprob,CLink;
	static int maxNumberOfMembers = 0;
	static String subdbaddress,subdbusername,subdbpassword,subdbBNschema,subdbDataSchema,subdbOutputSchema,subpathBayesNet,subLink,subContinuous,subGrounded;
	public static void main(String[] args) throws Exception {
		
		setVarsFromConfig();
		connectDB();
		System.out.println(indi);
		Statement initst = con4.createStatement();
		loadSubsetConfig();
		loadScoreConfig();
		
		
		for(int i=0;i<PlayerTeamW("43").size();i++){
	
			 String PlayerID = PlayerTeamW("43").get(i);
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db_01");
			System.out.println("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db_01");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_BN");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_CT");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_flat");
		}
		
	}
	
	public static void DeleteFileFolder(String path) {
		 
        File file = new File(path);
        if(file.exists())
        {
            do{
                delete(file);
            }while(file.exists());
        }else
        {
            System.out.println("File or Folder not found : "+path);
        }
 
    }
    private static void delete(File file)
    {
        if(file.isDirectory())
        {
            String fileList[] = file.list();
            if(fileList.length == 0)
            {
              //  System.out.println("Deleting Directory : "+file.getPath());
                file.delete();
            }else
            {
                int size = fileList.length;
                for(int i = 0 ; i < size ; i++)
                {
                    String fileName = fileList[i];
                   // System.out.println("File path : "+file.getPath()+" and name :"+fileName);
                    String fullPath = file.getPath()+"/"+fileName;
                    File fileOrFolder = new File(fullPath);
                  //  System.out.println("Full Path :"+fileOrFolder.getPath());
                    delete(fileOrFolder);
                }
            }
        }else
        {
           // System.out.println("Deleting file : "+file.getPath());
            file.delete();
        }
    }
	
	public static void connectDB() throws SQLException {
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + databaseName4;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con4 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
	}
	
	public static ArrayList<String> PlayerTeamW(String TeamID) throws SQLException {
		
		ArrayList<String> PlayerList = new ArrayList<String>();
		Statement st1 = con4.createStatement();
		System.out.println("sa");
		//where PlayerID not in (select PlayerID from PPLPositvePlayer.Path_BayesNets)
	//	ResultSet rs = st1.executeQuery(" select TeamID  from Teams where TeamID=11;" );
	//	ResultSet rs = st1.executeQuery("select distinct PlayerID from Players;" );
		ResultSet rs = st1.executeQuery("select PlayerID from   Premier_League_MidFielder_Dec2015.Players  ;" );
		//ResultSet rs = st1.executeQuery("select movieid from  imdb_MovieLens_Action.movies where movieid<>1717144  ;" );
		
		while (rs.next()) {
			PlayerList.add(""+rs.getInt(1));

		}
		  
		st1.close();
	
	
	return PlayerList;
	
}
	
	
	public static void setVarsFromConfig(){
		Config2 conf = new Config2();
		databaseName = conf.getProperty("dbname");
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
		opt1 = conf.getProperty("AutomaticSetup");
		opt2 = conf.getProperty("LinkCorrelations");
		kld=conf.getProperty("ComputeKLD");
		cont = conf.getProperty("Continuous");
		indi = conf.getProperty("Individual");
		neg = conf.getProperty("Negative");
		app1=conf.getProperty("Approach1");
		app2=conf.getProperty("Approach2");
	}
	
	public static int loadScoreConfig()
	{
		Properties configFile = new java.util.Properties();
		FileReader fr = null;
		
		try
		{
			fr = new FileReader( "/local-scratch/SRiahi/workspace/TestBayesBase/cfg/scorecomputation2.cfg" );
		}
		catch ( FileNotFoundException e )
		{
			System.out.println( "Failed to find configuration file!" );
			return -1;
		}
		
		BufferedReader br = new BufferedReader( fr );
		
		try
		{
			configFile.load( br );
		
			Cdbaddress	= configFile.getProperty( "dbaddress" );
			Cdbusername	= configFile.getProperty( "dbusername" );
			Cdbpassword	= configFile.getProperty( "dbpassword" );
			Cdbschema	= configFile.getProperty( "dbschema" );
			Cdbcounts	= configFile.getProperty( "dbcounts" );
			Cdbcondprob	= configFile.getProperty( "dbcondprob" );
			CLink = configFile.getProperty( "LinkAnalysis" );
	

			br.close();
			fr.close();
		}
		catch ( IOException e )
		{
			System.out.println( "Failed to load configuration file." );
			return -2;
		}
		
		return 0;
	}
	public static int loadSubsetConfig()
	{
		Properties configFile = new java.util.Properties();
		FileReader fr = null;
		
		try
		{
			fr = new FileReader( "/local-scratch/SRiahi/workspace/TestBayesBase/cfg/subsetctcomputatio2.cfg" );
		}
		catch ( FileNotFoundException e )
		{
			System.out.println( "Failed to find configuration file!" );
			return -1;
		}
		
		BufferedReader br = new BufferedReader( fr );
		
		try
		{
			configFile.load( br );
			
			subdbaddress	= configFile.getProperty( "dbaddress" );
			subdbusername	= configFile.getProperty( "dbusername" );
			subdbpassword	= configFile.getProperty( "dbpassword" );
			subdbBNschema	= configFile.getProperty( "dbBNschema" );
			subdbDataSchema	= configFile.getProperty( "dbDataSchema" );
			subdbOutputSchema	= configFile.getProperty( "dbOutputSchema" );
			subpathBayesNet	= configFile.getProperty( "pathBayesNet" );
			subLink = configFile.getProperty( "LinkAnalysis" );
			subContinuous = configFile.getProperty( "Continuous" );
			subGrounded = configFile.getProperty( "Grounded" );
				
			
			br.close();
			fr.close();
		}
		catch ( IOException e )
		{
			System.out.println( "Failed to load configuration file." );
			return -2;
		}
		
		return 0;
	}
	
}