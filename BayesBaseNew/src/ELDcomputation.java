
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
public class ELDcomputation {
	 static Connection con1;
	 // to be read from config
	 static String databaseName, databaseName2, databaseName1, OriginalDB, OutputDB, GenericDB;
	 static String real_database;
	 static String dbUsername="";
	 static String dbPassword="";
	 static String dbaddress="";
	 static String rchain ;
	 static int maxNumberOfMembers = 0;
	 private static boolean linkAnalysis;
	 public static void main(String[] args) throws Exception {
		 
		 ELDcomputation();
		 con1.close();
	 }
	 public ELDcomputation(String databaseName, String databaseName2){
		 local_CP.databaseName = databaseName;
		// local_CP.databaseName2 = databaseName2;
	 
	 }

	 
	 public static void ELDcomputation() throws Exception {
		 setVarsFromConfig();
	 	 connectDB();
	 	 ELd_generator();
	 	 con1.close();
	 }
		
	 public static void ELd_generator() throws SQLException {
			// TODO Auto-generated method stub
		 Statement st = con1.createStatement();
		 ArrayList<String> fids = new ArrayList<String>();
		 /*select distinct child from `Premier_League_Synthetic_Bernoulli_SV_BN`.`Path_BayesNets` where child not in
 (select rnid from `Premier_League_Synthetic_Bernoulli_SV_BN`.`RNodes`)*/
			ResultSet rs = st.executeQuery("select distinct child from "+databaseName+".Path_BayesNets where child not in (select" +
					" rnid from "+databaseName+".RNodes)");
		 /*create table Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP_temp` like Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP`;
insert into Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP_temp` select * from Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP`;
alter table Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP_temp` CHANGE CP CPGeneral float(7,6);
alter table Premier_League_Synthetic_Bernoulli_SV_db.`Feature1(Players0,MatchComp0)_local_CP_temp` CHANGE prior priorGeneral float(7,6);
*/
			 while (rs.next()) {
				 fids.add(rs.getString(1));
			 }
			 for (int i=0; i<fids.size(); ++i) {
				 String s = fids.get(i).substring(0, fids.get(i).length() - 1);
				 System.out.println("create table if not exists "+GenericDB+"."+s+"_local_CP_temp` like " +GenericDB+"."+s+"_local_CP`");
				 st.execute("drop table if exists "+GenericDB+"."+s+"_local_CP_temp`");
				 st.execute("create table if not exists "+GenericDB+"."+s+"_local_CP_temp` like " +GenericDB+"."+s+"_local_CP`"); 
				 st.execute("insert into "+GenericDB+"."+s+"_local_CP_temp` select * from "+GenericDB+"."+s+"_local_CP`");
				 st.execute("alter table "+GenericDB+"."+s+"_local_CP_temp` CHANGE CP CPGeneral float(7,6);");
				 st.execute("alter table "+GenericDB+"."+s+"_local_CP_temp` CHANGE prior priorGeneral float(7,6);");
				 System.out.println("create table if not exists  "+OutputDB+"."+s+"_local_CP_nj` as select * from "+OutputDB+"."+s+"_local_CP` natural" +
					 		" join "+GenericDB+"."+s+"_local_CP_temp`");
				 st.execute("drop table if exists "+OutputDB+"."+s+"_local_CP_nj`");
				 st.execute("create table if not exists  "+OutputDB+"."+s+"_local_CP_nj` as select * from "+OutputDB+"."+s+"_local_CP` natural" +
				 		" join "+GenericDB+"."+s+"_local_CP_temp`");
				 
				 
			 }
		}


	public static void setVarsFromConfig(){

			//Config2 is for set the configuration parameters when we have an individual to be grounded. 
	//it reads from src/config.cfg which also has parameters which define what type of learning we are doing if approach1=1 it means we need
	//to learn a bayesnet for each individual and if approach2=1 it means we should use the general bayesnets and score it on each individual. June 2014
			
			//Config conf = new Config();		 
			Config2 conf = new Config2();
		 	 dbUsername = conf.getProperty("dbusername");
		 	 dbPassword = conf.getProperty("dbpassword");
		 	 dbaddress = conf.getProperty("dbaddress");
		 	 real_database=conf.getProperty("dbname");
		 	OutputDB=conf.getProperty("OutputDB");
		 	OriginalDB=conf.getProperty("OriginalDB");
		 	 databaseName = real_database + "_BN";
		 	// databaseName2 = real_database + "_copy_CT";
		 	 databaseName1 = real_database + "_db";
		 	GenericDB=conf.getProperty("GenericDB");
		 	String la		= conf.getProperty( "LinkAnalysis" );
			if ( la.equals( "1" ) )
			{
				linkAnalysis = true;
			}
			else
			{
				linkAnalysis = false;
			}
		 	 
		 }
	 public static void connectDB() throws SQLException {
		 String CONN_STR1 = "jdbc:" + dbaddress + "/" + databaseName1;
	 
	 	 try {
	 	 	 java.lang.Class.forName("com.mysql.jdbc.Driver");
	 	 } catch (Exception ex) {
	 	 	 System.err.println("Unable to load MySQL JDBC driver");
	 	 }
	 	 con1 = DriverManager.getConnection(CONN_STR1, dbUsername, dbPassword);
	 	 java.sql.Statement st = con1.createStatement();
	 	 ResultSet maxl = st.executeQuery("Select max(length) From " + databaseName + ".lattice_set;");
	 	 maxl.absolute(1);
	 	 maxNumberOfMembers = maxl.getInt(1);
	 	 
	 	 ResultSet myrchain = st.executeQuery("select name as RChain from " + databaseName + ".lattice_set where lattice_set.length = " + maxNumberOfMembers + ";");
	 	 myrchain.absolute(1);
	 	 rchain = myrchain.getString(1);
	 	 System.out.println("rchain: "+ rchain +"\n");
	 	 st.close(); 
	}
}
