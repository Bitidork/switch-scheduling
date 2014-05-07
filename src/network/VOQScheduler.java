package network;

import util.Tuple;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Set;

/**
 * Implements a scheduler that uses VOQs.
 * <p>
 * It is up to the extending scheduler to determine what nodes to schedule (see {@link #createProgram(int, DeferredSchedulingNode, Tag)}}).
 * @author Bitidork
 *
 * @param <T> A type of message.
 */
public abstract class VOQScheduler<T extends Message> extends Scheduler<T> {

	@Override
	protected final void initializeTag( final DeferredSchedulingNode<T> node ) {
		node.tag = new Tag( this );
	}

	@Override
	public final void addMessageToSchedule( final int time, final Node<T> source,
			final DeferredSchedulingNode<T> node, final T message ) {
		@SuppressWarnings("unchecked")
		Tag tag = (Tag)node.tag;
		tag.addMessage( source, node, message );
	}

	@Override
	public final void scheduleNode( final int time, final DeferredSchedulingNode<T> node ) {
		@SuppressWarnings("unchecked")
		Tag tag = (Tag)node.tag;
		
		Set<Tuple<Node<T>, Node<T>>> program = this.createProgram(time, node, tag);
		
		Set<Node<T>> usedInputs = new HashSet<Node<T>>( );
		for ( Tuple<Node<T>, Node<T>> edge : program ) {
			if ( usedInputs.contains( edge.first ) )
				throw new IllegalStateException("Scheduling error: Reused input queue");
			
			usedInputs.add( edge.first );
			T head = tag.popMessageFromQueue( edge );
			node.transmitToNode( time, edge.second, head );
		}
	}
	
	/**
	 * Generates a plan for message transmission at the supplied node and time.
	 * This method should not transmit any messages, as this is taken care of in {@link #scheduleNode(int, DeferredSchedulingNode)}.
	 * @param time The time to create the plan at.
	 * @param node The node to create the plan for.
	 * @param tag The tag of the node.
	 * @return Returns the set of edges to schedule at the supplied time and for the supplied node.
	 */
	public abstract Set<Tuple<Node<T>, Node<T>>> createProgram( 
			final int time, 
			final DeferredSchedulingNode<T> node, 
			final Tag tag );

	/**
	 * Tag structure for VOQs.
	 * @author Bitidork
	 *
	 */
	protected final class Tag {
		/**
		 * Registers the message with this Tag.
		 * @param source The source of the message (that just transmitted it).
		 * @param node The node the message is currently at.
		 * @param message The message.
		 */
		protected void addMessage( final Node<T> source,
				final DeferredSchedulingNode<T> node, final T message ) {
			Tuple<Node<T>, Node<T>> e = new Tuple<Node<T>, Node<T>>( source, scheduler.getNextHop( node, message ) );
			Queue<T> voq = queues.get( e );
			if ( voq == null )
				queues.put( e, voq = new LinkedList<T>( ) );
			
			voq.add( message );
		}
		
		/**
		 * Gets the set of VOQs with pending messages.
		 * @return Returns the set of VOQs with pending messages.
		 */
		protected Set<Tuple<Node<T>, Node<T>>> getAvailableVOQs( ) {
			return queues.keySet();
		}
		
		/**
		 * Gets and removes the message at the head of the supplied <b>voq</b>.
		 * This method will exception if no message could be retrieved.
		 * @param voq The VOQ.
		 * @return Returns the message at the head of the supplied <b>voq</b>.
		 */
		protected T popMessageFromQueue( final Tuple<Node<T>, Node<T>> voq ) {
			Queue<T> queue = queues.get( voq );
			T msg = queue.remove( );
			if ( queue.isEmpty() )
				queues.remove( voq );
			
			return msg;
		}
		
		/**
		 * Gets and removes the message at the head of the supplied VOQ.
		 * This method will exception if no message could be retrieved.
		 * @param src The source of the VOQ.
		 * @param nextHop The output of the VOQ.
		 * @return Returns the message at the head of the supplied VOQ.
		 */
		protected T popMessageFromQueue( final Node<T> src, final Node<T> nextHop ) {
			return popMessageFromQueue( new Tuple<Node<T>, Node<T>>( src, nextHop ) );
		}
		
		/**
		 * Gets the message at the head of the supplied <b>voq</b>.
		 * This method will exception if no message could be retrieved.
		 * @param voq The VOQ.
		 * @return Returns the message at the top of the supplied <b>voq</b>.
		 */
		protected T peekMessageFromQueue( final Tuple<Node<T>, Node<T>> voq ) {
			Queue<T> queue = queues.get( voq );
			T msg = queue.peek( );
			if ( msg == null )
				throw new IllegalStateException("queue was empty");
			return msg;
		}
		
		/**
		 * Gets the message at the head of the supplied VOQ.
		 * This method will exception if no message could be retrieved.
		 * @param src The source of the VOQ.
		 * @param nextHop The output of the VOQ.
		 * @return Returns the message at the head of the supplied VOQ.
		 */
		protected T peekMessageFromQueue( final Node<T> src, final Node<T> nextHop ) {
			return peekMessageFromQueue( new Tuple<Node<T>, Node<T>>( src, nextHop ) );
		}
		
		/**
		 * The VOQs themselves, mapped from tuple objects.
		 * Empty queues are removed.
		 */
		private HashMap<Tuple<Node<T>, Node<T>>, Queue<T>> queues;
		
		/**
		 * The scheduler that created this tag.
		 */
		private Scheduler<T> scheduler;
		
		/**
		 * Creates a Tag object from the supplied scheduler.
		 * @param scheduler The scheduler that created this Tag object.
		 */
		public Tag( final Scheduler<T> scheduler ) {
			this.scheduler = scheduler;
			this.queues = new HashMap<Tuple<Node<T>, Node<T>>, Queue<T>>( );
		}
	}
}
