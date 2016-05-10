import java.sql.SQLException;


public class TestScoreComputationPart1_2 {
	public static void main( String[] args ) throws SQLException
	{
	if ( SubsetCTComputation.compute_subset_CT( ) != 0  )
	{
		System.out.println( "Failed to get counts." );
		return;
	}
	
	long t4 = System.currentTimeMillis();

	
	/*
	 * Compute CP
	 */
	try
	{
		local_CP.local_CP();
	}
	catch ( Exception e )
	{
		System.out.println( "Failed to compute conditional probabilities." );
		e.printStackTrace();
		return;
	}
	long t5 = System.currentTimeMillis();
	System.out.println( "Computing time: " + ( t5 - t4 ) + "ms" );

	
	/*
	 * Compute Scores
	 */
	if ( ScoreComputation.Compute_FID_Scores() != 0 )
	{
		System.out.println( "Failed to compute FID Scores." );
		return;
	}
	long t6 = System.currentTimeMillis();
	System.out.println( "Computing time: " + ( t6 - t5 ) + "ms" );
}
}
