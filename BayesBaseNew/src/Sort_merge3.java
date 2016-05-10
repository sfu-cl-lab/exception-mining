import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
/*
//sort merge version3
//here the compare function is also good
//without concatenating the order by columns

*/
public class Sort_merge3 {
	static Connection con;
	final static String database = "sort_merge";
	
	public static void main(String[] args) throws SQLException, IOException {
		
		connectDB();
		
		String table1="a2_0_star";
		String table2="a2_0_flat";
		String table3="final_table_csv";
		
		long time1=System.currentTimeMillis();
		
		sort_merge(table1,table2,table3,con);
		
		long time2=System.currentTimeMillis();
		
		System.out.println("total time:"+(time2-time1));
		System.out.println(compare_tables(table3,"a2_0_false",con));
		

	}
	
	public static void sort_merge(String table1,String table2, String table3, Connection conn) throws SQLException, IOException
	{
		//System.out.println("sort merge version 3");
		System.out.println("\nGenerating false table by Subtration using Sort_merge, cur_false_Table is : " + table3);
		
		//!!!!!!!!!!!!!!!!!remember to change the path and file name
		File ftemp=new File("sort_merge.csv");
		if(ftemp.exists()) ftemp.delete();
		
		File file=new File("sort_merge.csv");
		FileWriter fileW = new FileWriter(file);
		BufferedWriter output = new BufferedWriter( fileW );
		
		Statement st1=conn.createStatement();
		Statement st2=conn.createStatement();
		
		ArrayList<String> orderList=new ArrayList<String>();
		String order = null;
		/******************************************************************/ 
		//code for getting the order by sequence using TABLE1, IT DOES NOT MATTER WHICH TABLE WE USE AS BOTH TABLES HAVE SAME COLUMNS 
		ResultSet rst=st1.executeQuery("show columns from "+table1+" ;");
		
		while(rst.next())
		{
			orderList.add("`"+rst.getString(1)+"` ");
		}
		
		rst.close();
		
		for(int i=0;i<orderList.size();i++)
		{
			if((orderList.get(i)).contains("MULT"))
			{
				orderList.remove(i);
				break;
			}
		}
		
		if ( orderList.size() > 0 )
		{
			order=" "+orderList.get(0)+" ";
			for(int i=1;i<orderList.size();i++)
			{
				order=order+" , "+orderList.get(i);
			}
		}
		
		/**************************************************************/
		String temp= "MULT decimal ";
		for(int i=0;i<orderList.size();i++)
		{
			temp=temp+" , "+orderList.get(i)+" varchar(45) ";
		}
		
		st1.execute("drop table if exists "+table3+" ;");
		st1.execute("create table "+table3+" ( "+temp+") ;" );
		/***********************************************************************/
		//code for merging the two tables
		long time1=System.currentTimeMillis();
		ResultSet rst1 = null;
		ResultSet rst2 =null;
		
		if ( order != null )
		{
			System.out.println("rst1 : select distinct mult, "+order+" from "+table1+" order by "+order+" ;");
			rst1=st1.executeQuery("select distinct mult, "+order+" from "+table1+" order by "+order+" ;");
			rst2=st2.executeQuery("select distinct mult, "+order+" from "+table2+" order by "+order+" ;");
		}
		else
		{
			System.out.println("rst1 : select distinct mult from "+table1+";");
			rst1=st1.executeQuery("select distinct mult from "+table1+";");
			System.out.println( "rst2 : select distinct mult from "+table2+";" );
			rst2=st2.executeQuery("select distinct mult from "+table2+";");
		}
		
		long time2=System.currentTimeMillis();
		//System.out.print("order by time:"+(time2-time1));
		
		//finding the no. of rows in each table
		int size1=0,size2=0;
		
		while(rst1.next())size1++;
		while(rst2.next())size2++;
		
		//finding the no of columns in a table
		ResultSetMetaData rsmd=(ResultSetMetaData) rst1.getMetaData();
		int no_of_colmns=rsmd.getColumnCount();
		
		int i=1;int j=1;//index variables for both tables
		rst1.absolute(1);rst2.absolute(1);
		long time3=System.currentTimeMillis();
		//merging starting here 
		while(i<=size1&&j<=size2)
		{
			int val1=0,val2=0;
			for(int k=2;k<=no_of_colmns;k++)
			{
				try
				{
					val1=Integer.parseInt(rst1.getString(k));
					val2=Integer.parseInt(rst2.getString(k));
				}
				catch(java.lang.NumberFormatException e)
				{
					
				}
				finally
				{
					if(rst1.getString(k).compareTo(rst2.getString(k))>0)
					{
						val1=1; val2=0;
					}
					else if(rst1.getString(k).compareTo(rst2.getString(k))<0)
					{
						val1=0;val2=1;
					}
				}
				
				if(val1<val2)
				{
					String quer=rst1.getString(1);
					for(int c=2;c<=no_of_colmns;c++)
					{
						quer=quer+"$"+ rst1.getString(c);
					}
					
					output.write((quer)+"\n");
					i++;
					break;
				}
				else if(val1>val2)
				{
					j++;
					break;
				}
			}
			
			if(val1==val2)
			{
				String query="";
				try
				{
					query=query+(Integer.parseInt(rst1.getString(1))-Integer.parseInt(rst2.getString(1)));
				}
				catch ( NumberFormatException e )
				{
					query = query + Integer.parseInt(rst1.getString(1));
				}
				
				for(int c=2;c<=no_of_colmns;c++)
				{
					query=query+"$"+rst1.getString(c);
				}
				
				output.write(query+"\n");
				
				i++;
				j++;
			}
			
			rst1.absolute(i);rst2.absolute(j);
		}
		
		if(i>1)
		{
			rst1.absolute(i-1);
		}
		else
		{
			rst1.beforeFirst();
		}
		
		while(rst1.next())
		{
			String query=rst1.getString(1);
			for(int c=2;c<=no_of_colmns;c++)
			{
				query=query+"$"+rst1.getString(c);
			}
			
			output.write((query)+"\n");
		}
		
		output.close();
		fileW.close();
		long time4=System.currentTimeMillis();
		// System.out.print("\t insert time:"+(time4-time3));
		
		st2.execute("drop table if exists "+ table3+"; ");
		st2.execute("create table " +table3+" like "+ table1+" ;");
		st2.execute("LOAD DATA LOcal INFILE 'sort_merge.csv' INTO TABLE "+ table3 +" FIELDS TERMINATED BY '$' LINES TERMINATED BY '\\n'  ;");
				
		rst1.close();
		rst2.close();
		st1.close();
		st2.close();

		long time5=System.currentTimeMillis();
		//System.out.print("\t export csv file to sql:"+(time5-time4));
		System.out.println("\ntotal time: "+(time5-time1)+"\n");
	}
	 
