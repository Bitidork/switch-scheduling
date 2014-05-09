import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import network.FIFOScheduler;
import network.Node;
import network.Message;
import network.ParallelScheduler;
import network.ParallelScheduler.ParallelSchedulerResult;
import network.Scheduler;
import network.Switch;
import util.Tuple;


public final class MaximalIterationsTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		final int iterations = 1000;
		
		File f = new File( "data/maximal_testing.m ");
		if ( f.exists() )
			f.delete();
		
		f.createNewFile();	
		f.setWritable( true );
		
		final int[] testedN = new int[] { 4, 8, 16, 32, 64, 128, 256 };
		
		PrintStream fos = new PrintStream( f );
		try {
			fos.print( "testedN = [" );
			for ( int j = 0; j < testedN.length; j++ ) {
				fos.print( testedN[j] );
				
				if ( j != testedN.length ) {
					fos.print( "," );
				}
			}
			fos.println( "];" );
			
			fos.print( "testData = [" );
			for ( int j = 0; j < testedN.length; j++ ) {
				final int N = testedN[j]; 
				
				Scheduler<Message> scheduler = new FIFOScheduler<Message>( );
				Set<Tuple<Node<Message>, Node<Message>>> availableVOQs = new HashSet<Tuple<Node<Message>, Node<Message>>>( );
				
				Set<Node<Message>> nodes = new HashSet<Node<Message>>( );
				
				for ( int i = 0; i < N; i++ ) {
					nodes.add( new Switch<Message>( scheduler ) );
				}
				
				for ( Node<Message> node : nodes ) {
					for ( Node<Message> node2 : nodes ) {
						if ( node == node2 )
							continue;
						Tuple<Node<Message>, Node<Message>> edge = new Tuple<Node<Message>, Node<Message>>( node, node2 );
						availableVOQs.add( edge );
					}
				}
				
				LinkedList<Integer> maximalIterations = new LinkedList<Integer>( );
				fos.print( "[" );
				for ( int i = 0; i < iterations; i++ ) {
					ParallelSchedulerResult<Message> res = ParallelScheduler.createProgram(0, availableVOQs, 0);
					maximalIterations.add( res.iterations );
					fos.print( res.iterations );
					
					if ( i != iterations )
						fos.print( "," );
				}
				fos.print( "]" );
				if ( j != testedN.length )
					fos.println( ";..." );
				
				BigDecimal sum = BigDecimal.valueOf( 0 );
				for ( Integer i : maximalIterations ) {
					sum = sum.add( BigDecimal.valueOf( i ) );
				}
				
				BigDecimal mean = sum.divide( BigDecimal.valueOf( maximalIterations.size() ), 2, BigDecimal.ROUND_HALF_DOWN );
				
				System.out.println( "Average number of iterations for a maximal matching (" + N + " by " + N +"): " + mean );
			}
			fos.println( "];" );
		} finally {
			fos.close();
		}
	}

}
