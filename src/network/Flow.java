package network;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import util.Tuple;

/**
 * Represents a sequence of hops between a source and a destination.
 * @author Bitidork
 *
 */
public final class Flow<T extends Message> implements Iterable<DeferredSchedulingNode<? extends T>>{
	@Override
	public Iterator<DeferredSchedulingNode<? extends T>> iterator() {
		return this.nodes.iterator();
	}
	
	/**
	 * Gets the sending node of this flow.
	 * @return Returns the sending node of this flow.
	 */
	public DeferredSchedulingNode<? extends T> getSource( ) {
		return this.nodes.getFirst( );
	}
	
	/**
	 * Gets the receiving node of this flow.
	 * @return Returns the receiving node of this flow.
	 */
	public DeferredSchedulingNode<? extends T> getSink( ) {
		return this.nodes.getLast( );
	}
	
	/**
	 * Gets the number of messages sent along this flow per time frame.
	 * @return Returns the number of messages sent along this flow per time frame.
	 */
	public int getRequiredCapacity( ) {
		return this.requiredCapacity;
	}
	
	/**
	 * Gets the (source, destination) pair of this flow.
	 * @return Returns the (source, destination) pair of this flow.
	 */
	public Tuple<Node<? extends T>, Node<? extends T>> getEndpoints( ) {
		return this.endpoints;
	}
	
	/**
	 * Constructs a flow along the given sequence of nodes requiring the supplied capacity.
	 * @param sequence The sequence of nodes of this flow, starting with the source and ending with the destination.
	 * @param requiredCapacity The number of messages sent along this flow per frame.
	 * @throws IllegalArgumentException if <i>sequence</i> is empty or <i>requiredCapacity</i> is not positive
	 */
	public Flow( final AbstractSequentialList<DeferredSchedulingNode<? extends T>> sequence, final int requiredCapacity ) {
		if ( sequence.isEmpty() )
			throw new IllegalArgumentException("Node sequence was empty");
		
		if ( requiredCapacity <= 0 )
			throw new IllegalArgumentException("Required capacity was not positive");
		
		this.nodes = new LinkedList<DeferredSchedulingNode<? extends T>>( sequence );
		this.requiredCapacity = requiredCapacity;
		this.endpoints = new Tuple<Node<? extends T>, Node<? extends T>>( this.getSource(), this.getSink() );
	}
	
	public Flow(List<DeferredSchedulingNode<T>> sequence,
			int requiredCapacity ) {
		if ( sequence.isEmpty() )
			throw new IllegalArgumentException("Node sequence was empty");
		
		if ( requiredCapacity <= 0 )
			throw new IllegalArgumentException("Required capacity was not positive");
		
		this.nodes = new LinkedList<DeferredSchedulingNode<? extends T>>( );
		
		for ( DeferredSchedulingNode<? extends T> node : sequence) {
			this.nodes.add( node );
		}
		
		this.requiredCapacity = requiredCapacity;
		this.endpoints = new Tuple<Node<? extends T>, Node<? extends T>>( this.getSource(), this.getSink() );
	}


	private Tuple<Node<? extends T>, Node<? extends T>> endpoints;
	
	/**
	 * The sequence of nodes along this flow.
	 */
	private LinkedList<DeferredSchedulingNode<? extends T>> nodes;
	
	/**
	 * The number of messages sent along this flow per frame.
	 */
	private int requiredCapacity;
}
