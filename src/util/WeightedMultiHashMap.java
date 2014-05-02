package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Extends the {@link MultiHashMap} with the ability to weight values.
 * <p>
 * Adds functionality to permit random picking from the image of a key based on these weights.
 * Each value in an image has its own separate weight. This means that the weight of (k1,v1) is not necessarily equal to the weight of (k2,v1).
 * Values with unspecified weight are assumed to have a weight of 1.
 *  
 * @author Bitidork
 *
 * @param <K> The key type.
 * @param <V>> The value type.
 */
public final class WeightedMultiHashMap<K, V> extends MultiHashMap<K, V> {
	@Override
	public Set<V> get( final K k ) {
		Set<V> set = super.get( k );
		return set == null ? set : new UpdatingSet( k, set, this );
	}
	
	/**
	 * Puts the given key-value pair into the multimap with weight <b>w</b>.
	 * @param k The key.
	 * @param v The value.
	 * @param w The weight of <b>v</b> through this mapping on <b>k</b>.
	 * 
	 * @throws IllegalArgumentException if <b>w</b> is less than zero. 
	 */
	public void put( final K k, final V v, final Float w ) {
		if ( w < 0.0f )
			throw new IllegalArgumentException("Weight was less than 0");
		
		float newTotalWeight = this.getWeight( k ) + - this.getWeight(k, v) + w;
		
		this.put( k, v );
		this.weights.put( new Tuple<K, V>(k, v), w );
		this.weightSum.put( k, newTotalWeight );
	}
	
	/**
	 * Returns the weight of <b>v</b> through this mapping on <b>k</b>.
	 * @param k The key.
	 * @param v The value.
	 * @return Returns the weight of <b>v</b> through this mapping on <b>k</b>, or 0 if <b>v</b> is not in the image of <b>k</b>
	 */
	public float getWeight( final K k, final V v ) {
		Float w = this.weights.get( new Tuple<K, V>( k, v ) );
		return this.hasRelation( k, v ) ? ( w == null ? 1.0f : w ) : 0.0f;
	}
	
	/**
	 * Gets the sum of the weight of the values in relation with <b>k</b>.
	 * @param k The key.
	 * @return Returns the sum of the weight of the values in relation with <b>k</b>.
	 */
	public float getWeight( final K k ) {
		Float f = this.weightSum.get( k );
		return f == null ? 0.0f : f;
	}
	
	/**
	 * Translates the weight of the set for <b>k</b> by <b>delta</b>.
	 * @param k The key.
	 * @param delta The amount of weight to shift by.
	 * 
	 * @throws IllegalArgumentException if the new weight is negative
	 */
	private void translateWeight( final K k, final float delta ) {
		Float f = this.getWeight( k );
		Float newWeight = f + delta;
		if ( newWeight < 0 )
			throw new IllegalArgumentException("New weight was less than zero.");
		
		this.weightSum.put( k, newWeight );
	}
	
	/**
	 * Picks a random value from the value of <b>k</b> under this mapping biased by their weights.
	 * @param k The key to pick a value from.
	 * @param rand The random number generator to use.
	 * @return Returns the random value picked, or null if no value could be picked. This occurs either when the image is empty or the key has no weight.
	 */
	public V pickRandom( final K k, final Random rand ) {
		Set<V> R = this.get( k );
		float weightSum = this.getWeight( k );
		
		if ( R == null || R.isEmpty() || weightSum == 0.0f )
			return null;
		
		float num = rand.nextFloat() * weightSum;
		
		V _v = null;
		for ( V v : R ) {
			float weight = this.getWeight( k, v );
			if ( num <= this.getWeight( k, v) )
				return v;
			
			num -= weight;
			
			_v = v;
		}
		
		return _v; // RNG excludes 1
		//throw new IllegalStateException("weight of key is not being tracked correctly");
	}
	
