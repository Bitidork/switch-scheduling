package network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import network.InputLink;
import network.OutputLink;
import network.Message;

/**
 * An abstract class that provides the basic functionality of a network node.
 * <p>
 * Hides away concurrency details and other ordering issues. Users of this class must provide:
 * <ul>
 * <li>Update on a global clock: {@link #onUpdate(int)}
 * <li>Receive messages destined for this node: {@link #onReceiveMessage(int, Message)}
 * <li>Route messages through this node: {@link #scheduleMessage(int, Node, Message)}, {@link #sendOutboundMessages(int)}
 * </ul>
 * @author Bitidork
 *
 * @param <T> The type of message this type of node receives.
 */
public abstract class Node<T extends Message> {
	/**
	 * A class that represents a message currently in transmission. 
	 * @author Bitidork
	 *
	 */
	private class TransmissionEntry implements Comparable<TransmissionEntry> {
		private int time;
		private Node<T> source;
		private Node<T> sink;
		private T message;
		
		@Override
		public int compareTo( final TransmissionEntry e ) {
			if ( this.getTime( ) < e.getTime( ) )
				return -1;
			return this.getTime( ) == e.getTime( ) ? 0 : 1;
		}
		
		/**
		 * Gets the time the message will finish transmitting.
		 * @return Returns the time the message will finish transmitting.
		 */
		public int getTime( ) {
			return this.time;
		}
		
		/**
		 * Gets the node transmitting the message.
		 * @return Returns the node transmitting the message.
		 */
		public Node<T> getSource( ) {
			return this.source;
		}
		
		/**
		 * Gets the node this message is being transmitted to.
		 * @return Returns the node this message is being transmitted to.
		 */
		public Node<T> getSink( ) {
			return this.sink;
		}
		
		/**
		 * Gets the message.
		 * @return Returns the message.
		 */
		public T getMessage( ) {
			return this.message;
		}
		
		/**
		 * Constructs a transmission entry for <i>message</i> that will finish transmitting at <i>time</i> from <i>source</i>.
		 * @param time The time the message will finish transmitting.
		 * @param source The node that is transmitting the message.
		 * @param message The message being transmitted.
		 * 
		 * @throws IllegalArgumentException if source or message are null
		 */
		public TransmissionEntry( final int time, final Node<T> source, final Node<T> sink, final T message ) {
			if ( source == null )
				throw new IllegalArgumentException("source node cannot be null");
			
			if ( sink == null )
				throw new IllegalArgumentException("sink node cannot be null");
			
			if ( message == null )
				throw new IllegalArgumentException("message cannot be null");
			
			this.time = time;
			this.source = source;
			this.sink = sink;
			this.message = message;
		}
	}
	
	
	/**
	 * Called when this node receives a message destined for it.
	 * @param time The time at which the node received the message.
	 * @param message The message that is destined for this node.
	 * @see #update(int)
	 * @see #processMessage(int, Node, Message)
	 */
	protected abstract void onReceiveMessage( final int time, final T message );
	
	/**
	 * Schedules <i>message</i> that hopped from <i>source</i> to this node to be an outbound message.
	 * It should be possible for this message to be sent out from this node on a call to {@link #sendOutboundMessages(int)}.
	 * @param time The time to schedule the message at.
	 * @param source The node that the message most recently came from.
	 * @param message The message to schedule.
	 * @see #sendOutboundMessages(int)
	 * @see #update(int)
	 * @see #processMessage(int, Node, Message)
	 */
	protected abstract void scheduleMessage( final int time, final Node<T> source, final T message );
	
	/**
	 * Called whenever {@link #update(int)} is called.
	 * This method should be used to generate messages created by this node.
	 * @param time The time the node is updated.
	 * @see #update(int)
	 */
	protected abstract void onUpdate( final int time );
	
	/**
	 * Sends a subset of messages scheduled by {@link #scheduleMessage(int, Node, Message)} out from this node.
	 * <p>
	 * To transmit a message to a node, use {@link #transmitToNode(int, Node, Message)}.
	 * @param time The time at which to send messages out from this node.
	 * @see #scheduleMessage(int, Node, Message)
	 * @see #transmitToNode(int, Node, Message)
	 * @see #update(int)
	 */
	protected abstract void sendOutboundMessages( final int time );
	
	/**
	 * Constructs a <i>Node</i> with a unique identifier.
	 */
	public Node( ) {
		if ( Node.nodeCount == Integer.MAX_VALUE )
			throw new RuntimeException("Node.nodeCount overflow");
		
		this.id = Node.nodeCount;
		Node.nodeCount++;
		
		this.inputLinks = new HashMap<Node<T>, InputLink<T>>( );
		this.inputArrivals = new PriorityQueue<TransmissionEntry>( );
		
		this.outputLinks = new HashMap<Node<T>, OutputLink<T>>( );
		this.idleOutputPorts = new HashSet<Node<T>>( );
		this.outputArrivals = new PriorityQueue<TransmissionEntry>( );
	}
	
