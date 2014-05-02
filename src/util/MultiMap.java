package util;

import java.util.Collection;
import java.util.Set;

/**
 * Abstract class that specifies a mapping from a subset of K to the powerset of a subset V.
 * @author Bitidork
 *
 * @param <K> The key type.
 * @param <V>> The value type.
 */
public abstract class MultiMap<K, V> {
	/**
	 * Puts <b>v</b> in the value of <b>k</b> under this multimap.
	 * @param k The key.
	 * @param v The value.
	 */
	public abstract void put( final K k, final V v );
	
	/**
	 * Puts all elements of the set <b>v</b> into the value of <b>k</b> under this multimap.
	 * <p>
	 * If <b>c</b> is empty, this function must return immediately.
	 * @param k The key.
	 * @param c The set of values.
	 */
	public abstract void putAll( final K k, final Collection<? extends V> c );
	
	/**
	 * Gets the value of <b>k</b> under this multimap, or null if the key is not in its domain.
	 * <p>
	 * This set is directly modifiable.
	 * Behavior when {@link #removeAll(Object)} or the set is used and becomes empty is undefined.
	 * @param k The key.
	 * @return Returns the value of <b>k</b> under this multimap, or null if the key is not in its domain.
	 */
	public abstract Set<V> get( final K k );
	
	/**
	 * Removes <b>k</b> from the domain of this multimap.
	 * <p>
	 * After a call to this, get(<b>k</b>) must return null.
	 * @param k The key.
	 * @return Returns the value of <b>k</b> under this multimap, or null if <b>k</b> was not in the domain of this multimap.
	 */
	public abstract Set<V> removeAll( final K k );
	
	/**
	 * Gets the domain of this multimap.
	 * <p>
	 * This set must not be modified by a caller or parent caller of this function.
	 * @return Returns the domain of this multimap.
	 */
	public abstract Set<K> keySet( );
	
	/**
	 * Appends all the relations of <b>m</b> into this multimap.
	 * @param m The multimap to insert into this multimap.
	 */
	public void putAll( final MultiMap<K, V> m ) {
		for ( K k : this.keySet( ) ) {
			this.putAll( k, m.get( k ) );
		}
	}
	
	/**
	 * Checks if this multimap has the <b>k</b> in its domain.
	 * @param k The key.
	 * @return Returns true if and only if this multimap has <b>k</b> in its domain.
	 */
	public boolean hasKey( final K k ) {
		return this.keySet( ).contains( k );
	}
	
	/**
	 * Checks if <b>v</b> is a value under this mapping over <b>k</b>.
	 * @param k The key.
	 * @param v The value.
	 * @return Returns true if and only if <b>v</b> is a value under this mapping over <b>k</b>.
	 */
	public boolean hasRelation( final K k, final V v ) {
		Set<V> R = this.get( k );
		return R != null && R.contains( v );
	}
	
	/**
	 * Removes <b>v</b> from the value of <b>k</b> under this multimap.
	 * @param k The key.
	 * @param v The value.
	 * @return Returns v if it was in the value of <b>k</b>, else returns null.
	 */
	public V remove( final K k, final V v ) {
		Set<V> S = this.get( k );
		V retVal = S != null && S.remove( v ) ? v : null;
		if ( retVal == v && S.isEmpty( ) )
			this.removeAll( k );
		
		return retVal;
	}
}
