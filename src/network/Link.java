package network;

import java.util.Queue;

import network.Node;
import network.InputLink;
import network.OutputLink;
import network.Message;

/**
 * Represents a link that can:
 * <ul>
 * <li>Send a {@link network.Message} of type <i>T</i> from the source
 * <li>Receive a {@link network.Message} of type <i>T</i> at the sink
 * <li>Determine whether it is busy or otherwise unable to transmit
 * <li>Determine which {@link network.Node}s are connected to this link.
 * <li>Determine how long it takes to transmit a specific {@link network.Message} over this link.
 * </ul>
 * @author Bitidork
 * @see network.Node
 *
 * @param <T> The type of message this link transfers.
 */
public abstract class Link<T extends Message> {
	/**
	 * Constructs a Link with the given source and sink.
	 * @param source The {@link network.Node} that transmits data over this link.
	 * @param sink The {@link network.Node} that receives data over this link.
	 * @throws IllegalArgumentException if the source or sink node are null.
	 */
	public Link( final Node<T> source, final Node<T> sink ) {
		if ( source == null ) 
			throw new IllegalArgumentException("Source node is null");
		
		if ( sink == null )
			throw new IllegalArgumentException("Sink node is null");
		
		this.source = source;
		this.sink = sink;
		
		// delay creation of input and output links, for the InputLink and OutputLink classes.
		this.inputLink = null;
		this.outputLink = null;
	}
	
	/**
	 * Transmits object <i>data</i> from the source to the sink.
	 * <p>
	 * This method should be implemented in such a way that does not presume on the order of delivery.
	 * In other words, a source may send data before the receiver accepts data on the same time slot, but that data should not be retrieved in <i>receive</i>.
	 * <p>
	 * This method must be reentrant.
	 * @param time The time at which transmission should start.
	 * @param data The data to transmit over the link.
	 * @throws IllegalArgumentException if <i>data</i> is null
	 * @throws IllegalStateException if this link is unable to transmit <i>data</i>
	 */
	public abstract void transmit( final int time, final T data ) throws IllegalStateException;
	
	/**
	 * Pulls the data that is ready to be received at the start of <i>time</i> into a <i>Queue</i>.
	 * <p>
	 * This method should be implemented in such a way that does not presume on the order of delivery.
	 * In other words, a source may send data before the receiver accepts data on the same time slot, but that data should not be retrieved in <i>receive</i>.
	 * <p>
	 * This method must be reentrant.
	 * @param time The time at which the receiver attempts to pull data.
	 * @return Returns a <i>Queue</i> of data items in the order they were transmitted, up to the start of time slot <i>time</i>.
	 * @throws IllegalStateException if this link is unable to receive data
	 */
	public abstract Queue<T> receive( final int time ) throws IllegalStateException;
	
	/**
	 * Determines whether or not data may be transmitted at the start of the supplied time slot.
	 * @param time The time slot to perform this check at.
	 * @return Returns true if and only if data can be transmitted at the start of time slot <i>time</i>.
	 */
	public abstract boolean canTransmit( final int time );
	
	/**
	 * Calculates the amount of time needed to transmit <i>message</i> over this link.
	 * The value returned must be strictly positive (greater than zero).
	 * @param message The message to transmit over this link.
	 * @return Returns the amount of time needed to transmit <i>data</i> over this link.
	 */
	public abstract int getTransmissionTime( final T message );
	
	/**
	 * Gets the <i>Node</i> that transmits data over this link.
	 * @return The <i>Node</i> that transmits data over this link.
	 */
	public Node<T> getSource( ) {
		return this.source;
	}
	
	/**
	 * Gets the <i>Node</i> that receives data over this link.
	 * @return The <i>Node</i> that receives data over this link.
	 */
	public Node<T> getSink( ) {
		return this.sink;
	}
	
	/**
	 * Returns the {@link network.InputLink} for this link.
	 * @return Returns the {@link network.InputLink} for this link.
	 * @throws IllegalStateException if the {@link network.InputLink} could not be returned or created
	 */
	public InputLink<T> getInputLink( ) {
		if ( this.inputLink == null )
			this.inputLink = new InputLink<T>( this );
		
		return this.inputLink;
	}
	
	/**
	 * Returns the {@link network.OutputLink} for this link.
	 * @return Returns the {@link network.OutputLink} for this link.
	 * @throws IllegalStateException if the {@link network.OutputLink} could not be returned or created
	 */
	public OutputLink<T> getOutputLink( ) {
		if ( this.outputLink == null )
			this.outputLink = new OutputLink<T>( this );
		
		return this.outputLink;
	}

	/**
	 * The {@link network.Node} that transmits data over this link.
	 */
	private Node<T> source;
	
	/**
	 * The {@link network.Node} that transmits data over this link.
	 */
	private Node<T> sink;
	
	/**
	 * The {@link network.InputLink} for this link.
	 */
	private InputLink<T> inputLink;
	
	/**
	 * The {@link network.OutputLink} for this link.
	 */
	private OutputLink<T> outputLink;
}