	/**
	 * Updates this node at the supplied time.
	 * <p>
	 * The general structure of this method is as follows:
	 * <ol>
	 * <li>Process incoming messages ({@link #processMessage(int, Node, Message)})
	 * <ol>
	 * <li>Process messages destined for this node ({@link #onReceiveMessage(int, Message)})
	 * <li>Schedule messages going through this node ({@link #scheduleMessage(int, Node, Message)})
	 * </ol>
	 * <li>User-defined update event ({@link #onUpdate(int)})
	 * <li>Send some scheduled messages ({@link #sendOutboundMessages(int)})
	 * </ol> 
	 * @param time The time the update is being performed.
	 */
	public void update( final int time ) {
		// update idle output ports
		{
			TransmissionEntry e = null;
			while ( (e = this.outputArrivals.peek()) != null && e.getTime( ) <= time ) {
				this.idleOutputPorts.add( e.getSink( ) );
				this.outputArrivals.poll( );
			}
		}
		
		// find incoming arrivals
		synchronized ( this.inputArrivals ) {
			TransmissionEntry e = null;
			while ( (e = this.inputArrivals.peek( )) != null && e.getTime( ) <= time ) {
				this.processMessage( time, e.getSource( ), e.getMessage( ) );
				this.inputArrivals.poll( );
			}
		}
		
		// user-defined update event
		this.onUpdate( time );
		
		// transmit messages from this node
		this.sendOutboundMessages( time );
	}
	
	/**
	 * Processes a message that is going over the link between <i>source</i> and this node.
	 * <p>
	 * Messages destined for this node will go through {@link #onReceiveMessage(int, Message)}.
	 * Messages destined for another node will go through {@link #scheduleMessage(int, Node, Message)}.
	 * @param time The time the message was received.
	 * @param message The message to process.
	 */
	public void processMessage( final int time, final Node<T> source, final T message ) {
		if ( this == message.getDestination( ) ) {
			this.onReceiveMessage( time, message );
		} else {
			this.scheduleMessage( time, source, message );
		}
	}
	
	/**
	 * Registers the supplied link as an input link to this node.
	 * The link must be used exclusively by methods provided by the Node class.
	 * @param time The time at which the link is registered.
	 * @param link The link to register with this node.
	 * @throws IllegalArgumentException if this node is not the sink of the link
	 * @throws IllegalArgumentException if the link is busy (cannot transmit)
	 */
	public void addLink( final int time, final InputLink<T> link ) {
		if ( this != link.getSink( ) )
			throw new IllegalArgumentException("The node was not the sink of the supplied input link");
		
		if ( !link.canTransmit( time ) )
			throw new IllegalArgumentException("The link was busy when registering");
		
		this.inputLinks.put( link.getSource( ), link );
	}
	
	/**
	 * Registers the supplied link as an output link from this node.
	 * The link must be used exclusively by methods provided by the Node class.
	 * @param time The time at which the link is registered.
	 * @param link The link to register with this node.
	 * @throws IllegalArgumentException if this node is not the source of the link
	 * @throws IllegalArgumentException if the link is busy (cannot transmit)
	 */
	public void addLink( final int time, final OutputLink<T> link ) {
		if ( this != link.getSource( ) )
			throw new IllegalArgumentException("The node was not the source of the supplied input link");
		
		if ( !link.canTransmit( time ) )
			throw new IllegalArgumentException("The link was busy when registering");
		
		this.outputLinks.put( link.getSink( ), link );
		this.idleOutputPorts.add( link.getSink( ) );
	}
	
	/**
	 * Gets the unique identifier for this node.
	 * @return Returns the unique identifier for this node.
	 */
	public int getId( ) {
		return this.id;
	}
	
	@Override
	public int hashCode( ) {
		return this.getId( );
	}
	
	/**
	 * Gets a set of idle output ports from this node.
	 * @return Returns a set of idle output ports from this node.
	 */
	public Set<Node<T>> getIdleOutputPorts( ) {
		return new HashSet<Node<T>>(this.idleOutputPorts);
	}
	
	/**
	 * Transmits <i>message</i> to the supplied sink starting at <i>time</i>.
	 * Takes care of internal book-keeping.
	 * @param time The time to start transmitting the message.
	 * @param sink The node to transmit to.
	 * @param message The message to transmit.
	 */
	public void transmitToNode( final int time, final Node<T> sink, final T message ) {
		// get the link
		Link<T> link = this.outputLinks.get( sink );
		
		if ( link == null ) {
			throw new IllegalArgumentException("Suppled sink was not on an output port");
		} else if ( link.canTransmit(time) && this.idleOutputPorts.remove( sink ) ) {
			// calculate arrival time
			int arrivalTime = link.getTransmissionTime( message ) + time;
			// start transmitting
			link.transmit(time, message);
			
			// add arrival entry to the sink
			synchronized ( sink.inputArrivals ) {
				sink.inputArrivals.add( 
						new TransmissionEntry( 
								arrivalTime, 
								this, sink, 
								message ) );
			}
		} else {
			throw new IllegalStateException("Cannot transmit to supplied sink; link was busy");
		}
	}
	

	/**
	 * A non-negative unique number (across all Nodes) associated with this Node.
	 */
	private int id;
	
	/**
	 * A mapping whose range is restricted to the links whose sink is this node, that maps source nodes to the link that connects it to this node.
	 */
	private HashMap<Node<T>, InputLink<T>> inputLinks;
	
	/**
	 * A queue of messages to this node whose entries are sorted in ascending order by arrival time.
	 */
	private PriorityQueue<TransmissionEntry> inputArrivals;
	
	/**
	 * A mapping whose range is restricted to the links whose source is this node, that maps a sink node to the link that connects this node to it.
	 */
	private HashMap<Node<T>, OutputLink<T>> outputLinks;
	
	/**
	 * A set of nodes whose link from this node is idle.
	 */
	private HashSet<Node<T>> idleOutputPorts;
	
	/**
	 * A queue of messages from this node whose entries are sorted in ascending order by arrival time.
	 */
	private PriorityQueue<TransmissionEntry> outputArrivals;
	
	/**
	 * The number of nodes created so far.
	 */
	private static int nodeCount = 0;
}