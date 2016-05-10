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
public class IdenticalityCheck {
	static String opt1,opt2,indi,cont,neg,kld;
	static String databaseName, databaseName2, databaseName3;
	static String databaseName4="Premier_League_Strikers";//   Premier_League_2011
	static Connection con4;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress,app1, app2;
	static String 	Cdbaddress,Cdbusername,Cdbpassword,Cdbschema,Cdbcounts,Cdbcondprob,CLink, orig;
	static int maxNumberOfMembers = 0;
	static String subdbaddress,subdbusername,subdbpassword,subdbBNschema,subdbDataSchema,subdbOutputSchema,subpathBayesNet,subLink,subContinuous,subGrounded;
	public static void main(String[] args) throws Exception {
		IdenticalityCheck();
		 con4.close();
	}
	 public IdenticalityCheck(String databaseName, String databaseName2){
		 local_CP.databaseName = databaseName;
		// local_CP.databaseName2 = databaseName2;
	 
	 }
	
	 public static void IdenticalityCheck() throws Exception {
			setVarsFromConfig();
			connectDB();
			System.out.println(indi);
			Statement initst = con4.createStatement();
			PathBayesNetCP() ;
			CTCP();
			BNBN();
	 }
	public static void PathBayesNetCP() throws SQLException {
		
		Statement st = con4.createStatement();
		Statement st2 = con4.createStatement();
		
		 //System.out.println(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` add PBValidCP varchar(20)");
		 //ALTER TABLE tbl_Country DROP COLUMN IsDeleted ;
		System.out.println("Show columns from "+databaseName+"_BN.`FID_a,b,c_Scores` where Field='PBValidCP' ;");
		ResultSet drop=st.executeQuery("Show columns from "+databaseName+"_BN.`FID_a,b,c_Scores` where Field='PBValidCP' ;");
		if (drop.next() ) {
		    System.out.println("no data");
		    st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` DROP COLUMN PBValidCP");
		}

		
		 st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` add PBValidCP varchar(20)");
		 ArrayList<String> localCPs = new ArrayList<String>();
		ResultSet rst = st.executeQuery("select distinct child from "+databaseName+"_BN"+".Path_BayesNets where child not in (select" +
				" rnid from "+orig+"_BN.RNodes)");
		
		 while (rst.next()) {
			 localCPs.add(rst.getString(1));
		 }
		 for (int iii=0; iii<localCPs.size(); ++iii) {
			 String s= localCPs.get(iii).substring(1, localCPs.get(iii).length() - 1);
			 st.execute("show columns from  "+databaseName+"_db"+".`"+s+"_local_CP`");
			 /* SELECT column_name
     FROM information_schema.COLUMNS
     WHERE table_schema = 'Premier_League_Strikers_59325_db' and table_name='dribble_eff(Players0,MatchComp0)_local_CP' 
and column_name<>'a' and column_name<>'b' and column_name<>'CP' and column_name<>'prior' order by column_name*/
		 System.out.println("SELECT concat('`',column_name,'`') from information_schema.COLUMNS " +
			 		"WHERE table_schema =  '"+databaseName+"_db'"+" and table_name= '"+s+"_local_CP' and " +
	 				" and column_name<>'' and column_name<>'a' and column_name<>'b' and column_name<>'CP' and column_name<>'prior' and column_name<> '"+s +"' order by column_name;");
			 ResultSet rsLocalCP= st.executeQuery("SELECT concat('`',column_name,'`') from information_schema.COLUMNS " +
			 		"WHERE table_schema =  '"+databaseName+"_db'"+" and table_name= '"+s+"_local_CP'  " +
			 				" and column_name<>'' and column_name<>'a' and column_name<>'b' and column_name<>'c' and column_name<>'CP' and column_name<>'prior' and column_name<> '"+s +"' order by column_name;");
			 ArrayList<String> CPcolumn = new ArrayList<String>();
			 while (rsLocalCP.next()) {
				 CPcolumn.add(rsLocalCP.getString(1));
			 }	
			System.out.println("SELECT parent from "+databaseName+"_BN"+".Path_BayesNets where child='"
					 +localCPs.get(iii)+"' and RChain='`a,b,c`' and parent <>'`a`' and parent<>'`b`' and parent<>'`c`' and column<> '`c`'  and parent<> '"+s+"' order by parent;");

			 ResultSet BayesNets=st2.executeQuery("SELECT parent from "+databaseName+"_BN"+".Path_BayesNets where child='"
					 +localCPs.get(iii)+"' and RChain='`a,b,c`' and parent <>'`a`' and parent<>'`b`' and parent<>'`c`' and parent<> '"+s+"' and parent <> '' order by parent;");
			
			 ArrayList<String> PBParents = new ArrayList<String>();
 
			 
			 while (BayesNets.next()) {
				 PBParents.add(BayesNets.getString(1));
			 }	
			 
		 System.out.println(localCPs.get(iii)+":"+PBParents.size());
		 for(int i = 0; i < PBParents.size(); i++) {   
		//	   System.out.println("PathBN"+localCPs.get(iii)+PBParents.get(i));
			}  
		 
		 System.out.println(localCPs.get(iii)+":"+CPcolumn.size());
			 for(int i = 0; i < CPcolumn.size(); i++) {   
			//	   System.out.println("CP"+localCPs.get(iii)+CPcolumn.get(i));
				}  
			 
			 if(CPcolumn.size()!=PBParents.size()){
			/*	 System.out.println("Test 2 update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='NotValid'  where Fid= '"+localCPs.get(iii)+"'");*/
				 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='NotValid'  where Fid= '"+localCPs.get(iii)+"'");
			 }
			 
			 else{
				 int update=0;
			 
			 for (int j=0; j<PBParents.size(); j++) {
			 if (! CPcolumn.get(j).equals(PBParents.get(j) )){
				 System.out.println("update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='NotValid' where Fid= '"+localCPs.get(iii)+"'");
				 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='NotValid' where Fid= '"+localCPs.get(iii)+"'");
			 update=1;
			 }
			 
			 }
			 if(update==0){
				// System.out.println("update "+databaseName+"_BN.FID_a,b,c_Scores set PBValidCP='NotValid' where Fid= "+localCPs.get(iii));
			 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='Valid' where Fid= '"+localCPs.get(iii)+"'");
			 }
			 }
		 }
		
		
	}
	public static void CTCP() throws SQLException {
		
		Statement st = con4.createStatement();
		Statement st2 = con4.createStatement();
		
		 //System.out.println(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` add PBValidCP varchar(20)");
		 //ALTER TABLE tbl_Country DROP COLUMN IsDeleted ;
		ResultSet drop=st.executeQuery("Show columns from "+databaseName+"_BN.`FID_a,b,c_Scores` where Field='CTValidCP' ;");
		if (drop.next() ) {
		    System.out.println("no data");
			 st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` DROP COLUMN CTValidCP");
		}
		
	
		 st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` add CTValidCP varchar(20)");
		 ArrayList<String> localCPs = new ArrayList<String>();
		ResultSet rst = st.executeQuery("select distinct child from "+databaseName+"_BN"+".Path_BayesNets where child not in (select" +
				" rnid from "+orig+"_BN.RNodes)");
		
		 while (rst.next()) {
			 localCPs.add(rst.getString(1));
		 }
		 for (int iii=0; iii<localCPs.size(); ++iii) {
			 String s= localCPs.get(iii).substring(1, localCPs.get(iii).length() - 1);
			 ResultSet rsLocalCP= st.executeQuery("SELECT column_name from information_schema.COLUMNS " +
				 		"WHERE table_schema =  '"+databaseName+"_db'"+" and table_name= '"+s+"_local_CP'  " +
				 				" and column_name<>'' and  column_name<>'a' and column_name<>'b' and column_name<>'CP' and column_name<> 'c' and column_name<>'prior' and column_name<> '"+s +"' order by column_name;");
			 ArrayList<String> CPcolumn = new ArrayList<String>();
			 while (rsLocalCP.next()) {
				 CPcolumn.add(rsLocalCP.getString(1));
			 }	
			 ResultSet rsLocalCT= st.executeQuery("SELECT column_name from information_schema.COLUMNS " +
				 		"WHERE table_schema =  '"+databaseName+"_db'"+" and table_name= '"+s+"_a,b,c_local_CT'  " +
				 				" and column_name<>'' and  column_name<>'a' and column_name<>'b' and column_name<> 'c' and column_name<>'MULT' and column_name<> '"+s +"' order by column_name;");
			
			 ArrayList<String> CTcolumn = new ArrayList<String>();
			 while (rsLocalCT.next()) {
				 CTcolumn.add(rsLocalCT.getString(1));
			 }
			 if(CPcolumn.size()!=CTcolumn.size()){
					/*	 System.out.println("Test 2 update "+databaseName+"_BN.`FID_a,b,c_Scores` set PBValidCP='NotValid'  where Fid= '"+localCPs.get(iii)+"'");*/
						 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set CTValidCP='NotValid'  where Fid= '"+localCPs.get(iii)+"'");
					 }
					 
					 else{
						 int update=0;
					 
					 for (int j=0; j<CTcolumn.size(); j++) {
					 if (! CPcolumn.get(j).equals(CTcolumn.get(j) )){
						 System.out.println("update "+databaseName+"_BN.`FID_a,b,c_Scores` set CTValidCP='NotValid' where Fid= '"+localCPs.get(iii)+"'");
						 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set CTValidCP='NotValid' where Fid= '"+localCPs.get(iii)+"'");
					 update=1;
					 }
					 
					 }
					 if(update==0){
						// System.out.println("update "+databaseName+"_BN.FID_a,b,c_Scores set PBValidCP='NotValid' where Fid= "+localCPs.get(iii));
					 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set CTValidCP='Valid' where Fid= '"+localCPs.get(iii)+"'");
					 }
					 }
				 
		 }
		
	}
	public static void BNBN() throws SQLException {
		Statement st = con4.createStatement();
		ResultSet drop=st.executeQuery("Show columns from "+databaseName+"_BN.`FID_a,b,c_Scores` where Field='BNValidBN' ;");
		if (drop.next() ) {
		    System.out.println("there is  data");
			 st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` DROP COLUMN BNValidBN");
		}
		

		 st.execute(" alter table "+databaseName+"_BN.`FID_a,b,c_Scores` add BNValidBN varchar(20)");
		 ResultSet rs= st.executeQuery("Select Rchain,child, parent from "+orig+"_BN.Path_BayesNets where  (Rchain,child, parent) not in (select  Rchain,child, parent from "+databaseName+"_BN.Path_BayesNets  where Rchain='`a,b,c`') and Rchain='`a,b,c`'");
		 if (rs.next() ) {
			 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set BNValidBN='NotValid' ");
		 }
		 else{
			 st.execute("update "+databaseName+"_BN.`FID_a,b,c_Scores` set BNValidBN='Valid' ");
		 }
	
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
		orig=conf.getProperty("OriginalDB");
	}
	
	public static void connectDB() throws SQLException {
		String CONN_STR2 = "jdbc:" + dbaddress + "/" + orig;
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
		ResultSet rs = st1.executeQuery("select distinct PlayerID from  Premier_League_Strikers.Players ;" );
		
		while (rs.next()) {
			PlayerList.add(""+rs.getInt(1));

		}
		  
		st1.close();
	
	
	return PlayerList;
	
}
}
