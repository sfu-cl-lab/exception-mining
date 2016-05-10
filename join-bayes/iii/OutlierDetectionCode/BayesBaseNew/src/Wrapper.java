//June 24 2014, where we ground teamID, for relation of appearsPlayer(P,M) we don't have a way to effect the groundings
//so we just add appearsTeam(T,m) as a parent for the nodes that only have appearsPlayer(p,m) as parent
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

public class Wrapper {
	static String opt1,opt2,opt3,indi,cont,neg,kld;
	static String databaseName, databaseName2, databaseName3,OutputDB,background,GeneralPathBayesNets,OriginalDB,GenericDB;
	static String databaseName4;//="imdb_MovieLens_Comedy";//   Premier_League_2011
	
	static String resultdataBase;// = "Premier_League_2011_OUT_BG"; // imdb_MovieLens_Positive
	static Connection con4,con3;
	static String dbUsername;
	static String dbPassword;
	static String dbaddress,app1, app2;
	static String 	Cdbaddress,Cdbusername,Cdbpassword,Cdbschema,Cdbcounts,Cdbcondprob,CLink,rchain,ELD1,ELD2,ELD3;
	static int maxNumberOfMembers = 0;
	static String subdbaddress,subdbusername,subdbpassword,subdbBNschema,subdbDataSchema,subdbOutputSchema,subpathBayesNet,subLink,subContinuous,subGrounded,dbschema,dbcondprob,dbcounts;
	public static void main(String[] args) throws Exception {
		
		setVarsFromConfig();
		connectDB();
		System.out.println(indi);
		
		loadSubsetConfig();
		loadScoreConfig();
		
		
		for(int i=0;i<PlayerTeamW("43").size();i++){
			
			 String PlayerID = PlayerTeamW("43").get(i);
			
			 File file = new File("src/config.cfg");
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write("dbname = "+databaseName4+"_"+PlayerID+"\n");
				output.write("dbusername = "+dbUsername+"\n");
				output.write("dbpassword = "+dbPassword+"\n");
				output.write("dbaddress = "+dbaddress+"\n");
				output.write("AutomaticSetup = " + opt1 +"\n");
				output.write("LinkCorrelations = "+ opt2 + "\n");
				output.write("LinkAnalysis = "+ opt2 + "\n");
				output.write("ComputeKLD = "+ kld + "\n");
				output.write("Continuous = " + cont + "\n");
				output.write("Individual = " + indi +"\n");
				output.write("Negative = " + neg +"\n");
				output.write("Approach1 = " + app1 +"\n");
				output.write("Approach2 = " + app2 +"\n");
				output.write("OutputDB = "+databaseName4+"_"+PlayerID+"_db"+"\n");
				output.write("background = "+background+"\n");
				output.write("GeneralPathBayesNets = "+GeneralPathBayesNets+"\n");
				output.write("OriginalDB = "+OriginalDB+"\n");
				output.write("resultdataBase = "+resultdataBase+"\n");
				output.write("GenericDB = "+databaseName4+"_"+PlayerID+"_db_01"+"\n");
				output.write("ELD = "+ELD1+"\n");
				//OriginalDB
				output.close();
				
				File fileScore = new File("/local-scratch/SRiahi/workspaceNew/BayesBaseNew/cfg/scorecomputation.cfg");
				BufferedWriter outputScore = new BufferedWriter(new FileWriter(fileScore));
				outputScore.write("dbaddress = "+Cdbaddress+"\n");
				outputScore.write("dbusername = "+Cdbusername+"\n");
				outputScore.write("dbpassword = "+Cdbpassword+"\n");
				outputScore.write("dbschema = " + Cdbschema +"_"+PlayerID+"_BN"+"\n");
				outputScore.write("dbcounts = "+ Cdbcounts +"_"+PlayerID+"_db"+ "\n");
				outputScore.write("dbcondprob = "+ Cdbcondprob +"_"+PlayerID+"_db"+ "\n");
				outputScore.write("LinkAnalysis = " + CLink + "\n");
				outputScore.write("ELD = " + ELD2+ "\n");

				outputScore.close();
					

					
				File fileSub = new File("/local-scratch/SRiahi/workspaceNew/BayesBaseNew/cfg/subsetctcomputation.cfg");
				BufferedWriter outputSub = new BufferedWriter(new FileWriter(fileSub));
				outputSub.write("dbaddress = "+subdbaddress+"\n");
				outputSub.write("dbusername = "+subdbusername+"\n");
				outputSub.write("dbpassword = "+subdbpassword+"\n");
				outputSub.write("dbBNschema = " + subdbBNschema +"_"+PlayerID+"_BN"+"\n");
				outputSub.write("dbDataSchema = "+ subdbDataSchema + "\n");
				outputSub.write("dbOutputSchema = "+ subdbOutputSchema +"_"+PlayerID+"_db"+ "\n");
				outputSub.write("pathBayesNet = " + subpathBayesNet + "\n");
				outputSub.write("LinkAnalysis = "+ subLink + "\n");
				outputSub.write("Continuous = " + subContinuous + "\n");
				outputSub.write("Grounded = " + subGrounded + "\n");
				//dbschema =
				outputSub.write("dbschema = " + dbschema+"_"+PlayerID+"_BN" + "\n");
				outputSub.write("dbcounts = " + dbcounts+"_"+PlayerID+"_db" + "\n");
				outputSub.write("dbcondprob = " + dbcondprob+"_"+PlayerID+"_db" + "\n");
				outputSub.write("ELD = " + ELD3+ "\n");
				outputSub.close();
				
				//for(String PlayerID: PlayerTeam("43")){
				System.out.println("Players"+PlayerID+"\n"); 
				System.out.println("Con4"+con4+"\n");
				Statement st = con4.createStatement();
				String schmaName0 = databaseName4 + "_" + PlayerID;
				String schmaName = databaseName4 + "_" + PlayerID + "_BN" ;
				st.execute("drop SCHEMA if exists " + schmaName + ";");
				st.execute("CREATE SCHEMA  " + schmaName + ";");
				String schmaName2 = databaseName4 + "_" + PlayerID + "_CT" ;
				st.execute("drop SCHEMA if exists " + schmaName2 + ";");
				st.execute("CREATE SCHEMA  " + schmaName2 + ";");
				String databaseName5 = databaseName4 + "_" + PlayerID+"_BN";
				
				
				st.execute("delete from " + databaseName4 + "_setup.Groundings ;");
			
			if(indi.equals("1")) {

			//	String sql = "CREATE TABLE if not exists " +databaseName4+".Groundings (  pvid varchar(40) NOT NULL,  id varchar(256) NOT NULL,  PRIMARY KEY  (pvid,id));";

	//	st.execute("insert into " + databaseName4 + "_setup.Groundings values ( 'Teams0', " + PlayerID +");");
			st.execute("insert into " + databaseName4 + "_setup.Groundings values ( 'Players0', " + PlayerID +");");
		//	st.execute("insert into " + databaseName4 + "_setup.Groundings values ( 'movies0', " + PlayerID +");");
	}
			
			//Groundings
	//		String sql = "CREATE TABLE " +databaseName5+".Groundings (  pvid varchar(40) NOT NULL,  id varchar(256) NOT NULL,  PRIMARY KEY  (pvid,id));";

			System.out.println("PlayerID: " + PlayerID+"\n");
			//we have 4 different senarios here, 1- which in config file it is app1=1 and app2=0:
			// in this part we lean bayesnets and then learn ct on the individual, but use cp's for generic bayesnets
			// in config file the case for app1=0 app2=1
			//learn bayesnets for individual, but replace pathbayesnets with generic, learn ct's basyed on this new path_bayesnets
			//but for the individual, cp's for generic and score
			//app1=0 app2=0 we learn everything for individual.
			//just use the generic pathbayesnets and everuthing stays the same
			if(app1.equals("1")&& app2.equals("0")){
			/*	TestScoreComputation.CallBBLearner();
				//June 24 2014, where we ground teamID, for relation of appearsPlayer(P,M) we don't have a way to effect the groundings
				//so we just add appearsTeam(T,m) as a parent for the nodes that only have appearsPlayer(p,m) as parent
				TestScoreComputation.CallCTComputation();
				  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
				  st.execute("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
			    	ArrayList<String> children = new ArrayList<String>();
			    	ArrayList<String> rchains = new ArrayList<String>();
			    	System.out.println("select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					ResultSet rspath = st.executeQuery( "select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					while ( rspath.next() )
					{
						String child = rspath.getString( 1 );
						children.add( child );
						
						String rchain = rspath.getString( 2 );
						rchains.add( rchain );
						
					}
					
					rspath.close();
					
					int len = rchains.size();
					for ( int jj = 0; jj < len; jj++ )
					{
						String child = children.get( jj );
						String rchain = rchains.get( jj );
						System.out.println("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 st.execute("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 
					}
				  TestScoreComputation.CallCPComputation();
					if(ELD1.equals("1")) {
				  TestScoreComputation.CallELDComputation();
				  }
				  TestScoreComputation.ScoreComputation();*/
				TestScoreComputation.CallBBLearner();
			//	  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
				//  st.execute("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
		    	ArrayList<String> children = new ArrayList<String>();
		    	ArrayList<String> rchains = new ArrayList<String>();
		    	System.out.println("select distinct "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
						".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
				ResultSet rspath = st.executeQuery( "select distinct "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
						".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
				while ( rspath.next() )
				{
					String child = rspath.getString( 1 );
					children.add( child );
					
					String rchain = rspath.getString( 2 );
					rchains.add( rchain );
					
				}
				
				rspath.close();
				
				int len = rchains.size();
				for ( int jj = 0; jj < len; jj++ )
				{
					String child = children.get( jj );
					String rchain = rchains.get( jj );
					System.out.println("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
							 "', '"+child+"', '`b`')");
					 st.execute("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
							 "', '"+child+"', '`b`')");
					 
				}
				TestScoreComputation.CallCTComputation();
				TestScoreComputation.CallCPComputation();
				if(ELD1.equals("1")) {
				TestScoreComputation.CallELDComputation();}
				
				TestScoreComputation.ScoreComputation();

			}
			
			
			
			else if(app1.equals("0")&& app2.equals("1")){
				TestScoreComputation.CallBBLearner();
		
				  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");//replace with truncate
				  System.out.println("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
				  st.execute("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
					
			    	ArrayList<String> children = new ArrayList<String>();
			    	ArrayList<String> rchains = new ArrayList<String>();
			    	System.out.println("select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					ResultSet rspath = st.executeQuery( "select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					while ( rspath.next() )
					{
						String child = rspath.getString( 1 );
						children.add( child );
						
						String rchain = rspath.getString( 2 );
						rchains.add( rchain );
						
					}
					
					rspath.close();
					
					int len = rchains.size();
					for ( int jj = 0; jj < len; jj++ )
					{
						String child = children.get( jj );
						String rchain = rchains.get( jj );
						System.out.println("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 st.execute("insert  IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 
					}
					
				//TestScoreComputationPart1_1.main(null);
				  TestScoreComputation.CallCTComputation();
				  
					 String largest_rchain="";
			         Statement st_largest=con3.createStatement();
			         ResultSet rs_largest= st_largest.executeQuery(" Select name as Rchain from "+databaseName4+"_BN"+".lattice_set where length=( SELECT max(length) FROM "+databaseName4+"_BN"+".lattice_set); ");
			         rs_largest.absolute(1);
			         largest_rchain = rs_largest.getString(1);
			     	//System.out.println("\n largest_rchain : " + largest_rchain);
			         st_largest.close();
					
					/*
					 * Initialize local variables
					 */
			     	ArrayList<String> fids = new ArrayList<String>();
					rchain = largest_rchain.replace( "`", "" );
					ResultSet rs = st.executeQuery( "SELECT DISTINCT child FROM " + 
							databaseName4 +"_BN" +".Path_BayesNets"  +
							" WHERE rchain = '`" + rchain + "`' and child not in (select rnid from " +databaseName4+"_BN"+".RNodes);" );
					while ( rs.next() )
					{
						String fid = rs.getString( 1 ).replace( "`", "" );
						fids.add( fid );
					}
					
					rs.close();
					int len2 = fids.size();
					for ( int ii = 0; ii < len2; ii++ )
					{
						String fid = fids.get( ii );
						System.out.println("create table "+databaseName4+"_"+PlayerID+"_db"+".`"+fid+"_local_CP`"+" like "+databaseName4+"_db"+".`"+fid+"_local_CP`");
						st.execute("create table "+databaseName4+"_"+PlayerID+"_db"+".`"+fid+"_local_CP`"+" like "+databaseName4+"_db"+".`"+fid+"_local_CP`");
						st.execute("insert into  "+databaseName4+"_"+PlayerID+"_db"+".`"+fid+"_local_CP`"+" select * from "+databaseName4+"_db"+".`"+fid+"_local_CP`");
					}
				//  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".FNodes");
				  st.execute("insert ignore into "+databaseName4+"_"+PlayerID+"_BN"+".FNodes select * from " +databaseName4+"_BN.FNodes");
				//  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes");
				  st.execute("insert ignore into "+databaseName4+"_"+PlayerID+"_BN"+".RNodes select * from " +databaseName4+"_BN.RNodes");
			//	  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
				  if(ELD1.equals("1")) {
				  TestScoreComputation.CallELDComputation();}
				  TestScoreComputation.ScoreComputation();
				  ArrayList<String> localCPs = new ArrayList<String>();
				  System.out.println("create schema "+databaseName4+"_"+PlayerID+"_db_01;");
				  st.execute("drop schema if exists "+databaseName4+"_"+PlayerID+"_db_01;");
				  st.execute("create schema "+databaseName4+"_"+PlayerID+"_db_01;");
					ResultSet rst = st.executeQuery("select distinct child from "+databaseName4+"_BN"+".Path_BayesNets where child not in (select" +
							" rnid from "+databaseName4+"_BN.RNodes)");
					
					 while (rst.next()) {
						 localCPs.add(rst.getString(1));
					 }
					 for (int iii=0; iii<localCPs.size(); ++iii) {
						 String s= localCPs.get(iii).substring(1, localCPs.get(iii).length() - 1);
						 System.out.println("create table "+databaseName4+"_"+PlayerID+"_db_01"+"."+s+"_local_CP as select * from "+databaseName4+"_"+PlayerID+"_db"+"."+s+"_local_CP");
						 st.execute("Drop table if exists "+databaseName4+"_"+PlayerID+"_db_01"+".`"+s+"_local_CP`");
						 st.execute("create table "+databaseName4+"_"+PlayerID+"_db_01"+".`"+s+"_local_CP` as select * from "+databaseName4+"_"+PlayerID+"_db"+".`"+s+"_local_CP`");
					 }
			}
			else if(app2.equals("0") && app1.equals("0") ){
				TestScoreComputation.CallBBLearner();
				  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
				  st.execute("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
		    	ArrayList<String> children = new ArrayList<String>();
		    	ArrayList<String> rchains = new ArrayList<String>();
		    	System.out.println("select distinct "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
						".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
				ResultSet rspath = st.executeQuery( "select distinct "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
						".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
						".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
						+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
				while ( rspath.next() )
				{
					String child = rspath.getString( 1 );
					children.add( child );
					
					String rchain = rspath.getString( 2 );
					rchains.add( rchain );
					
				}
				
				rspath.close();
				
				int len = rchains.size();
				for ( int jj = 0; jj < len; jj++ )
				{
					String child = children.get( jj );
					String rchain = rchains.get( jj );
					System.out.println("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
							 "', '"+child+"', '`b`')");
					 st.execute("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
							 "', '"+child+"', '`b`')");
					 
				}
				TestScoreComputation.CallCTComputation();
				TestScoreComputation.CallCPComputation();
				if(ELD1.equals("1")) {
				TestScoreComputation.CallELDComputation();}
				
				TestScoreComputation.ScoreComputation();

			}
			else{
				TestScoreComputation.CallBBLearner();
				//June 24 2014, where we ground teamID, for relation of appearsPlayer(P,M) we don't have a way to effect the groundings
				//so we just add appearsTeam(T,m) as a parent for the nodes that only have appearsPlayer(p,m) as parent
				  st.execute("create table "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets_Copy as select * from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
				  st.execute("delete from "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets");
			
				  st.execute("insert into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets select * from " +databaseName4+"_BN.Path_BayesNets");
			    	ArrayList<String> children = new ArrayList<String>();
			    	ArrayList<String> rchains = new ArrayList<String>();
			    	System.out.println("select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					ResultSet rspath = st.executeQuery( "select distinct "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child, "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.Rchain from "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes, "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets where "+databaseName4+"_"+PlayerID+"_BN"+
							".Path_BayesNets.child="+databaseName4+"_"+PlayerID+"_BN"+
							".RNodes_2Nodes.2nid and "+databaseName4+"_"+PlayerID+"_BN"+".RNodes_2Nodes.rnid='`a`' and "
							+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets.parent <>'`b`'");
					while ( rspath.next() )
					{
						String child = rspath.getString( 1 );
						children.add( child );
						
						String rchain = rspath.getString( 2 );
						rchains.add( rchain );
						
					}
					
					rspath.close();
					
					int len = rchains.size();
					for ( int jj = 0; jj < len; jj++ )
					{
						String child = children.get( jj );
						String rchain = rchains.get( jj );
						System.out.println("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 st.execute("insert IGNORE into "+databaseName4+"_"+PlayerID+"_BN"+".Path_BayesNets VALUES ('"+rchain+
								 "', '"+child+"', '`b`')");
						 
					}
				  TestScoreComputation.CallCTComputation();
				  TestScoreComputation.CallCPComputation();
				  if(ELD1.equals("1")) {  TestScoreComputation.CallELDComputation();}
				  TestScoreComputation.ScoreComputation();
			}
			System.out.println("*** " + opt1);
			ResultSet rst = st.executeQuery("SELECT max(length) FROM "+databaseName4+"_"+PlayerID+"_BN"+ ".lattice_set;");
	        rst.absolute(1);
	        maxNumberOfMembers = rst.getInt(1);
	        ResultSet rst1 = st.executeQuery("select name from  "+databaseName4+"_"+PlayerID+"_BN"+ ".lattice_set where length = " +maxNumberOfMembers+";");
	        rst1.absolute(1);
	        Statement initst = con4.createStatement();
	        String rchain = rst1.getString(1);
	      IdenticalityCheck.IdenticalityCheck();
	  //   initst.execute("insert into `Premier_League_Strikers_UnionBN`.`IDS` Values ("+PlayerID+")");
	   //  initst.execute("create table Premier_League_Strikers_UnionBN.Path_BayesNets_"+PlayerID+" as select * from "+databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets");
//		initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_BN");
//			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_CT");
//		initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db");
//		initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db_01");
	     initst.execute("alter table "+databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets add PlayerID varchar(20);");
			initst.execute("update " +databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets set PlayerID = " + PlayerID +";");
			initst.execute("alter table " +databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets drop primary key;");
			initst.execute("alter table " +databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets add primary key(PlayerID,Rchain,child,parent);");
			System.out.println(rchain+"***************");
			System.out.println("insert into " + resultdataBase + ".Path_BayesNets select * from "+databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets where Rchain = '"+rchain+"';");
		
						initst.execute("insert into " + resultdataBase + ".Path_BayesNets select * from "+databaseName4+"_"+PlayerID+"_BN"+ ".Path_BayesNets where Rchain = '"+rchain+"';");
		
			initst.execute("alter table "+databaseName4+"_"+PlayerID+"_BN"+ ".`FID_a,b,c_Scores` add PlayerID varchar(20);");
			initst.execute("update " +databaseName4+"_"+PlayerID+"_BN"+ ".`FID_a,b,c_Scores` set PlayerID = " + PlayerID +";");
			initst.execute("alter table " +databaseName4+"_"+PlayerID+"_BN"+ ".`FID_a,b,c_Scores` drop primary key;");
			initst.execute("alter table " +databaseName4+"_"+PlayerID+"_BN"+ ".`FID_a,b,c_Scores` add primary key(PlayerID,FID);");
		initst.execute("insert  into " + resultdataBase + ".Scores select * from "+databaseName4+"_"+PlayerID+"_BN"+ ".`FID_a,b,c_Scores` where PlayerID is not null;");
		initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_BN");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_CT");
			initst.execute("drop schema if exists  " +databaseName4+"_"+PlayerID+"_db");
			initst.close();
		    rst.close();
	        rst1.close();
	        st.close();
	 //       disconnectDB();
	 //       connectDB();
	//        connectDBIND(PlayerID);
	 //       disconnectDBIND(PlayerID);
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
		
		String CONN_STR3 = "jdbc:" + dbaddress + "/" + databaseName4+"_BN";
		try {
			java.lang.Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			System.err.println("Unable to load MySQL JDBC driver");
		}
		con3 = (Connection) DriverManager.getConnection(CONN_STR2, dbUsername, dbPassword);
		
	}

	private static int disconnectDB()
	{
		try
		{
			con4.close();
			con3.close();
		}
		
		catch ( SQLException e )
		{
			System.out.println( "Failed to close database connection." );
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}

	public static ArrayList<String> PlayerTeamW(String TeamID) throws SQLException {
		
		ArrayList<String> PlayerList = new ArrayList<String>();
		Statement st1 = con4.createStatement();
		System.out.println("sa");
		//where PlayerID not in (select PlayerID from PPLPositvePlayer.Path_BayesNets)
		ResultSet rs = st1.executeQuery(" select PlayerID from Premier_League_MidFielder_Dec2015.`Players`;" );
//		ResultSet rs = st1.executeQuery(" select distinct PlayerID from `Premier_League_Synthetic_Bernoulli_Feature_00_bk`.`Scores`;" );
//		ResultSet rs = st1.executeQuery(" SELECT * FROM PremierLeagure_Goalies.Players;" );
		//ResultSet rs = st1.executeQuery(" select movieid  from movies where movieid not in (select PlayerID from imdb_MovieLens_Comedy_01.);" );
//	ResultSet rs = st1.executeQuery("select distinct PlayerID from Players;" );
	//	ResultSet rs = st1.executeQuery("select distinct PlayerID from Premier_League_2011.Scores where PlayerID not in (select PlayerID from Premier_League_2011.Scores) ;" );
		
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
		opt3 = conf.getProperty("LinkAnalysis");
		kld=conf.getProperty("ComputeKLD");
		cont = conf.getProperty("Continuous");
		indi = conf.getProperty("Individual");
		neg = conf.getProperty("Negative");
		app1=conf.getProperty("Approach1");
		app2=conf.getProperty("Approach2");
		background=conf.getProperty("background");
		GeneralPathBayesNets=conf.getProperty("GeneralPathBayesNets");
		OriginalDB=conf.getProperty("OriginalDB");
		databaseName4=conf.getProperty("OriginalDB");
		resultdataBase=conf.getProperty("resultdataBase");
		GenericDB=conf.getProperty("GenericDB");
		ELD1=conf.getProperty("ELD");
		//OriginalDB
		//OutputDB=Premier_League_2011_3_db
		
	}
	
	public static int loadScoreConfig()
	{
		Properties configFile = new java.util.Properties();
		FileReader fr = null;
		
		try
		{
			//fr = new FileReader( "/local-scratch/SRiahi/workspace/TestBayesBase/cfg/scorecomputation2.cfg" );
			fr = new FileReader( "/local-scratch/SRiahi/workspaceNew/BayesBaseNew/cfg/groundcfg/scorecomputation.cfg" ); // use the global version, June 16, 2014
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
			ELD2 = configFile.getProperty( "ELD" );

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
//			fr = new FileReader( "/local-scratch/SRiahi/workspace/TestBayesBase/cfg/subsetctcomputatio2.cfg" );
			fr = new FileReader( "/local-scratch/SRiahi/workspaceNew/BayesBaseNew/cfg/groundcfg/subsetctcomputation.cfg" ); // use the global version, June 16, 2014

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
			dbschema = configFile.getProperty( "dbschema" );//add dbschema Jun 18
			dbcounts = configFile.getProperty( "dbcounts" );
			dbcondprob = configFile.getProperty( "dbcounts" );
			ELD3 = configFile.getProperty( "ELD" );
			
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