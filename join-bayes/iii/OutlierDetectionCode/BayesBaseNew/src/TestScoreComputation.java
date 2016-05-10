import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Test run for Score Computation
 * 
 * Author: Kurt Routley
 * Date created: Friday, February 14, 2014
 */

public class TestScoreComputation
{
	public static void main( String[] args ) throws SQLException
	{
		CallBBLearner();
		CallCTComputation();
		CallCPComputation();
		 CallELDComputation();
	}
	
	public static void CallBBLearner(){
		
		long t1 = System.currentTimeMillis();
		/*
		 * Setup database
		 */
		/*try // uncommen this if you run it for ILP Zqian, June 16,2014
		{
			MakeSetup.runMS();
		}
		catch ( Exception e )
		{
			System.out.println( "Failed to setup database." );
			e.printStackTrace();
			return;
		}*/
		long t2 = System.currentTimeMillis();
		System.out.println( "Setup time: " + ( t2 - t1 ) + "ms" );
		System.out.println( "Current runtime: " + ( t2 - t1 ) + "ms" );
		
		/*
		 * Create Bayes Net
		 */
		try
		{
			RunBB.runBBLearner();
		}
		catch ( Exception e )
		{
			System.out.println( "Failed to learn Bayes Net." );
			e.printStackTrace();
			return;
		}
		
	}
	public static void CallCTComputation() throws SQLException{
		long t3 = System.currentTimeMillis();
	
		
		/*
		 * Compute CT
		 */		
		if ( SubsetCTComputation.compute_subset_CT( ) != 0 )		
		{
			System.out.println( "Failed to get counts." );
			return;
		}
		
		long t4 = System.currentTimeMillis();
		System.out.println( "Counting time: " + ( t4 - t3 ) + "ms" );

		
		
	}
	public static void CallCPComputation() throws SQLException{
		try
		{
			local_CP.local_CP();
			System.out.println("we are here22");
		}
		
		catch ( Exception e )
		{
			System.out.println( "Failed to compute conditional probabilities." );
			e.printStackTrace();
			return;
		}
		
		/*
		 * Compute Scores
		 */
	}	
		public static void CallELDComputation() throws SQLException{
		try
		{
			ELDcomputation.ELDcomputation();
			System.out.println("we are here");
		}
		catch(Exception e){
			System.out.println( "Failed to compute ELD probabilities." );
			e.printStackTrace();
			return;
		}
	}
	public static void ScoreComputation() throws SQLException{
		
		if ( ScoreComputation.Compute_FID_Scores() != 0 )
		{
			System.out.println( "Failed to compute FID Scores." );
			return;
		}
	}
	
}