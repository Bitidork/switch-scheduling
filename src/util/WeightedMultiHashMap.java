package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Extends the {@link MultiHashMap} with the ability to weight values.
 * <p>
 * Adds functionality to permit random picking from the image of a key based on these weights.
 * Each value in an image has its own separate weight. This means that the weight of (k1,v1) is not necessarily equal to the weight of (k2,v1).
 * Values with unspecified weight are assumed to have a weight of 1.
 * Values not in the mapping have a weight of 0.
 *  
 * @author Bitidork
 *
 * @param <K> The key type.
 * @param <V>> The value type.
 */
public final class WeightedMultiHashMap<K, V> extends MultiMap<K, V> {
	/**
	 * Puts <b>v</b> into the image of <b>k</b> under this mapping, with weight <b>w</b>.
	 * @param k The key.
	 * @param v The value.
	 * @param w The weight of the value under the key.
	 */
	public void put(K k, V v, float w) {
		WeightedHashSet<V> set = (WeightedHashSet<V>)this.get(k);
		if ( set == null ) {
			map.put( k, set = new WeightedHashSet<V>( ) );
		}
		
		set.add( v, w );
	}
	
	@Override
	public void put(K k, V v) {
		Set<V> set = this.get(k);
		if ( set == null )
			map.put( k, set = new WeightedHashSet<V>( ) );
		
		set.add(v);
	}

	@Override
	public void putAll(K k, Collection<? extends V> c) {
		Set<V> set = this.get(k);
		if ( set == null )
			map.put( k, set = new WeightedHashSet<V>( ) );
		
		set.addAll(c);
	}

	@Override
	public Set<V> get(K k) {
		return map.get(k);
	}
	
	/**
	 * Gets the total weight of the given key.
	 * @param k The key.
	 * @return Returns the total weight of the given key, or 0 if <b>k</b> is not in the key set.
	 */
	public Float getWeight( K k ) {
		WeightedHashSet<V> set = (WeightedHashSet<V>)this.get( k );
		return set == null ? 0.0f : set.getWeight( );
	}
	
	/**
	 * Gets the weight of the given relation.
	 * @param k The key.
	 * @param v The value.
	 * @return Returns the total weight of the given relation, or 0 if <b>k</b> is not in the key set.
	 */
	public Float getWeight( K k, V v ) {
		WeightedHashSet<V> set = (WeightedHashSet<V>)this.get( k );
		return set == null ? 0.0f : set.getWeight( v );
	}

	@Override
	public Set<V> removeAll(K k) {
		return map.remove( k );
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}
	
	/**
	 * Picks a random element from the image of <b>k</b> biased by the weights of the elements.
	 * This method expects at least one element with non-zero weight.
	 * @param k The key.
	 * @param rand The random number generator to use.
	 * @return Returns the element picked from this set.
	 * 
	 * @throws IllegalStateException if the set is empty or has a total weight of 0
	 */
	public V pickRandom(final K k, final Random rand) {
		WeightedHashSet<V> set = (WeightedHashSet<V>) this.get(k);
		if ( set == null )
			throw new IllegalStateException("set did not exist");
		
		return set.pickRandom( rand );
	}

	/**
	 * Picks a random element from the image of <b>k</b> biased by the weights of the elements.
	 * This method expects at least one element with non-zero weight.
	 * This method uses the thread's local random number generator.
	 * @param k The key.
	 * @return Returns the element picked from this set.
	 * 
	 * @throws IllegalStateException if the set is empty or has a total weight of 0
	 * @see ThreadLocalRandom
	 */
	public V pickRandom(final K k) {
		return this.pickRandom(k, ThreadLocalRandom.current());
	}
	
	/**
	 * The underlying mapping this multimap uses.
	 */
	private HashMap<K, Set<V>> map;
	
	/**
	 * Constructs an empty, weighted, hash-based multimap.
	 */
	public WeightedMultiHashMap( ) {
		this.map = new HashMap<K, Set<V>>( );
	}
}
