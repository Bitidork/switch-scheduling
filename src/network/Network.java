package network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import util.Tuple;

public abstract class Network<T extends Message> {
	/**
	 * Called in {@link #run(int)}, before nodes are updated.
	 */
	public abstract void prePhase( );


	/**
	 * Called in {@link #run(int)}, after nodes are updated.
	 */
	public abstract void postPhase( );
	
	/**
	 * Updates this network over the supplied number of frames.
	 * @param frames The number of frames to run for.
	 */
	public void run( final int frames ) {
		int iterations = Constants.FRAME_SIZE * frames;
		
		this.prePhase( );
		
		for ( int i = 0; i < iterations; i++ ) {
			for ( Node<? extends T> node : nodes )
				node.update( i );
		}
		
		this.postPhase( );
	}
	
	/**
	 * Gets the set of nodes in this network.
	 * @return Returns the set of nodes in this network.
	 */
	public Set<Node<? extends T>> getNodes( ) {
		return Collections.unmodifiableSet(this.nodes);
	}
	
	/**
	 * Gets the collection of flows in this network.
	 * @return Returns the collection of flows in this network.
	 */
	public Collection<Flow<? extends T>> getFlows( ) {
		return Collections.unmodifiableCollection(flows.values( ));
	}
	
	/**
	 * Gets the set of (source,sink) pairs of flows in this network.
	 * @return Returns the set of (source,sink) pairs of flows in this network.
	 */
	public Set<Tuple<Node<? extends T>, Node<? extends T>>> getFlowTerminals( ) {
		return Collections.unmodifiableSet(flows.keySet( ));
	}
	
	/**
	 * Gets the flow associated with the given (source, sink) pair.
	 * @param terminals The (source, sink) pair.
	 * @return Returns the flow associated with the given (source, sink) pair, or null if no such flow exists.
	 */
	public Flow<? extends T> getFlow( final Tuple<Node<? extends T>, Node<? extends T>> terminals ) {
		return flows.get( terminals );
	}

	/**
	 * Gets the flow associated with the given (source, sink) pair.
	 * @param source The source terminal of the flow.
	 * @param sink The sink terminal of the flow.
	 * @return Returns the flow associated with the given (source, sink) pair, or null if no such flow exists.
	 */
	public Flow<? extends T> getFlow( final Node<? extends T> source, final Node<? extends T> sink ) {
		return flows.get( new Tuple<Node<? extends T>, Node<? extends T>>( source, sink ) );
	}
	
	/**
	 * Constructs an empty network.
	 */
	public Network( final Scheduler<T> scheduler, final Random rng ) {
		this.flows = new HashMap<Tuple<Node<? extends T>, Node<? extends T>>, Flow<? extends T>>( );
		this.nodes = new HashSet<Node<? extends T>>( );
		this.scheduler = scheduler;
		this.rng = rng;
	}
	
	/**
	 * Removes the supplied flow from the network, if it exists.
	 * @param flow The flow.
	 */
	void removeFlow( Flow<T> flow ) {
		if ( flows.containsKey(flow.getEndpoints()) ) {
			flows.remove( flow.getEndpoints() );
			
			for ( DeferredSchedulingNode<? extends T> node : flow ) {
				scheduler.removeDecision( node, flow.getSource(), flow.getSink() );
			}
		}
	}
	
	/**
	 * Registers the supplied flow from the network.
	 * If it already exists, simply replaces it.
	 * @param flow The flow.
	 */
	void addFlow( Flow<T> flow ) {
		if ( flows.containsKey( flow.getEndpoints() ) )
			this.removeFlow( flow );
		
		flows.put( flow.getEndpoints(), flow );
		
		DeferredSchedulingNode<? extends T> previousNode = flow.getSource();
		for ( DeferredSchedulingNode<? extends T> node : flow ) {
			scheduler.putDecision(previousNode, flow.getSource(), flow.getSink(), node);
			previousNode = node;
		}
	}
	
	/**
	 * A mapping of (sender,receiver) tuples to the flow object between them.
	 */
	HashMap<Tuple<Node<? extends T>, Node<? extends T>>, Flow<? extends T>> flows;
	
	/**
	 * The set of nodes in this network.
	 */
	Set<Node<? extends T>> nodes;
	
	/**
	 * The scheduler this network uses.
	 */
	Scheduler<T> scheduler; 
	
	/**
	 * The random number generator for this network to use.
	 */
	Random rng;
}