	 public static boolean compare_tables(String table1 , String table2,Connection conn) throws SQLException{
			
			
			
			
			java.sql.Statement st1=conn.createStatement();
			java.sql.Statement st2=conn.createStatement();
			
			
			ResultSet rst1=st1.executeQuery("show columns from "+table1);
			ResultSet rst2=st2.executeQuery("show columns from "+table2);
			ArrayList<String> list1=new ArrayList<String>();
			ArrayList<String> list2=new ArrayList<String>();
			while(rst1.next()){
			list1.add(rst1.getString(1));
			}
			while(rst2.next()){
				list2.add(rst2.getString(1));
				}
			if(list1.size()!=list2.size()){
				System.out.println("not equal number of columns in "+table1+" and "+table2);
				return false;
			}
			
				for(int i=0;i<list1.size();i++){
				if(!list1.get(i).equals(list2.get(i))){
				System.out.println(list1.get(i)+" "+list2.get(i));	
				System.out.println("coulumns are not same or their order is not same");
				return false;
			}
				}
			rst1.absolute(1);
			String order=rst1.getString(1);
			while(rst1.next()){
				order=order+" , `"+rst1.getString(1)+"`";
			}
			
			 int size1=0;
			 int size2=0;
		     rst1=st1.executeQuery("select distinct * from "+table1+" order by "+order+" ;" );
		     rst2=st2.executeQuery("select distinct * from "+table2+ " order by "+order+" ;");	
		     while(rst1.next())size1++;
		     while(rst2.next())size2++;
		     if(size1!=size2)
		    	 System.out.println("not equal no. of rows in both the tables");
		     
		     rst1.beforeFirst();
		     rst2.beforeFirst();
		     
		     while(rst1.next()&&rst2.next()){
		     String temp1="",temp2="";
		    	
		    for(int i=1;i<=list1.size();i++)
		    {
			temp1=temp1+rst1.getString(i);
			temp2=temp2+rst2.getString(i);
		    }
			if(!temp1.equals(temp2)){
				System.out.println("tables not equal");
				return false;
			  }
		    }
		
		     
		     
	   System.out.println("tables are equal");
	   return true;
	 }
	
	
	public static void connectDB() throws SQLException {
        String CONN_STR = "jdbc:mysql://kripke.cs.sfu.ca/"+database;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception ex) {
            System.err.println("Unable to load MySQL JDBC driver");
        }
        con = (Connection) DriverManager.getConnection(CONN_STR, "root", "alibz");
    }
}