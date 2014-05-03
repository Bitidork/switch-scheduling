package network;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import util.Tuple;
import java.util.HashMap;

public final class ParallelScheduler<T extends Message> extends VOQScheduler<T> {

	
	//for each of four iterations
	//build set of nodes OR voqs that can be scheduled for each unscheduled output
	//each output selects an input/voq at random
	//each input selects an output (at random)to which it has been granted, puts it on the queue of messages to be scheduled
	//remove each scheduled input and output from the pool
	@Override
	public Set<Tuple<Node<T>, Node<T>>> createProgram(int time, DeferredSchedulingNode<T> node, network.VOQScheduler<T>.Tag tag) {

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
	}
	
	

}