	/**
	 * Picks a random value from the value of <b>k</b> under this mapping biased by their weights.
	 * <p>
	 * Uses the thread's local random number generator.
	 * @param k The key to pick a value from.
	 * @return Returns the random value picked, or null if no value could be picked.
	 * 
	 * @see ThreadLocalRandom
	 */
	public V pickRandom( final K k ) {
		return this.pickRandom( k, ThreadLocalRandom.current() );
	}
	
	/**
	 * The mapping of weights.
	 * If (k,v) is not in its keys, the weight is assumed to be 1.0f.
	 */
	private HashMap<Tuple<K, V>, Float> weights;
	
	/**
	 * Used as an optimization tool. See {@link #pickRandom(Object, Random)}.
	 */
	private HashMap<K, Float> weightSum;

	public WeightedMultiHashMap( ) {
		this.weights = new HashMap<Tuple<K, V>, Float>( );
		this.weightSum = new HashMap<K, Float>( );
	}
	
	private class UpdatingSet implements Set<V> {
		private K k;
		private Set<V> set;
		private WeightedMultiHashMap<K, V> map;
		
		public UpdatingSet( K k, Set<V> set, WeightedMultiHashMap<K,V> map) {
			this.k = k;
			this.set = set;
			this.map = map;
		}

		@Override
		public boolean add(V e) {
			boolean retVal = set.add( e );
			if ( retVal )
				map.translateWeight( k, 1.0f );

			return retVal;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			Set<V> unique = new HashSet<V>( c );
			unique.removeAll( set );
			map.translateWeight( k, unique.size( ) );
			return set.addAll( unique );
		}

		@Override
		public void clear() {
			for ( V v : set )
				map.weights.remove( new Tuple<K,V>(k, v) );
			map.weightSum.remove( k );
			map.removeAll( k );
			set.clear();
			set = null; // null pointer on next use
			map = null;
			k = null;
		}

		@Override
		public boolean contains(Object o) {
			return set.contains( o );
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return set.containsAll( c );
		}

		@Override
		public boolean isEmpty() {
			return set == null ? true : set.isEmpty( );
		}

		@Override
		public Iterator<V> iterator() {
			return set.iterator();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			map.translateWeight( k, -map.getWeight( k, (V)o ) );
			map.weights.remove( new Tuple<K, V>( k, (V)o ) );
			
			boolean retVal = set.remove( o );
			
			// ensure null-entry consistency
			if ( set.isEmpty() ) {
				map.removeAll(k);
				map.weightSum.remove( o );
				set = null; // null pointer on next use
				map = null;
				k = null;
			}
			return retVal;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			Set<V> removedSet = new HashSet<V>( set );
			removedSet.retainAll( c );
			
			float delta = 0.0f;
			for ( V v : removedSet ) {
				delta -= map.getWeight( k, v );
				map.weights.remove( new Tuple<K, V>(k, v) );
			}
			
			map.translateWeight( k, delta );
			
			boolean retVal = set.removeAll( c );
			
			// ensure null-entry consistency
			if ( set.isEmpty() ) {
				map.removeAll( k );
				map.weightSum.remove( c );
				set = null; // null pointer on next use
				map = null;
				k = null;
			}
			return retVal;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Set<V> removedSet = new HashSet<V>( set );
			removedSet.removeAll( c );
			
			float delta = 0.0f;
			for ( V v : removedSet ) {
				delta -= map.getWeight( k, v );
				map.weights.remove( new Tuple<K, V>(k, v) );
			}
			
			map.translateWeight( k, delta );
			boolean retVal = set.removeAll( removedSet );
			
			// ensure null-entry consistency
			if ( set.isEmpty() ) {
				map.removeAll( k );
				map.weightSum.remove( c );
				set = null; // null pointer on next use
				map = null;
				k = null;
			}
			return retVal;
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public Object[] toArray() {
			return set.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return set.toArray(a);
		}
	}
}
