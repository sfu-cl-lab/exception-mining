/*
 * Author: Kurt Routley
 * Date: September 20, 2013
 * 
 * MakeTargetSetup.java
 *  	- Creates the @database@_target_setup table, which includes:
 *  		- 
 */

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Connection;

public class MakeTargetSetup {

	static Connection con1;

	//  to be read from config.cfg.
	// The config.cfg file should  be the working directory.
	static String databaseName, databaseName0, databaseName1;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress;
	
	public static void main(String[] args) throws Exception
	{
		long t1 = System.currentTimeMillis(); 
		System.out.println( "Start Program..." );
		
		/*
		 * Create Markov Blanket
		 */
		MarkovBlanket.runMakeMarkovBlanket();
		
		/*
		 *  Perform setup if not completed
		 */
		RunBB.setVarsFromConfig();
		if ( RunBB.opt1.equals( "1" ) )
		{
			MakeSetup.runMS();
			System.out.println( "Setup database is ready." );
		} 
		else
		{
			System.out.println( "Setup database exists." );
		}
		

		
		long t2 = System.currentTimeMillis(); 
		System.out.println( "Running time is " + (t2-t1) + "ms." );
		
		runMakeTargetSetup();
		
		t2 = System.currentTimeMillis();
		System.out.println( "Final runtime is: " + ( t2 - t1 ) + "ms." );
	}
	
	public static void runMakeTargetSetup() throws Exception
	{
		setVarsFromConfig();
		connectDB();
		
		BZScriptRunner bzsr = new BZScriptRunner(databaseName,con1);
		bzsr.runScript("src/scripts/transfer_to_target.sql");  

		Statement st = con1.createStatement();
		
		System.out.println( "SHOW TABLES IN " + databaseName + ";" );
		ResultSet rs = st.executeQuery( "SHOW TABLES IN " + databaseName + ";" );
		
		while ( rs.next() )
		{
			String tableName = rs.getString( 1 );
			
			Statement st2 = con1.createStatement();
			
		String databaseName_test = databaseName.substring(0,databaseName.lastIndexOf("_")) //cross validation
					+"_Test"+databaseName.substring(databaseName.length()-1)
					;

			System.out.println( "CREATE TABLE " + databaseName1 + "." + tableName + 
						 " AS SELECT * FROM " + databaseName_test + "." + tableName + ";" );
			st2.execute( "CREATE TABLE " + databaseName1 + "." + tableName +  // copy data from testing database as target, May 6th, zqian
						 " AS SELECT * FROM " + databaseName_test + "." + tableName + ";" );
			
//			
//			System.out.println( "CREATE TABLE " + databaseName1 + "." + tableName + 
//					 " AS SELECT * FROM " + databaseName + "." + tableName + ";" );
//		st2.execute( "CREATE TABLE " + databaseName1 + "." + tableName +  // 
//					 " AS SELECT * FROM " + databaseName + "." + tableName + ";" );
//
			
			st2.close();
		}
		
		rs.close();
		
		st.close();
        
		disconnectDB();
	}
	
	
	public static void setVarsFromConfig()
	{
		Config conf = new Config();
		databaseName = conf.getProperty("dbname");
		databaseName0 = databaseName + "_target_setup";
		databaseName1 = databaseName + "_target";
		
		dbUsername = conf.getProperty("dbusername");
		dbPassword = conf.getProperty("dbpassword");
		dbaddress = conf.getProperty("dbaddress");
	}

	public static void connectDB() throws SQLException 
	{
		//open database connections to the original database, 
		//the setup database, the bayes net database, and the contingency 
		//table database

		String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName;
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con1 = (Connection) DriverManager.getConnection( CONN_STR1, 
														 dbUsername, 
														 dbPassword);

	}
	
	public static void disconnectDB() throws SQLException 
	{
		con1.close();
	}

}