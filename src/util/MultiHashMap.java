package util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a {@link MultiMap} using a HashMap and HashSets.
 * @author Bitidork
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class MultiHashMap<K, V> extends MultiMap<K, V> {
	@Override
	public void put( final K k, final V v ) {
		Set<V> S = this.get( k );
		if ( S == null ) {
			// this.map.put( k, S = new HashSet<V>( ) ); // UNUSED; CAUSED ISSUES WITH WEIGHTEDMULTIHASHMAP
			this.map.put( k, new HashSet<V>( ) );
			S = this.get( k );
		}
		
		S.add( v );
	}

	@Override
	public void putAll( final K k, final Collection<? extends V> c ) {
		if ( c.isEmpty() )
			return;
		
		Set<V> S = this.get( k );
		if ( S == null ) {
			// this.map.put( k, S = new HashSet<V>( v ) ); // UNUSED; CAUSED ISSUES WITH WEIGHTEDMULTIHASHMAP
			this.map.put( k, new HashSet<V>( c ) );
			S = this.get( k );
		}
		
		S.addAll( c );
	}

	@Override
	public Set<V> get( final K k ) {
		return this.map.get( k );
	}

	@Override
	public Set<V> removeAll( final K k ) {
		return this.map.remove( k );
	}

	@Override
	public Set<K> keySet( ) {
		return Collections.unmodifiableSet( this.map.keySet( ) );
	}
	
	@Override
	public String toString( ) {
		return map.toString();
	}

	/**
	 * The underlying mapping this multimap uses.
	 */
	private HashMap<K, Set<V>> map;
	
	public MultiHashMap( ) {
		this.map = new HashMap<K, Set<V>>( );
	}
}
