package network;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import util.Tuple;
import util.WeightedMultiHashMap;

import java.util.concurrent.ThreadLocalRandom;

public final class ParallelScheduler<T extends Message> extends VOQScheduler<T> {

	public static class ParallelSchedulerResult<T extends Message> {
		public Set<Tuple<Node<T>, Node<T>>> program;
		
		public int iterations;
		
		public ParallelSchedulerResult( Set<Tuple<Node<T>, Node<T>>> program, int iterations ) {
			this.program = program;
			this.iterations = iterations;
		}
	}
	
	/**
	 * Static method variant for parallel iterative scheduling, which schedules <i>node</i> at the supplied <i>time</i>.
	 * The VOQs are provided in the call.
	 * @param time The time to schedule node at.
	 * @param node The node to schedule.
	 * @param availableVOQs The available VOQs.
	 * @param iterations The number of iterations to run parallel iterative matching for. If 0, will run until it is a maximal matching.
	 * @return Returns the edges to schedule.
	 * 
	 * @throws IllegalArgumentException if the number of iterations supplied was negative
	 */
	public static <T extends Message> ParallelSchedulerResult<T> createProgram( final int time, 
			Set<Tuple<Node<T>, Node<T>>> _availableVOQs, 
			final int iterations ) {
		if ( iterations < 0 )
			throw new IllegalArgumentException("Number of iterations was not non-negative");
		
		// copy of the available VOQs, for editing
		Set<Tuple<Node<T>, Node<T>>> availableVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( _availableVOQs );
		// set of scheduled edges
		Set<Tuple<Node<T>, Node<T>>> scheduledVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( );
		// the RNG to use
		Random rng = ThreadLocalRandom.current( );
		
		
		// number of iterations performed
		int i = 0;
		
		// iterate
		/*
		while ( (iterations == 0 && !availableVOQs.isEmpty()) ||
				(iterations != 0 && i < iterations) ) {
		*/
		
		// quit early while loop:
		while ( (iterations != 0 && i < iterations) || !availableVOQs.isEmpty( ) ) {
			// mapping from output to the set of grantable inputs
			WeightedMultiHashMap<Node<T>, Node<T>> requests = new WeightedMultiHashMap<Node<T>, Node<T>>( );
			
			// build requests
			for ( Tuple<Node<T>, Node<T>> e : availableVOQs ) {
				requests.put( e.second, e.first );
			}
			
			WeightedMultiHashMap<Node<T>, Node<T>> grants = new WeightedMultiHashMap<Node<T>, Node<T>>( );
			
			// Phase 1: Grant
			for ( Node<T> output : requests.keySet() ) {
				Node<T> grantedInput = requests.pickRandom( output, rng );
				
				if ( grantedInput == null ) // shouldn't ever really happen
					continue;
				
				grants.put( grantedInput, output );
			}
			
			// set of invalidated inputs
			Set<Node<T>> invalidatedInputs = new HashSet<Node<T>>( );
			Set<Node<T>> invalidatedOutputs = new HashSet<Node<T>>( );
			
			// Phase 2: Accept
			for ( Node<T> input : grants.keySet() ) {
				Node<T> acceptedOutput = grants.pickRandom( input );
				
				if ( acceptedOutput == null ) // shouldn't ever really happen
					continue;
				
				Tuple<Node<T>, Node<T>> edge = new Tuple<Node<T>, Node<T>>( input, acceptedOutput );
				scheduledVOQs.add( edge );
				invalidatedInputs.add( input );
				invalidatedOutputs.add( acceptedOutput );
			}
			
			// build set of invalidated VOQs
			Set<Tuple<Node<T>, Node<T>>> invalidatedVOQs = new HashSet<Tuple<Node<T>, Node<T>>>( );
			for ( Tuple<Node<T>,Node<T>> availableVOQ : availableVOQs ) {
				Node<T> input = availableVOQ.first,
						output = availableVOQ.second;
				if ( invalidatedInputs.contains(input) || invalidatedOutputs.contains(output) )
					invalidatedVOQs.add( availableVOQ );
			}
			// remove invalidated VOQs
			availableVOQs.removeAll( invalidatedVOQs );
			
			// increment # iterations
			i++;
		}
		
		return new ParallelSchedulerResult<T>( scheduledVOQs, i );
	}
	
