package network;

import network.Node;

/**
 * Acts as a generic message that can be sent through a network.
 * <p>
 * Each message has a source, destination, and timestamp.
 * This class should be extended to define a payload or other statistical information. 
 * @author Bitidork
 *
 */
public class Message {
	/**
	 * Creates a packet heading from <i>source</i> to <i>destination</i> created at time slot <i>time</i>.
	 * @param source The <i>Node</i> that generated the cell.
	 * @param destination The <i>Node</i> that the packet is for.
	 * @param timestamp The time at which the packet was cell.
	 * @throws IllegalArgumentException if source or destination are null
	 * @throws IllegalArgumentException if timestamp is negative
	 */
	@SuppressWarnings("unchecked")
	public Message( final Node<? extends Message> source, final Node<? extends Message> destination, final int timestamp ) {
		if ( source == null )
			throw new IllegalArgumentException("source is null");
		
		if ( destination == null )
			throw new IllegalArgumentException("destination is null");
		
		if ( timestamp < 0 )
			throw new IllegalArgumentException("timestamp is negative");
		
		this.source = (Node<Message>)source;
		this.destination = (Node<Message>)destination;
		this.timestamp = timestamp;
	}
	
	/**
	 * Gets the <i>Node</i> that generated this message.
	 * @return Returns the <i>Node</i> that generated this message.
	 */
	public Node<Message> getSource( ) {
		return this.source;
	}
	
	/**
	 * Gets the <i>Node</i> that this message is for.
	 * @return Returns the <i>Node</i> that this message is for.
	 */
	public Node<Message> getDestination( ) {
		return this.destination;
	}
	
	/**
	 * Gets the time that this message was created.
	 * @return The time that this message was created.
	 */
	public int getTimestamp( ) {
		return this.timestamp;
	}
	
	/**
	 * The <i>Node</i> that generated the message.
	 */
	private Node<Message> source;
	
	/**
	 * The <i>Node</i> the message is for.
	 */
	private Node<Message> destination;
	
	/**
	 * The time at which the message was created.
	 */
	private int timestamp;
}
