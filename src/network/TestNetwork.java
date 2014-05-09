package network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.math.BigDecimal;
import java.math.BigInteger;

import util.WeightedHashSet;
import util.WeightedMultiHashMap;

/**
 * A network that facilitates testing of a scheduling scheme.
 * @author Bitidork
 *
 */
public class TestNetwork extends Network<Message> {
	public class GeneratorNode extends DeferredSchedulingNode<Message> {
		@Override
		protected void onReceiveMessage(int time, Message message) {
			throw new IllegalStateException("Generator node received a message");
		}

		@Override
		protected void onUpdate(int time) {
			final int timeLeft = Constants.FRAME_SIZE - (time % Constants.FRAME_SIZE);
			final Random rng = network.getRNG();
			
			// on every frame start, clear hash set
			if ( (time % Constants.FRAME_SIZE) == 0 ) {
				msgsToCreate = new WeightedHashSet<Flow<Message>>( network.flowsFromNode.getWHS( this ) );
			}
			
			// test if message should be created
			// P(e) = #msgs to generate / #time slots left in frame
			final int msgsLeft = msgsToCreate.getWeight().intValue();
			final int roll = rng.nextInt( timeLeft );
			if ( roll < msgsLeft ) {
				Flow<Message> flow = msgsToCreate.pickRandom( rng );
				if ( flow != null ) {
					// create message
					Message msg = new Message(flow.getSource(), flow.getSink(), time);
					
					// decrement msgs left
					msgsToCreate.add( flow, msgsToCreate.getWeight(flow) - 1 );
					
					// schedule message
					this.scheduleMessage(time, this, msg);
				}
			}
		}
		
		/**
		 * Constructs a generator node with the supplied scheduler and network.
		 * @param scheduler The scheduler to use.
		 * @param network The network to use.
		 */
		public GeneratorNode(Scheduler<Message> scheduler, TestNetwork network ) {
			super(scheduler);
			this.network = network;
		}
		
		private TestNetwork network;
		
		/**
		 * A set of flows weighted by the number of messages generated this frame.
		 */
		private WeightedHashSet<Flow<Message>> msgsToCreate;
	}
	
	public class ReceiverNode extends DeferredSchedulingNode<Message> {
		@Override
		protected void onReceiveMessage(int time, Message message) {
			MessageEntry me = new MessageEntry( time - message.getTimestamp(), time );
			data.add( me );
		}

		@Override
		protected void onUpdate(int time) {
			// do nothing
		}
		
		public class MessageEntry { 
			public int age;
			public int arrivalTime;
			
			public MessageEntry( final int age, final int arrivalTime ) {
				this.age = age;
				this.arrivalTime = arrivalTime;
			}
		}
		
		public ReceiverNode(Scheduler<Message> scheduler) {
			super(scheduler);
			data = new LinkedList<MessageEntry>( );
		}
		
		public LinkedList<MessageEntry> getData( ) {
			return this.data;
		}
		
		/**
		 * List of message entries.
		 */
		private LinkedList<MessageEntry> data;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addFlow( final Flow<Message> flow ) {
		// create uncreated links
		Node<Message> node = (Node<Message>)flow.getSource();
		for ( Node<? extends Message> _nextNode : flow ) {
			Node<Message> nextNode = (Node<Message>)_nextNode;
			if ( !node.isAnOutput( nextNode ) ) {
				// create the link
				QueuedLink<Message> link = new QueuedLink<Message>( node, nextNode );
				
				// register link on output
				node.addLink( 0, link.getOutputLink( ) );
				
				// register link on input
				nextNode.addLink( 0, link.getInputLink( ) );
			}
			
			node = nextNode;
		}
		
		super.addFlow(flow);
		
		this.flowsFromNode.put( (Node<Message>)flow.getSource(), flow, (float)flow.getRequiredCapacity() );
	}
	
	/**
	 * @return Returns a new generator node.
	 */
	GeneratorNode createGenerator( ) {
		GeneratorNode node = new GeneratorNode( this.getScheduler(), this );
		this.addNode( node );
		this.generators.add( node );
		return node;
	}
	
	/**
	 * @return Returns a new receiver node.
	 */
	ReceiverNode createReceiver( ) {
		ReceiverNode node =  new ReceiverNode( this.getScheduler() );
		this.addNode( node );
		this.receivers.add( node );
		return node;
	}
	
	/**
	 * Creates a testing network with the supplied scheduler and RNG.
	 * @param name The name of the network.
	 * @param scheduler The scheduler.
	 * @param rng The rng.
	 */
	public TestNetwork( String name, Scheduler<Message> scheduler, Random rng) {
		super(scheduler, rng);
		this.name = name;
		flowsFromNode = new WeightedMultiHashMap<Node<Message>, Flow<Message>>( );
		generators = new HashSet<GeneratorNode>( );
		receivers = new HashSet<ReceiverNode>( );
	}
	