	//for each of four iterations
	//build set of nodes OR voqs that can be scheduled for each unscheduled output
	//each output selects an input/voq at random
	//each input selects an output (at random)to which it has been granted, puts it on the queue of messages to be scheduled
	//remove each scheduled input and output from the pool
	@Override
	public Set<Tuple<Node<T>, Node<T>>> createProgram(int time, DeferredSchedulingNode<T> node, network.VOQScheduler<T>.Tag tag) {
		ParallelSchedulerResult<T> res = ParallelScheduler.createProgram(time, tag.getAvailableVOQs(), Constants.PARALLEL_ITERATIVE_ITERATIONS);
		return res.program;
		
		/*
		//initialize pool of tuples to draw from, output set

		Set<Tuple<Node<T>, Node<T>>> output = new HashSet<Tuple<Node<T>, Node<T>>>();
		HashMap<Node<T>, Set<Tuple<Node<T>, Node<T>>>> grants;
		HashSet<Tuple<Node<T>,Node<T>>> pool = new HashSet<Tuple<Node<T>, Node<T>>>(tag.getAvailableVOQs());
		//4 iterations total
		for(int index = 0; index < 4; index++){
			//inputs request access from each available output; kinda taken care of already
			//outputs choose randomly amongst each input to grant access; iterate through, build set of arrays
			grants = new HashMap<Node<T>, Set<Tuple<Node<T>, Node<T>>>>();
			HashMap<Node<T>, Set<Tuple<Node<T>,Node<T>>>> sortedRequests = new HashMap<Node<T>, Set<Tuple<Node<T>,Node<T>>>>();
			for(Tuple<Node<T>, Node<T>> link: pool){
				Node<T> src = link.first;
				Node<T> dest = link.second;
				Set<Tuple<Node<T>,Node<T>>> s = sortedRequests.get(dest);
				if(s == null){
					sortedRequests.put(dest,  s = new HashSet<Tuple<Node<T>,Node<T>>>());
				}
				s.add(link);
			}
			//now that we have all the available tuples sorted by their output nodes, for each set choose one to grant request
			for(Node<T> outputNode: sortedRequests.keySet()){
				int j = new Random().nextInt(sortedRequests.get(outputNode).size());
				int k = 0;
				Tuple<Node<T>, Node<T>> grant = null;
				for(Tuple<Node<T>,Node<T>> request: sortedRequests.get(outputNode)){
					if(k == j){
						grant = request;
						break;
					}
					j++;
				}
				//automatically sort grants by input
				Set<Tuple<Node<T>,Node<T>>> sortedGrants = grants.get(grant.first);
				if(sortedGrants == null)
					sortedGrants = new HashSet<Tuple<Node<T>,Node<T>>>();
				sortedGrants.add(grant);
			}
			//for each key(input node) in grants, accept one grant at random and add it to the output set
			for(Node<T> g: grants.keySet()){
				int j = new Random().nextInt(sortedRequests.get(g).size());
				int k = 0;
				Tuple<Node<T>, Node<T>> accept = null;
				for(Tuple<Node<T>,Node<T>> currentGrant: grants.get(g)){
					if(k == j){
						//accept the grant
						accept = currentGrant;
						output.add(accept);
						//remove all instances of either the input or output node from pool
						HashSet<Tuple<Node<T>,Node<T>>> newPool = (HashSet<Tuple<Node<T>, Node<T>>>) pool.clone();
						for(Tuple<Node<T>,Node<T>> link: pool){
							if(link.first == accept.first || link.second == accept.second){
								newPool.remove(link);
							}
						}
						pool = newPool;
						break;
					}
					j++;
				}
			}
			
		}
		
		return output;
		*/
	}
	
	

}
