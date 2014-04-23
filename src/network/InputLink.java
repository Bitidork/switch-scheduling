package network;

import java.util.Queue;

import network.WrapperLink;
import network.Message;

/**
 * Implements a {@link network.WrapperLink} that only permits usage of a {@link network.Link} for receiving data.
 * @author Bitidork
 * @see WrapperLink
 *
 * @param <T> The type of message the link transmits.
 */
public final class InputLink<T extends Message> extends WrapperLink<T> {
	/**
	 * Constructs an InputLink that wraps the suppled link.
	 * @param link The link to wrap.
	 * @throws NullPointerException if link is null
	 * @throws IllegalArgumentException if the source or sink node are null
	 */
	public InputLink( final Link<T> link ) {
		super( link );
	}

	@Override
	public void transmit( final int time, final T data) throws IllegalStateException {
		throw new IllegalStateException( "InputLink cannot be used to transmit" );
	}

	@Override
	public Queue<T> receive( final int time ) throws IllegalStateException {
		return this.getWrappedLink( ).receive( time );
	}

	@Override
	public boolean canTransmit( final int time ) {
		return false;
	}

	@Override
	public InputLink<T> getInputLink( ) {
		return this;
	}
	
	@Override
	public OutputLink<T> getOutputLink( ) {
		throw new IllegalStateException("Cannot get the OutputLink of an InputLink");
	}
}
