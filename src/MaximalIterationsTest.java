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
	 */
	public static void main(String[] args) {
		final int N = 64;
		final int iterations = 500;
		
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
		for ( int i = 0; i < iterations; i++ ) {
			ParallelSchedulerResult<Message> res = ParallelScheduler.createProgram(0, availableVOQs, 0);
			maximalIterations.add( res.iterations );
		}
		
		BigDecimal sum = BigDecimal.valueOf( 0 );
		for ( Integer i : maximalIterations ) {
			sum = sum.add( BigDecimal.valueOf( i ) );
		}
		
		BigDecimal mean = sum.divide( BigDecimal.valueOf( maximalIterations.size() ) );
		
		System.out.println( "Average number of iterations for a maximal matching (" + N + " by " + N +"): " + mean );
	}

}
