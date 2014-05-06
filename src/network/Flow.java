package network;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a sequence of hops between a source and a destination.
 * @author Bitidork
 *
 */
public final class Flow<T extends Message> implements Iterable<Node<? extends T>>{
	@Override
	public Iterator<Node<? extends T>> iterator() {
		return this.nodes.iterator();
	}
	
	/**
	 * Gets the sending node of this flow.
	 * @return Returns the sending node of this flow.
	 */
	public Node<? extends T> getSource( ) {
		return this.nodes.getFirst( );
	}
	
	/**
	 * Gets the receiving node of this flow.
	 * @return Returns the receiving node of this flow.
	 */
	public Node<? extends T> getSink( ) {
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
	 * Constructs a flow along the given sequence of nodes requiring the supplied capacity.
	 * @param sequence The sequence of nodes of this flow, starting with the source and ending with the destination.
	 * @param requiredCapacity The number of messages sent along this flow per frame.
	 * @throws IllegalArgumentException if <i>sequence</i> is empty or <i>requiredCapacity</i> is not positive
	 */
	public Flow( final AbstractSequentialList<Node<? extends T>> sequence, final int requiredCapacity ) {
		if ( sequence.isEmpty() )
			throw new IllegalArgumentException("Node sequence was empty");
		
		if ( requiredCapacity <= 0 )
			throw new IllegalArgumentException("Required capacity was not positive");
		
		this.nodes = new LinkedList<Node<? extends T>>( sequence );
		this.requiredCapacity = requiredCapacity;
	}
	
	/**
	 * The sequence of nodes along this flow.
	 */
	private LinkedList<Node<? extends T>> nodes;
	
	/**
	 * The number of messages sent along this flow per frame.
	 */
	private int requiredCapacity;
}
