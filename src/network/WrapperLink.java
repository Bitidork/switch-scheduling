package network;

import network.Link;
import network.Message;

/**
 * A {@link network.Link} that is used to wrap around another {@link network.Link}.
 * <p> 
 * This link implements {@link network.Link#getSource()} and  {@link network.Link#getSink()} by calling on the wrapped link.
 * @author Bitidork
 * @see network.Link
 *
 * @param <T> The type of message the link transmits.
 */
public abstract class WrapperLink<T extends Message> extends Link<T> {
	/**
	 * Creates a link that wraps around the supplied link.
	 * @param link The link to wrap around.
	 * @throws NullPointerException if link is null
	 * @throws IllegalArgumentException if the source or sink node are null
	 */
	public WrapperLink( final Link<T> link ) {
		super( link.getSource( ), link.getSink( ) );
	
		this.wrappedLink = link;
	}
	
	@Override
	public Node<T> getSource( ) {
		return this.getWrappedLink( ).getSource( );
	}
	
	@Override
	public Node<T> getSink( ) {
		return this.getWrappedLink( ).getSink( );
	}
	
	@Override
	public int getTransmissionTime( final T message ) {
		return this.getWrappedLink( ).getTransmissionTime( message );
	}
	
	/**
	 * Gets the link that this link wraps around.
	 * @return The link that this link wraps around.
	 */
	protected Link<T> getWrappedLink( ) {
		return this.wrappedLink;
	}
	
	/**
	 * The link that this link wraps around.
	 */
	private Link<T> wrappedLink;
}
