package network;

import util.Tuple;
import util.WeightedMultiHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Defines the abstract functionality of a scheduler, and provides support for a decision structure.
 * <p>
 * The scheduler must provide {@link #addMessageToSchedule(int, Node, DeferredSchedulingNode, Message)}, {@link #scheduleNode(int, DeferredSchedulingNode)}, and {@link #initializeTag(DeferredSchedulingNode)}.
 * @author Bitidork
 *
 * @param <T> A type of message.
 * @see DeferredSchedulingNode
 */
public abstract class Scheduler<T extends Message> {
	/**
	 * Initializes the tag object of the supplied node.
	 * @param node The node whose tag object should be initialized.
	 */
	protected abstract void initializeTag( final DeferredSchedulingNode<T> node );
	
	/**
	 * Adds <i>message</i> arriving at <i>node</i> from <i>source</i> at <i>time</i> to be scheduled when possible by this scheduler.
	 * If <i>node</i> generated the message, <i>source</i> and <i>node</i> will be equal.
	 * @param time The time the message arrived at <i>node</i>.
	 * @param source The node the message just came from.
	 * @param node The node the message arrived at.
	 * @param message The message to be scheduled.
	 * @see #scheduleNode(int, DeferredSchedulingNode)
	 */
	public abstract void addMessageToSchedule( final int time, final Node<T> source, final DeferredSchedulingNode<T> node, final T message );
	
	/**
	 * Schedules the supplied node at the given time, and transmits over its output links in this way.
	 * @param time The time to start scheduling.
	 * @param node The node to schedule.
	 */
	public abstract void scheduleNode( final int time, final DeferredSchedulingNode<T> node );
	
	/**
	 * Initializes the decision structure of the supplied node.
	 * @param node The node whose decision structure should be initialized.
	 */
	private final void initializeDecisionStructure( final DeferredSchedulingNode<T> node ) {
		node.decisionStructure = new DecisionStructure( );
	}
	
	/**
	 * Adds the supplied node to this scheduler and initializes the tag and decision structures of that node.
	 * @param node The node to add under the domain of this scheduler.
	 */
	public final void addNode( final DeferredSchedulingNode<T> node ) {
		this.initializeTag( node );
		this.initializeDecisionStructure( node );
		this.nodes.add( node );
	}
	
	/**
	 * Constructs a Scheduler with domain over an empty collection of nodes.
	 */
	public Scheduler( ) {
		this.nodes = new HashSet<DeferredSchedulingNode<T>>( );
	}
	
	/**
	 * The set of nodes added to this scheduler.
	 */
	private Set<DeferredSchedulingNode<T>> nodes;
	
	/**
	 * Gets a copy of the collection of nodes this scheduler has domain over.
	 * @return Returns a copy of the collection of nodes this scheduler has domain over.
	 */
	public Set<DeferredSchedulingNode<T>> getNodes( ) {
		return new HashSet<DeferredSchedulingNode<T>>( this.nodes );
	}
	
	/**
	 * Gets the next node in the flow to carry the message along its destination.
	 * @param node A node.
	 * @param message A message.
	 * @return Returns the next node in the flow to carry the message along its destination.
	 */
	@SuppressWarnings("unchecked")
	public Node<T> getNextHop( final DeferredSchedulingNode<T> node, final T message ) {
		return node.decisionStructure.getDecision( (Node<T>)message.getSource( ), (Node<T>)message.getDestination( ) );
	}
	
	/**
	 * Sets the next node in the flow from <b>source</b> to <b>destination</b> at <b>node</b> to be <b>nextHop</b>.
	 * @param node The node to put the decision at.
	 * @param source The source of the flow.
	 * @param destination The destination of the flow.
	 * @param nextHop The next node messages should arrive at.
	 */
	public void putDecision( final DeferredSchedulingNode<T> node, final Node<T> source, final Node<T> destination, final Node<T> nextHop ) {
		node.decisionStructure.putDecision( source, destination, nextHop );
	}
	
	/**
	 * Gets the supplied node's decision structure.
	 * @param node The node.
	 * @return Returns the node's supplied decision structure.
	 */
	public DecisionStructure getDecisionStructure( final DeferredSchedulingNode<T> node ) {
		return node.decisionStructure;
	}
	
	/**
	 * Contains the structure necessary to determine where to send messages.
	 * @author Bitidork
	 *
	 */
	public class DecisionStructure {
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
			return this.reservedCapacities.getWeight( voq.first, voq.second ).intValue();
		}
		
		/**
		 * Picks a random input, weighted based on the reserved capacity through the output.
		 * @param output The output.
		 * @param rng The random number generator to use.
		 * @return Returns a random input, weighted based on the reserved capacity through the output, or null if no node could be picked.
		 */
		public Node<T> pickRandomInput( final Node<T> output, final Random rng ) {
			try {
				return this.reservedCapacities.pickRandom( output, rng );
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
		 * A mapping from output terminals to the set of input terminals that use that output.
		 * Each input is weighted by the capacity reserved across the terminals.
		 */
		private WeightedMultiHashMap<Node<T>, Node<T>> reservedCapacities;
		
		/**
		 * Initializes an empty decision structure.
		 */
		public DecisionStructure( ) {
			this.decisionMap = new HashMap<Tuple<Node<T>, Node<T>>, Node<T>>( );
			this.reservedCapacities = new WeightedMultiHashMap<Node<T>, Node<T>>( );
		}
	}
}
