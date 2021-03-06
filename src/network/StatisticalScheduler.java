package network;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import util.Tuple;
import util.WeightedMultiHashMap;

import java.util.concurrent.ThreadLocalRandom;

public final class StatisticalScheduler<T extends Message> extends VOQScheduler<T> {

	@Override
	public Set<Tuple<Node<T>, Node<T>>> createProgram(int time, DeferredSchedulingNode<T> node, network.VOQScheduler<T>.Tag tag) {
		// the set of idle output ports
		Set<Node<T>> availableOutputs = new HashSet<Node<T>>( node.getIdleOutputPorts( ) );
		// the rng to use
		Random rng = ThreadLocalRandom.current();
		// available VOQs
		Set<Tuple<Node<T>, Node<T>>> availableVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( tag.getAvailableVOQs( ) );
		
		Set<Tuple<Node<T>, Node<T>>> scheduledVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( );
		
		DecisionStructure<T> ds = this.getDecisionStructure(node);
		
		for ( int k = 0; k < Constants.STATISTICAL_ITERATIONS; k++ ) {
			// mapping from an input nodes to the set of output nodes that granted the request, weighted by a random int in [0,X_ij], where X_ij is the reserved capacity through the VOQ 
			WeightedMultiHashMap<Node<T>, Node<T>> grants = new WeightedMultiHashMap<Node<T>, Node<T>>( );
			
			// Step 1, 2.a in the paper
			for ( Node<T> outputNode : availableOutputs ) {
				// pick random input weighted by reserved capacity
				Node<T> grantedInput = ds.pickRandomInput(outputNode, rng, availableVOQs);
					
				
				// no flows through outputNode
				if ( grantedInput == null )
					continue;
				
				int Xij = ds.getReservedCapacity(grantedInput, outputNode);
				float mij = (float)rng.nextInt(Xij + 1);
				grants.put(grantedInput, outputNode, mij);
			}
			
			{		
				// generate sets of invalidated input and output terminals
				Set<Node<T>> invalidatedInputs = new HashSet<Node<T>>( );
				Set<Node<T>> invalidatedOutputs = new HashSet<Node<T>>( );
				
				// Step 2.a, 2.b in the paper
				for ( Node<T> inputNode : grants.keySet() ) {
					// accepted output
					Node<T> acceptedOutput = grants.pickRandom(inputNode, rng);
					
					Tuple<Node<T>, Node<T>> edge = new Tuple<Node<T>,Node<T>>( inputNode, acceptedOutput );
					
					// cases where edge cannot be scheduled
					if ( acceptedOutput == null ) // all weights 0 or empty set 
						continue;
	
					
					if ( tag.getVOQLength( edge ) == 0 ) // no messages in voq
						continue;
					
					scheduledVOQs.add( edge );
					invalidatedInputs.add( edge.first );
					invalidatedOutputs.add( edge.second );
				}
				
				// remove invalidated VOQs
				Set<Tuple<Node<T>, Node<T>>> invalidatedVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( );
				for ( Tuple<Node<T>,Node<T>> availableVOQ : tag.getAvailableVOQs() ) {
					Node<T> input = availableVOQ.first,
							output = availableVOQ.second;
					if ( invalidatedInputs.contains(input) || invalidatedOutputs.contains(output) )
						invalidatedVOQs.add( availableVOQ );
				}
				
				//availableVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( availableVOQs );
				availableVOQs.removeAll( invalidatedVOQs );
				availableOutputs.removeAll( invalidatedOutputs );
			}
		}
		
		if ( Constants.STATISICAL_ADDITIONAL_PARALLEL_ITERATIONS > 0) {
			ParallelScheduler.ParallelSchedulerResult<T> res = ParallelScheduler.createProgram(time, availableVOQs, Constants.STATISICAL_ADDITIONAL_PARALLEL_ITERATIONS );
			scheduledVOQs.addAll(res.program);
		}
		
		return scheduledVOQs;
	}
}
