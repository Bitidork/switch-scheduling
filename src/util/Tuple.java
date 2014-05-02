package util;

/**
 * Implements a tuple whose first elements is of type <b>A</b> and second element of type <b>B</b>.
 * @author Bitidork
 *
 * @param <A> The first element type.
 * @param <B> The second element type.
 */
public final class Tuple<A, B> {
	/**
	 * The first element.
	 */
	public A first;
	
	/**
	 * The second element.
	 */
	public B second;
	
	@Override
	public int hashCode( ) {
		return first.hashCode( ) | Integer.rotateLeft( second.hashCode( ), 16 );
	}
	
	@Override
	public boolean equals( final Object obj ) {
		if ( !(obj instanceof Tuple) )
			return false;
		
		@SuppressWarnings("rawtypes")
		Tuple p = (Tuple)obj;
		
		return ( this.first == p.first || (this.first != null && this.first.equals( p.first ) ) ) &&
				( this.second == p.second || (this.second != null && this.second.equals( p.second ) ) );
	}
	
	/**
	 * Constructs a tuple with the supplied elements.
	 * @param first The first element.
	 * @param second The second element.
	 */
	public Tuple( final A first, final B second ) {
		this.first = first;
		this.second = second;
	}
}
