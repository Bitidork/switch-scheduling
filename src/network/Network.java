package network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import util.Tuple;

/**
 * A generic node-oriented network. Provides utility for adding flows.
 * @author Bitidork
 *
 * @param <T> The type of message.
 */
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
				node.update( previousRunTime + i );
		}
		
		previousRunTime += iterations;
		
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
		this.previousRunTime = 0;
	}
	
	/**
	 * Adds the supplied node to the network.
	 * @param node The node to add.
	 */
	void addNode( final Node<T> node ) {
		this.nodes.add( node );
	}
	
	/**
	 * Removes the supplied flow from the network, if it exists.
	 * @param flow The flow.
	 */
	void removeFlow( final Flow<T> flow ) {
		if ( flows.containsKey(flow.getEndpoints()) ) {
			flows.remove( flow.getEndpoints() );
			
			DeferredSchedulingNode<? extends T> previousNode = flow.getSource();
			DeferredSchedulingNode<? extends T> node = flow.getSource();
			for ( DeferredSchedulingNode<? extends T> nextNode : flow ) {
				// remove decision
				scheduler.removeDecision( node, flow.getSource(), flow.getSink() );

				// get ds
				@SuppressWarnings("unchecked")
				DecisionStructure<T> ds = scheduler.getDecisionStructure((DeferredSchedulingNode<T>)node);
				
				// voq
				@SuppressWarnings("unchecked")
				Tuple<Node<T>, Node<T>> voq = new Tuple<Node<T>, Node<T>>( (Node<T>)previousNode, (Node<T>)nextNode );
				
				// modify reserved capacity
				ds.setReservedCapacity(voq, 0);
				
				previousNode = node;
				node = nextNode;
			}
		}
	}
	
	/**
	 * Registers the supplied flow from the network.
	 * If it already exists, simply replaces it.
	 * @param flow The flow.
	 */
	void addFlow( final Flow<T> flow ) {
		if ( flows.containsKey( flow.getEndpoints() ) )
			this.removeFlow( flow );
		
		flows.put( flow.getEndpoints(), flow );
		
		DeferredSchedulingNode<? extends T> previousNode = flow.getSource();
		DeferredSchedulingNode<? extends T> node = flow.getSource();
		for ( DeferredSchedulingNode<? extends T> nextNode : flow ) {
			// place the decision
			scheduler.putDecision(node, flow.getSource(), flow.getSink(), nextNode);
			
			// get ds
			@SuppressWarnings("unchecked")
			DecisionStructure<T> ds = scheduler.getDecisionStructure((DeferredSchedulingNode<T>)node);
			
			// voq
			@SuppressWarnings("unchecked")
			Tuple<Node<T>, Node<T>> voq = new Tuple<Node<T>, Node<T>>( (Node<T>)previousNode, (Node<T>)nextNode );
			
			// modify reserved capacity
			ds.translateReservedCapacity( voq, flow.getRequiredCapacity() );
			
			previousNode = node;
			node = nextNode;
		}
	}
	
	/**
	 * Gets the scheduler used by this network.
	 * @return Returns the scheduler used by this network.
	 */
	Scheduler<T> getScheduler( ) {
		return this.scheduler;
	}
	
	/**
	 * Gets the RNG used by this network.
	 * @return Returns the RNG used by this network.
	 */
	Random getRNG( ) {
		return this.rng;
	}
	
	/**
	 * A mapping of (sender,receiver) tuples to the flow object between them.
	 */
	private HashMap<Tuple<Node<? extends T>, Node<? extends T>>, Flow<? extends T>> flows;
	
	/**
	 * The set of nodes in this network.
	 */
	private Set<Node<? extends T>> nodes;
	
	/**
	 * The scheduler this network uses.
	 */
	private Scheduler<T> scheduler; 
	
	/**
	 * The random number generator for this network to use.
	 */
	private Random rng;
	
	private int previousRunTime;
}
