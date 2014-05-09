import java.util.concurrent.ThreadLocalRandom;

import network.FIFOScheduler;
import network.Message;
import network.ParallelScheduler;
import network.PrivilegedGeneratorNetwork;
import network.Scheduler;
import network.StatisticalScheduler;
import network.TestNetwork;
import network.UniformTestNetwork;

/**
 * Simulator class.
 * @author cs162-gk
 *
 */
public final class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final int NUM_FRAMES_TO_RUN = 250;
		
		for ( int i = 0; i < 3; i++ ) {
			Scheduler<Message> scheduler = null;
			String schedulerName = null;
			
			switch ( i ) {
			case 0: schedulerName = "fifoScheduler"; scheduler = new FIFOScheduler<Message>( ); break;
			case 1: schedulerName = "parallelScheduler"; scheduler = new ParallelScheduler<Message>( ); break;
			case 2: schedulerName = "statisticalScheduler"; scheduler = new StatisticalScheduler<Message>( ); break;
			}
			
			for ( int j = 1; j < 2; j++ ) {
				TestNetwork network = null;
				if ( j == 0 ) {
					network = new UniformTestNetwork( "uniform network with " + schedulerName, scheduler, ThreadLocalRandom.current() );
				} else {
					network = new PrivilegedGeneratorNetwork( "privileged network with " + schedulerName, scheduler, ThreadLocalRandom.current() );
				}
				
				network.run( NUM_FRAMES_TO_RUN );
			}
		}
	}

}
