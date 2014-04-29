package network;
import java.util.HashSet;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * A node that uses the FIFO policy when scheduling messages.
 * <p>
 * Only implements {@link #scheduleMessage(int, Node, Message)} and {@link #sendOutboundMessages(int)}.
 * @author Bitidork
 *
 * @param <T> A message type.
 */
public abstract class FIFONode<T extends Message> extends Node<T> {	
	@Override
	public void scheduleMessage(int time, Node<T> source, T message) {
		Queue<T> queue = this.inputQueues.get( source );
		if ( queue == null ) { 
			this.inputQueues.put( source, queue = new LinkedList<T>( ) );
			this.inverseInputQueues.put( queue, source );
		}
		
		queue.add( message );
	}

	@Override
	public void sendOutboundMessages( final int time) {
		this.scheduler.scheduleSwitch( time, this );
	}
	
	/**
	 * Constructs a node with empty input queues and the supplied scheduler.
	 * @param scheduler The scheduler this switch will use.
	 */
	public FIFONode( final FIFOScheduler<T> scheduler ) {
		this.inputQueues = new HashMap<Node<T>, Queue<T>>( );
		this.inverseInputQueues = new HashMap<Queue<T>, Node<T>>( );
		this.scheduler = scheduler;
	}
	
	/**
	 * Gets the nodes on the input of this node for which input is waiting.
	 * <p>
	 * You should never call this outside of a scheduler.
	 * @return Returns the nodes on the input of this node for which input is waiting.
	 */
	protected Set<Node<T>> getAvailableInputNodes( ) {
		return new HashSet<Node<T>>(this.inputQueues.keySet());
	}
	
	/**
	 * Gets the queue of messages that have arrived from <i>node</i> and are waiting to be scheduled.
	 * <p>
	 * You should never call this outside of a scheduler.
	 * @param node The node that is outputting to the returned queue.
	 * @return Returns the queue of messages that have arrived from <i>node</i> and are waiting to be scheduled.
	 * @throws IllegalArgumentException if <i>node</i> is not in {@link #getAvailableInputNodes()}
	 */
	protected Queue<T> getAvailableInputQueue( final Node<T> node ) {
		Queue<T> retVal = this.inputQueues.get( node );
		if ( retVal == null )
			throw new IllegalArgumentException("requested queue had no messages");
		
		return retVal;
	}
	
	/**
	 * Removes the queue from mappings internally.
	 * <p>
	 * You should never call this outside of a scheduler.
	 * @param queue The queue to remove. This queue should be empty.
	 * @throws IllegalArgumentException if the queue was not within any mapping or if it was not empty
	 */
	protected void removeInputQueue( final Queue<T> queue ) {
		Node<T> node = inverseInputQueues.get( queue );
		
		if ( node == null ) 
			throw new IllegalArgumentException("queue not found in mapping");
		else if ( !queue.isEmpty() )
			throw new IllegalArgumentException("queue was not empty");
		
		inverseInputQueues.remove( queue );
		inputQueues.remove( node );
	}
	
	/**
	 * A mapping from nodes that can transmit to this switch and the queue of message waiting to be transmitted.
	 * If the queue of messages is empty, the entry must be removed from the mapping.
	 */
	private HashMap<Node<T>, Queue<T>> inputQueues;
	
	/**
	 * Similar to {@link #inputQueues}, except the set-theoretical inverse.
	 */
	private HashMap<Queue<T>, Node<T>> inverseInputQueues;
	
	/**
	 * The scheduler used by this switch.
	 */
	private FIFOScheduler<T> scheduler;
}
