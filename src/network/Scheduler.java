package network;

import java.util.HashSet;
import java.util.Set;

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
		node.decisionStructure = new DecisionStructure<T>( );
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
	@SuppressWarnings("unchecked")
	public void putDecision( final DeferredSchedulingNode<? extends T> node, final Node<? extends T> source, final Node<? extends T> destination, final Node<? extends T> nextHop ) {
		((DeferredSchedulingNode<T>)node).decisionStructure.putDecision( (Node<T>)source, (Node<T>)destination, (Node<T>)nextHop );
	}
	
	/**
	 * Removes the decision in place, or does nothing if it is not in place.
	 * @param node The node the decision is at.
	 * @param source The source of the flow.
	 * @param destination The destination of the flow.
	 */
	@SuppressWarnings("unchecked")
	public void removeDecision( final DeferredSchedulingNode<? extends T> node, final Node<? extends T> source, final Node<? extends T> destination ) {
		((DeferredSchedulingNode<T>)node).decisionStructure.removeDecision( (Node<T>)source, (Node<T>)destination );
	}
	
	/**
	 * Gets the supplied node's decision structure.
	 * @param node The node.
	 * @return Returns the node's supplied decision structure.
	 */
	public DecisionStructure<T> getDecisionStructure( final DeferredSchedulingNode<T> node ) {
		return node.decisionStructure;
	}
}
