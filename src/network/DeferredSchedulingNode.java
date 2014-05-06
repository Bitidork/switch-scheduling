package network;

/**
 * A node that routes messages by referring to a scheduler.
 * <p>
 * Implements {@link #scheduleMessage(int, Node, Message)} and {@link #sendOutboundMessages(int)}.
 * @author Bitidork
 *
 * @param <T> A type of message.
 * @see Scheduler
 */
public abstract class DeferredSchedulingNode<T extends Message> extends Node<T> {
	@Override
	protected void scheduleMessage( final int time, final Node<T> source, final T message) {
		this.scheduler.addMessageToSchedule( time, source, this, message);
	}

	@Override
	protected void sendOutboundMessages( final int time ) {
		this.scheduler.scheduleNode( time, this );
	}
	
	/**
	 * Constructs a node that refers to the supplied scheduler.
	 * @param scheduler The scheduler that this node should use.
	 */
	public DeferredSchedulingNode( final Scheduler<T> scheduler ) {
		super( );
		this.scheduler = scheduler;
		this.scheduler.addNode( this );
	}

	/**
	 * A metadata object to be used by this node's associated scheduler.
	 * This is used by a specific {@link Scheduler} implementation.
	 */
	Object tag;
	
	/**
	 * An object used to store the decision structure for this node.
	 * This is used specifically functionality implemented in {@link Scheduler}.
	 * This field should not be used explicitly. Use the interface provided in the {@link Scheduler} class.
	 * @see Scheduler
	 */
	DecisionStructure<T> decisionStructure;
	
	/**
	 * The scheduler that this node uses.
	 */
	Scheduler<T> scheduler;
}
