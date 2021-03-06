package network;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import util.Tuple;
import util.WeightedHashSet;
import util.WeightedMultiHashMap;

/**
 * Contains the structure necessary to determine where to send messages.
 * @author Bitidork
 *
 */
public class DecisionStructure<T extends Message> {
	/**
	 * Gets the next node that a message along the given flow should travel.
	 * @param source The node that created the message.
	 * @param destination The node that the message is destined for.
	 * @return Returns the next node that a message along the given flow should travel. This value is guaranteed to be non-null.
	 * 
	 * @throws IllegalStateException if the next node is undefined.
	 */
	public Node<T> getDecision( final Node<T> source, final Node<T> destination ) {
		Node<T> nextHop = this.decisionMap.get( new Tuple<Node<T>, Node<T>>( source, destination ) );
		if ( nextHop == null )
			throw new IllegalStateException("Next hop was undefined");
		
		return nextHop;
	}
	
	/**
	 * Adjusts this decision structure so flow between the <b>source</b> and <b>destination</b> at this node arrive at nextHop next.
	 * @param source The node that created the message.
	 * @param destination The node that the message is destined for.
	 * @param nextHop The next node that the message should arrive at.
	 * 
	 * @throws IllegalArgumentException if <b>nextHop</b> is null
	 */
	public void putDecision( final Node<T> source, final Node<T> destination, final Node<T> nextHop ) {
		if ( nextHop == null )
			throw new IllegalArgumentException("next hop was undefined");
		this.decisionMap.put( 
				new Tuple<Node<T>, Node<T>>( source, destination ), 
				nextHop );
	}
	
	/**
	 * Adjusts this decision structure so that the next node in the supplied flow is undefined.
	 * @param source The node that created the message.
	 * @param destination The node that the message is destined for.
	 * @return Returns the next node in the flow, or null if it was previously undefined.
	 */
	public Node<T> removeDecision( final Node<T> source, final Node<T> destination ) {
		return this.decisionMap.remove( 
				new Tuple<Node<T>, Node<T>>( source, destination ) );
	}
	
	/**
	 * Gets the (source,destination) pairs that map to a hop.
	 * @return Returns the (source,destination) pairs that map to a hop.
	 */
	public Set<Tuple<Node<T>, Node<T>>> getDecisionOptions( ) {
		return this.decisionMap.keySet( );
	}
	
	/**
	 * Gets the amount of capacity reserved across the supplied VOQ.
	 * @param voq The virtual output queue.
	 * @return Returns the amount of capacity reserved across the supplied VOQ.
	 */
	public int getReservedCapacity( final Tuple<Node<T>, Node<T>> voq ) {
		return this.reservedCapacities.getWeight( voq.second, voq ).intValue();
	}
	
	/**
	 * Gets the amount of capacity reserved across the supplied VOQ.
	 * @param inputTerminal The input terminal of the VOQ.
	 * @param outputTerminal The output terminal of the VOQ.
	 * @return Returns the amount of capacity reserved across the supplied VOQ.
	 */
	public int getReservedCapacity( final Node<T> inputTerminal, final Node<T> outputTerminal ) {
		return this.getReservedCapacity( new Tuple<Node<T>, Node<T>>( inputTerminal, outputTerminal ) );
	}
	
	/**
	 * Translates the reserved capacity across the VOQ by the given amount.
	 * @param voq The voq.
	 * @param amount The amount of translate by.
	 */
	protected void translateReservedCapacity( final Tuple<Node<T>, Node<T>> voq, final int amount ) {
		this.setReservedCapacity( voq, this.getReservedCapacity( voq ) + amount );
	}
	
	/**
	 * Sets the reserved capacity across the VOQ by the given amount.
	 * @param voq The voq.
	 * @param amount The amount of reserved capacity to set.
	 */
	protected void setReservedCapacity( final Tuple<Node<T>, Node<T>> voq, final int amount ) {
		if ( amount < 0 ) { // except
			throw new IllegalArgumentException("Negative capacity");
		} else if ( amount == 0 ) { // remove
			this.reservedCapacities.remove(voq.second, voq);
		} else { // replace
			this.reservedCapacities.put( voq.second, voq, amount );
		}
	}
	
	/**
	 * Picks a random input, weighted based on the reserved capacity through the output.
	 * @param output The output.
	 * @param rng The random number generator to use.
	 * @return Returns a random input, weighted based on the reserved capacity through the output, or null if no node could be picked.
	 */
	public Node<T> pickRandomInput( final Node<T> output, final Random rng ) {
		try {			
			Tuple<Node<T>, Node<T>> pickedInput = this.reservedCapacities.pickRandom( output, rng );
			if ( pickedInput == null )
				return null;
			
			return pickedInput.first;
		} catch ( IllegalStateException e ) {
			return null;
		}
	}
	
	public Node<T> pickRandomInput( final Node<T> output, final Random rng, Set<Tuple<Node<T>,Node<T>>> availableVOQs ) {
		try {
			WeightedHashSet<Tuple<Node<T>, Node<T>>> voqs = this.reservedCapacities.getWHS( output );
			if ( voqs == null )
				return null;
			
			WeightedHashSet<Tuple<Node<T>, Node<T>>> inputs = new WeightedHashSet<Tuple<Node<T>, Node<T>>>(voqs);
			inputs.retainAll( availableVOQs );
			Tuple<Node<T>, Node<T>> edge = inputs.pickRandom( rng );
			if ( edge == null )
				return null;
			return edge.first;
		} catch ( IllegalStateException e ) {
			return null;
		}
	}
	
	/**
	 * Picks a random input, weighted based on the reserved capacity through the output, using the thread's local random number generator.
	 * @param output The output.
	 * @return Returns a random input, weighted based on the reserved capacity through the output, or null if no node could be picked.
	 * @see ThreadLocalRandom
	 */
	public Node<T> pickRandomInput( final Node<T> output ) {
		return this.pickRandomInput( output, ThreadLocalRandom.current( ) );
	}
	
	/**
	 * The mapping of (source,destination) to next hop specific to this node.
	 */
	private HashMap<Tuple<Node<T>, Node<T>>, Node<T>> decisionMap;
	
	/**
	 * A mapping from output terminals to the VOQs whose output terminal is that terminal.
	 * Each input is weighted by the capacity reserved across the terminals.
	 */
	private WeightedMultiHashMap<Node<T>, Tuple<Node<T>, Node<T>>> reservedCapacities;
	
	/**
	 * Initializes an empty decision structure.
	 */
	public DecisionStructure( ) {
		this.decisionMap = new HashMap<Tuple<Node<T>, Node<T>>, Node<T>>( );
		this.reservedCapacities = new WeightedMultiHashMap<Node<T>, Tuple<Node<T>, Node<T>>>( );
	}
}
