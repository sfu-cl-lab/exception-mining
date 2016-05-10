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
public class DeleteSchemas {
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
		
		System.out.println(indi);
		}

}
