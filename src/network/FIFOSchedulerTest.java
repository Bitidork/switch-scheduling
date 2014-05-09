package network;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FIFOSchedulerTest {

	private class Packet extends Message {
		public Packet(Node<? extends Message> source, Node<? extends Message> destination,
				int timestamp) {
			super(source, destination, timestamp);
			value = 0;
		}

		public int value; // used to check in-order delivery
	}
	
	// generates messages
	private class Host1 extends DeferredSchedulingNode<Packet> {
		private int currentValue = 0;
		Node<Packet> receiver;

		public Host1(Scheduler<Packet> scheduler, Node<Packet> dest) {
			super(scheduler);
			this.receiver = dest;
		}

		@Override
		protected void onReceiveMessage(int time, Packet message) {
			throw new IllegalStateException("shouldn't have received a message");
		}

		@Override
		protected void onUpdate(int time) {
			// generate only up to 5 messages
			if ( time != 0 )
				return;
			
			for ( int i = 0; i < 5; i++ ) {
				if ( currentValue < 5 ) {
					Packet packet = new Packet(this, receiver, time);
					packet.value = currentValue;
					this.scheduleMessage(time, this, packet);
					currentValue++;
				}
			}
		}
		
	}
	
	//receives messages
	private class Host2 extends DeferredSchedulingNode<Packet> {
		public Host2(Scheduler<Packet> scheduler) {
			super(scheduler);
			// TODO Auto-generated constructor stub
		}

		private int expectedValue = 0;

		@Override
		protected void onReceiveMessage(int time, Packet message) {
			if ( message.value != expectedValue )
				throw new IllegalStateException("Retrieved value did not match expected value");
			System.out.println( "Received " + message.value + " at time " + time );
			expectedValue++;
		}

		@Override
		protected void onUpdate(int time) {
			// empty
		}
	}
	
	private Host1 sender;
	private Host2 receiver, distraction;
	private Switch<Packet> netSwitch;
	
	@Before
	public void setUp() throws Exception {
		Scheduler<Packet> scheduler = new FIFOScheduler<Packet>( );
		receiver = new Host2(scheduler);
		sender = new Host1(scheduler, receiver);
		netSwitch = new Switch<Packet>( scheduler );
		
		distraction = new Host2(scheduler) {
			@Override protected void onReceiveMessage(int time, Packet message) {
				throw new IllegalStateException("distraction received message");
			}
		};
		
		Link<Packet> link1Switch = new QueuedLink<Packet>(sender, netSwitch);
		Link<Packet> linkSwitch2 = new QueuedLink<Packet>(netSwitch, receiver);
		Link<Packet> linkSwitchDist = new QueuedLink<Packet>(netSwitch, distraction);
		
		sender.addLink( 0, link1Switch.getOutputLink() );
		netSwitch.addLink( 0, link1Switch.getInputLink() )
			.addLink( 0, linkSwitch2.getOutputLink() )
			.addLink( 0, linkSwitchDist.getOutputLink() );
		distraction.addLink( 0, linkSwitchDist.getInputLink());
		receiver.addLink( 0, linkSwitch2.getInputLink() );
		
		
		
		scheduler.putDecision(sender, sender, receiver, netSwitch);
		scheduler.putDecision(sender, sender, distraction, netSwitch);
		scheduler.putDecision(netSwitch, sender, receiver, receiver);
		scheduler.putDecision(netSwitch, sender, distraction, distraction);
	}

	@Test
	public void test() {

		for ( int i = 0; i < 10; i++ ) {
			sender.update(i);
			netSwitch.update(i);
			distraction.update(i);
			receiver.update(i);
		}

		assertEquals( 5, receiver.expectedValue );
	}

}
