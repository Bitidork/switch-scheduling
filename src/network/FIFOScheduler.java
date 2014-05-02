package network;

import util.Tuple;
import util.WeightedMultiHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Implements a FIFO scheduler.
 * @author Bitidork
 *
 * @param <T> A type of message.
 */
public final class FIFOScheduler<T extends Message> extends Scheduler<T> {
	@Override
	protected void initializeTag( final DeferredSchedulingNode<T> node ) {
		node.tag = new Tag( );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addMessageToSchedule( final int time, final Node<T> source,
			final DeferredSchedulingNode<T> node, T message) {
		((Tag)node.tag).addMessage(source, node, message);		
	}

	@Override
	public void scheduleNode(int time, DeferredSchedulingNode<T> node) {
		@SuppressWarnings("unchecked")
		Tag tag = (Tag)node.tag;
		WeightedMultiHashMap<Node<T>, Node<T>> requestingInputs = new WeightedMultiHashMap<Node<T>, Node<T>>( );
		
		Set<Node<T>> inputNodes = tag.getNodesWithAvailableInput( );
		Set<Node<T>> outputNodes = node.getIdleOutputPorts( );
		
		// fill in the multi hash map
		for ( Node<T> src : inputNodes ) {
			T head = tag.peekMessageFromQueue( src );
			Node<T> nextHop = this.getNextHop( node, head );
			if ( outputNodes.contains( nextHop ) )
				requestingInputs.put( nextHop, src );			
		}
		
		// grant random requests
		Set<Tuple<Node<T>, Node<T>>> grantedRequests = new HashSet<Tuple<Node<T>, Node<T>>>( );
		for ( Node<T> output : requestingInputs.keySet() ) {
			Node<T> grantedInput = requestingInputs.pickRandom( output );
			grantedRequests.add( new Tuple<Node<T>, Node<T>>( grantedInput, output ));
		}
		
		// transmit
		for ( Tuple<Node<T>, Node<T>> edge : grantedRequests ) {
			T head = tag.popMessageFromQueue( edge.first );
			node.transmitToNode( time, edge.second, head );
		}
	}
	
	/**
	 * Tag for the FIFOScheduler. Maintains a Queue of messages for each input, and removes empty queues.
	 * @author Bitidork
	 *
	 */
	protected class Tag {	
		/**
		 * Registers the message with this Tag.
		 * @param source The source of the message (that just transmitted it).
		 * @param node The node the message is currently at.
		 * @param message The message.
		 */
		protected void addMessage( final Node<T> source, final DeferredSchedulingNode<T> node, final T message ) {
			Queue<T> queue = this.inputQueues.get( source );
			if ( queue == null )
				this.inputQueues.put( source, queue = new LinkedList<T>( ));
			
			queue.add( message );
		}
		
		/**
		 * Gets the set of sources of messages still at this node.
		 * @return Returns the set of sources of messages still at this node.
		 */
		protected Set<Node<T>> getNodesWithAvailableInput( ) {
			return inputQueues.keySet();
		}
		
		/**
		 * Gets and removes (from the queue itself) the next message that came from the supplied node.
		 * This method will exception if no message could be retrieved.
		 * @param inputQueue The input queue.
		 * @return Returns the next message that came from the supplied node.
		 */
		protected T popMessageFromQueue( final Node<T> inputQueue ) {
			Queue<T> queue = inputQueues.get( inputQueue );
			T msg = queue.remove( );
			if ( queue.isEmpty() )
				inputQueues.remove( inputQueue );
			
			return msg;
		}
		
		/**
		 * Gets the next message that came from the supplied node.
		 * This method will exception if no message could be retrieved.
		 * @param inputQueue The input queue.
		 * @return Returns the next message that came from the supplied node.
		 */
		protected T peekMessageFromQueue( final Node<T> inputQueue ) {
			Queue<T> queue = inputQueues.get( inputQueue );
			T msg = queue.peek( );
			if ( msg == null )
				throw new IllegalStateException("queue was empty");
			return msg;
		}
		
		/**
		 * A mapping between sources and input queues themselves.
		 * Any empty queue is removed.
		 */
		private HashMap<Node<T>, Queue<T>> inputQueues;
	
		/**
		 * Constructs a new Tag object.
		 */
		public Tag( ) {
			this.inputQueues = new HashMap<Node<T>, Queue<T>>( );
		}
	}
}
