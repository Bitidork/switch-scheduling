package network;

import java.util.Arrays;
import java.util.Random;

import network.Constants;

/**
 * A network composed of 16 generators, 16 receivers, and 1 switch, where each generator a uniform amount to each receiver.
 * @author Bitidork
 *
 */
public final class UniformTestNetwork extends TestNetwork {
	public UniformTestNetwork(String name, Scheduler<Message> scheduler, Random rng) {
		super(name, scheduler, rng);
		
		final int numGenerators = 16;
		final int numReceivers = 16;
		
		GeneratorNode[] generators = new GeneratorNode[numGenerators];
		ReceiverNode[] receivers = new ReceiverNode[numReceivers];
		
		for ( int i = 0; i < numGenerators; i++ ) {
			generators[i] = this.createGenerator();
		}
		
		for ( int i = 0; i < numReceivers; i++ ) {
			receivers[i] = this.createReceiver();
		}
		
		Switch<Message> sw = new Switch<Message>( scheduler );
		
		this.addNode( sw );
		
		for ( DeferredSchedulingNode<Message> generator : generators ) {
			for ( DeferredSchedulingNode<Message> receiver : receivers ) {
				Flow<Message> flow = new Flow<Message>( Arrays.asList( generator, sw, receiver ), Constants.SAFE_CAPACITY / numGenerators );
				this.addFlow( flow );
			}
		}
	}
}
