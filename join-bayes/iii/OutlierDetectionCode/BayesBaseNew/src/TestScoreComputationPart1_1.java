/*
 * Test run for Score Computation
 * 
 * Author: Kurt Routley
 * Date created: Friday, February 14, 2014
 */

public class TestScoreComputationPart1_1
{
	public static void main( String[] args )
	{
		long t1 = System.currentTimeMillis();
		/*
		 * Setup database
		 */
		try
		{
		//	MakeSetup.runMS();
		}
		catch ( Exception e )
		{
			System.out.println( "Failed to setup database." );
			e.printStackTrace();
			return;
		}
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
		long t3 = System.currentTimeMillis();
		System.out.println( "Learning time: " + ( t3 - t2 ) + "ms" );
		System.out.println( "Current runtime: " + ( t3 - t1 ) + "ms" );
		
		/*
		 * Compute CT
		 */
//		try
//		{
//			callLocal_CT.main(null);
//		}
//		catch ( Exception e )
//		{
//			System.out.println( "Failed to get counts." );
//			e.printStackTrace();
//			return;
//		}

	}
}