	/**
	 * Gets the set of nodes generating messages in this network.
	 * @return Returns the set of nodes generating messages in this network.
	 */
	public HashSet<GeneratorNode> getGenerators( ) {
		return this.generators;
	}
	
	/**
	 * Gets the set of nodes receiving messages in this network.
	 * @return Returns the set of nodes receiving messages in this network.
	 */
	public HashSet<ReceiverNode> getReceivers( ) {
		return this.receivers;
	}
	
	/**
	 * A multimap where the image of a node is the set of flows it generates.
	 */
	private WeightedMultiHashMap<Node<Message>, Flow<Message>> flowsFromNode;
	
	/**
	 * The set of nodes generating messages in this network.
	 */
	private HashSet<GeneratorNode> generators;
	
	/**
	 * The set of nodes receiving messages in this network.
	 */
	private HashSet<ReceiverNode> receivers;
	
	private String name;

	@Override
	public void prePhase() {
		System.out.println("========================");
		System.out.println("Starting test network: " + name );
	}

	@Override
	public void postPhase() {
		System.out.println("%%%%%");
		System.out.println("Test network end; calculating stats");
		
		int numMessages = 0;
		Integer maxAge = null, minAge = null;
		BigInteger sumAges = BigInteger.valueOf(0L);
		LinkedList<Integer> arrivalDisparities = new LinkedList<Integer>( );
		HashMap<ReceiverNode, LinkedList<Integer>> receiverArrivalDisparities = new HashMap<ReceiverNode, LinkedList<Integer>>( );
		
		for ( ReceiverNode receiver : this.getReceivers( ) ) {			
			LinkedList<ReceiverNode.MessageEntry> data = receiver.getData( );
			LinkedList<Integer> currentArrivalDisparities = new LinkedList<Integer>( );
			
			if ( !data.isEmpty() ) {
				ReceiverNode.MessageEntry previousMe = data.getFirst();
				for ( ReceiverNode.MessageEntry me : receiver.getData( ) ) {
					numMessages++;
					sumAges = sumAges.add( BigInteger.valueOf( me.age ) );
					currentArrivalDisparities.add( me.arrivalTime - previousMe.arrivalTime );
					
					previousMe = me;
					
					if ( maxAge == null || ( maxAge < me.age ) )
						maxAge = me.age;
					
					if ( minAge == null || ( minAge > me.age ) )
						minAge = me.age;
				}
				currentArrivalDisparities.removeFirst();
				arrivalDisparities.addAll( currentArrivalDisparities );
			}
			
			receiverArrivalDisparities.put( receiver, currentArrivalDisparities );
		}
		
		BigInteger sumDisparities = BigInteger.valueOf( 0L );
		for ( Integer i : arrivalDisparities ) {
			sumDisparities = sumDisparities.add( BigInteger.valueOf( i ) );
		}
		
		System.out.println("Number of messages received:" + numMessages );
		System.out.print("Mean Receiver Arrival Disparities = [");
		int j = 0;
		for ( ReceiverNode receiver : receiverArrivalDisparities.keySet() ) {
			LinkedList<Integer> disparities = receiverArrivalDisparities.get( receiver );
			BigInteger sumCurrentDisparities = BigInteger.valueOf( 0L );
			
			for ( Integer i : disparities ) {
				sumCurrentDisparities = sumCurrentDisparities.add( BigInteger.valueOf( i ) );
			}
			
			j++;
			if ( disparities.size() != 0 ) {
				BigDecimal meanDisparity = BigDecimal.valueOf( sumCurrentDisparities.longValue() ).divide( BigDecimal.valueOf( disparities.size() ), BigDecimal.ROUND_HALF_DOWN );
				System.out.print( meanDisparity );
			} else {
				System.out.print( 0 );
			}
			
			if ( j == receiverArrivalDisparities.keySet().size() ) {
				System.out.println("]");
			} else {
				System.out.print(",");
			}
		}
		
		BigDecimal meanAge = numMessages != 0 ? 
				new BigDecimal( sumAges.longValue() ).divide( BigDecimal.valueOf( numMessages ), BigDecimal.ROUND_HALF_DOWN ) : 
					BigDecimal.valueOf(0);
		BigDecimal meanDisparity = arrivalDisparities.size( ) != 0 ? 
				new BigDecimal( sumDisparities.longValue() ).divide( BigDecimal.valueOf( arrivalDisparities.size( ) ), BigDecimal.ROUND_HALF_DOWN ) : 
					BigDecimal.valueOf(0);
		
		System.out.println("Mean Arrival Disparity = " + meanDisparity);
		System.out.println("Mean Age = " + meanAge );
		System.out.println("(Min Age, Max Age) = [" + minAge + "," + maxAge + "]");
		
		
		System.out.println("========================");
	}
}
