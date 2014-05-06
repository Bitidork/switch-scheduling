package network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import util.Tuple;

public final class Network<T extends Message> {
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
	public Network( final Scheduler<T> scheduler ) {
		this.flows = new HashMap<Tuple<Node<? extends T>, Node<? extends T>>, Flow<? extends T>>( );
		this.scheduler = scheduler;
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
}
