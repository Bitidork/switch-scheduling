package network;

import java.util.Arrays;
import java.util.Random;

public final class PrivilegedGeneratorNetwork extends TestNetwork {

	public PrivilegedGeneratorNetwork(String name, Scheduler<Message> scheduler, Random rng) {
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
		
		final int p = 2 * Constants.SAFE_CAPACITY / ( generators.length * ( generators.length + 1) );
		Switch<Message> sw = new Switch<Message>( scheduler );
		
		this.addNode( sw );
		
		for ( int i = 1; i <= numGenerators; i++ ) {
			GeneratorNode generator = generators[i - 1];
			
			for ( ReceiverNode receiver : receivers ) {
				Flow<Message> flow = new Flow<Message>( Arrays.asList( generator, sw, receiver ), i * p );
				this.addFlow( flow );
			}
		}
	}

}
