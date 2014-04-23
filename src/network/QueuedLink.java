package network;

import java.util.LinkedList;
import java.util.Queue;

import network.Node;
import network.Message;

/**
 * A link that implements transmission and reception according to the ordering constraints outlined in {@link network.Link#transmit(int, Message)} and {@link network.Link#receive(int)}.
 * @author Bitidork
 *
 * @param <T> The type of message this link transmits.
 */
public abstract class QueuedLink<T extends Message> extends Link<T> {
	/**
	 * Creates a <i>QueuedLink</i> between the given <i>source</i> and <i>sink</i>
	 * @param source The {@link network.Node} that transmits data over this link.
	 * @param sink The {@link network.Node} that receives data over this link.
	 * @throws IllegalArgumentException if source or sink are null
	 */
	public QueuedLink( final Node<T> source, final Node<T> sink ) {
		super( source, sink );
		
		this.readyQueue = new LinkedList<T>( );
	}
	
	@Override
	public void transmit( final int time, final T data ) throws IllegalStateException {
		synchronized ( this ) {
			if ( !this.canTransmit( time ) )
				throw new IllegalStateException("link is already transmitting");
			
			if ( this.payload != null && this.receivedPayloadTime <= time ) {
				this.readyQueue.add( this.payload );
				this.payload = null;
			}
			
			this.payload = data;
			this.receivedPayloadTime = time + this.getTransmissionTime( data );
			
			if ( this.receivedPayloadTime <= time )
				throw new IllegalStateException("The transmission time for data was zero or negative");
		}
	}

	@Override
	public Queue<T> receive( final int time ) throws IllegalStateException {
		synchronized ( this ) {
			if ( this.payload != null && this.receivedPayloadTime <= time ) {
				this.readyQueue.add( this.payload );
				this.payload = null;
			}
			
			Queue<T> retVal = this.readyQueue;
			this.readyQueue = new LinkedList<T>( );
			return retVal;
		}
	}

	@Override
	public boolean canTransmit( final int time ) {
		synchronized ( this ) {
			return this.payload == null || this.receivedPayloadTime <= time;
		}
	}
	
	/**
	 * The queue of messages that are ready to be received.
	 * This may be updated in {@link #transmit(int, Message)} and {@link #receive(int)}. 
	 */
	private Queue<T> readyQueue;
	
	/**
	 * The message currently being transmitted. If this value is null, then no message is being transmitted.
	 * This may be updated in {@link #transmit(int, Message)} and {@link #receive(int)}.
	 */
	private T payload;
	
	/**
	 * The time when the payload is received.
	 */
	private int receivedPayloadTime;
}
