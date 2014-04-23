package network;

import java.util.Queue;

import network.Link;
import network.Message;

/**
 * Implements a {@link network.WrapperLink} that only permits usage of a {@link network.Link} for transmitting data.
 * @author Bitidork
 * @see WrapperLink
 *
 * @param <T> The type of message the link transmits.
 */
public final class OutputLink<T extends Message> extends WrapperLink<T> {
	/**
	 * Constructs an InputLink that wraps the suppled link.
	 * @param link The link to wrap.
	 * @throws NullPointerException if link is null
	 * @throws IllegalArgumentException if the source or sink node are null
	 */
	public OutputLink( final Link<T> link ) {
		super( link );
	}

	@Override
	public void transmit( final int time, final T data ) throws IllegalStateException {
		this.getWrappedLink( ).transmit( time, data );
	}

	@Override
	public Queue<T> receive( final int time ) throws IllegalStateException {
		throw new IllegalStateException("OutputLink cannot be used to receive data");
	}

	@Override
	public boolean canTransmit( final int time ) {
		return this.getWrappedLink( ).canTransmit( time );
	}
	
	@Override
	public InputLink<T> getInputLink( ) {
		throw new IllegalStateException("Cannot get the InputLink of an OutputLink");
	}
	
	@Override
	public OutputLink<T> getOutputLink( ) {
		return this;
	}
}
