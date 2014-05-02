package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implements a HashSet with weighted elements, and facilitates biased sampling.
 * @author Bitidork
 *
 * @param <T> The element type this set contains.
 */
public class WeightedHashSet<T> implements Set<T> {
	/**
	 * Adds the given value to the set with the supplied weight.
	 * @param e The value to add.
	 * @param weight The weight of the value. 
	 * @return true if this set did not already contain the specified element
	 * 
	 * @throws IllegalArgumentException if <b>weight</b> is negative
	 */
	public boolean add(T e, float weight) {
		if ( weight < 0 )
			throw new IllegalArgumentException("weight is negative");
		
		float delta = weight - this.getWeight( e );
		totalWeight += delta;
		weights.put( e, weight );
		return set.add( e );
	}

	@Override
	public boolean add(T e) {
		float delta = 1.0f - this.getWeight( e );
		totalWeight += delta;
		return set.add( e );
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		Set<T> newAdditions = new HashSet<T>( c );
		newAdditions.removeAll( set );
		
		float delta = (float)newAdditions.size();
		totalWeight += delta;
		return set.addAll( newAdditions );
	}

	@Override
	public void clear() {
		set.clear( );
		weights.clear();
		totalWeight = 0.0f;
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return set.iterator();
	}

	@Override
	public boolean remove(Object o) {
		totalWeight -= this.getWeight(o);
		weights.remove(o);
		return set.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Set<Object> removedObjects = new HashSet<Object>( c );
		removedObjects.retainAll( set );
		
		float delta = 0.0f;
		for ( Object o : removedObjects ) {
			delta -= this.getWeight(o);
			weights.remove( o );
		}
		
		totalWeight += delta;
		return set.removeAll(removedObjects);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Set<Object> removedObjects = new HashSet<Object>( set );
		removedObjects.removeAll( c );
		
		float delta = 0.0f;
		for ( Object o : removedObjects ) {
			delta -= this.getWeight(o);
			weights.remove( o );
		}
		
		totalWeight -= delta;
		return set.removeAll(removedObjects);
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
	public <S> S[] toArray(S[] a) {
		return set.toArray(a);
	}
	
	/**
	 * Gets the weight of the supplied object.
	 * Objects not in the set have weight 0.
	 * Each set member has, by default, a weight of 1.
	 * @param o The object.
	 * @return Returns the weight of the object.
	 */
	public Float getWeight( Object o ) {
		boolean hasObject = this.contains(o);
		Float w = this.weights.get( o );
		return hasObject ? ( w == null ? 1.0f : w ) : 0.0f;
	}
	
	/**
	 * Gets the sum of the weights of values in this set.
	 * This is done in constant time.
	 * @return Returns the sum of the weights of values in this set.
	 */
	public Float getWeight( ) {
		return totalWeight;
	}
	
	/**
	 * Picks a random element from this set biased by the weights of the elements.
	 * This method expects at least one element with non-zero weight.
	 * @param rand The random number generator to use.
	 * @return Returns the element picked from this set.
	 * 
	 * @throws IllegalStateException if the set is empty or has a total weight of 0
	 */
	public T pickRandom( final Random rand ) {
		Set<T> R = this.set;
		float weightSum = this.getWeight( );
		
		if ( R.isEmpty() || weightSum == 0.0f )
			throw new IllegalStateException("empty or otherwise unweighted set");
		
		// float num = rand.nextFloat() * weightSum; // want interval of (0, 1]
		float num = (1.0f - rand.nextFloat( )) * weightSum;
		
		//T _v = null;
		for ( T v : R ) {
			float weight = this.getWeight( v );
			if ( num <= this.getWeight( v ) )
				return v;
			
			num -= weight;
			
			//_v = v;
		}
		
		//return _v; // RNG excludes 1; SHOULD NEVER HAPPEN NOW
		throw new IllegalStateException( "total weight is not being tracked correctly" );
	}
	
	/**
	 * Picks a random element from this set biased by the weights of the elements.
	 * This method expects at least one element with non-zero weight.
	 * This method uses the thread's local random number generator.
	 * @return Returns the element picked from this set.
	 * 
	 * @throws IllegalStateException if the set is empty or has a total weight of 0
	 * @see ThreadLocalRandom
	 */
	public T pickRandom( ) {
		return this.pickRandom( ThreadLocalRandom.current() );
	}
	
	/**
	 * The underlying set.
	 */
	private HashSet<T> set;
	
	/**
	 * The mapping between a value and its weight.
	 */
	private HashMap<T, Float> weights;
	
	/**
	 * The total weight of this set.
	 */
	private float totalWeight;
	
	/**
	 * Constructs an empty weighted set.
	 */
	public WeightedHashSet( ) {
		this.set = new HashSet<T>( );
		this.weights = new HashMap<T, Float>( );
		this.totalWeight = 0.0f;
	}
	
	/**
	 * Constructs a weighted set with items in the supplied collection <b>c</b>, all with weight 1.
	 * @param c The collection to add.
	 */
	public WeightedHashSet( Collection<? extends T> c ) {
		this( );
		this.addAll( c );
	}
}
