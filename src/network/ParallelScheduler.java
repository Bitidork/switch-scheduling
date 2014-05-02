package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ParallelScheduler<T extends Message> extends SwitchScheduler<ParallelNode<T>, T> {

	@Override
	public void scheduleSwitch( final int time, final ParallelNode<T> node ) {
		ConcurrentHashMap<Pair, Node<T>> decisionMap = this.getDecisionMapping(node);
		
		//for each of four iterations
			//build set of nodes OR voqs that can be scheduled for each unscheduled output
			//each output selects an input/voq at random
			//each input selects an output (at random)to which it has been granted, puts it on the queue of messages to be scheduled
			//remove each scheduled input and output from the pool
		
		/*ConcurrentHashMap<Pair, Node<T>> decisionMap = this.getDecisionMapping( node );
		
		// a mapping for which the domain are used output nodes and the values are a set of queues that can be scheduled for that output
		HashMap<Node<T>, Set<Queue<T>>> assignedQueues = new HashMap<Node<T>, Set<Queue<T>>>( );
		
		// collect queue collections
		for ( Node<T> source : node.getAvailableInputNodes() ) {
			Queue<T> queue = node.getAvailableInputQueue( source );
			T head = queue.poll( );
			
			@SuppressWarnings("unchecked")
			Node<T> nextHop = decisionMap.get( new Pair((Node<T>)head.getSource(), (Node<T>)head.getDestination()) );
			
			if ( nextHop == null )
				throw new IllegalStateException("Got a (source, switch, destination) triple that could not be routed");
			
			Set<Queue<T>> range = assignedQueues.get( nextHop );
			if ( range == null )
				assignedQueues.put( nextHop, range = new HashSet<Queue<T>>( ) );
		}
		
		Random rng = new Random( );
		// transmit
		for ( Node<T> output : assignedQueues.keySet() ) {
			List<Queue<T>> waitingList = new ArrayList<Queue<T>>(assignedQueues.get( output ));
			Queue<T> queue = waitingList.get( rng.nextInt( waitingList.size() ) );
			
			T head = queue.remove( );
			
			// remove from node available queues
			if ( queue.isEmpty() )
				node.removeInputQueue( queue );
			
			@SuppressWarnings("unchecked")
			Node<T> nextHop = decisionMap.get( new Pair((Node<T>)head.getSource(), (Node<T>)head.getDestination()) );
			
			node.transmitToNode( time, nextHop, head );
		}
		*/
	}
	
	/**
	 * Causes messages at <i>node</i> that originated from <i>source</i> and are destined for <i>destination</i> to transmit to <i>nextHop</i>.
	 * @param source The message source.
	 * @param node The switch the message is at.
	 * @param destination The message destination.
	 * @param nextHop The next node to transmit the message to.
	 */
	public void putDecision( final Node<T> source, final ParallelNode<T> node, final Node<T> destination, final Node<T> nextHop ) {
		ConcurrentHashMap<Pair, Node<T>> m = mapping.get( node );
		if ( m == null )
			mapping.put( node, m = new ConcurrentHashMap<Pair, Node<T>>( ));
		
		m.put( new Pair(source, destination), nextHop );
	}
	
	/**
	 * Gets the decision mapping used for the supplied node.
	 * @param node The switch to get the mapping for.
	 * @return Returns the decision mapping used for the supplied node.
	 * @throws IllegalArgumentException if the supplied switch was not found in the schedule
	 */
	private ConcurrentHashMap<Pair, Node<T>> getDecisionMapping( final ParallelNode<T> node ) {
		ConcurrentHashMap<Pair, Node<T>> m = mapping.get( node );
		if ( m == null )
			throw new IllegalArgumentException("Supplied node was not under the domain of this scheduler");
		
		return m;
	}
	
	/**
	 * Constructs a FIFOScheduler with an empty source-switch-destination to next hop mapping.
	 */
	public ParallelScheduler( ) {
		this.mapping = new ConcurrentHashMap<ParallelNode<T>, ConcurrentHashMap<Pair, Node<T>>>( );
	}
	
	/**
	 * Effectively a mapping of (source, switch, destination) triplets to the node for the next hop.
	 */
	private ConcurrentHashMap<ParallelNode<T>, ConcurrentHashMap<Pair, Node<T>>> mapping;
	
	
	/**
	 * A (source, destination) tuple.
	 * @author Bitidork
	 *
	 */
	private class Pair {
		public Node<T> source, destination;
		
		public Pair( final Node<T> source, final Node<T> destination ) {
			this.source = source;
			this.destination = destination;
		}
		
		@Override
		public int hashCode( ) {
			return this.source.hashCode() << 16 | this.destination.hashCode();
		}
		
		@Override
		public boolean equals( Object ocomp ) {
			if ( ocomp == null || !(ocomp instanceof ParallelScheduler.Pair ) )
				return false;
			@SuppressWarnings("unchecked")
			Pair comp = (Pair)ocomp;
			return this.source.equals(comp.source) && this.destination.equals(comp.destination);
		}
	}
}
