package network;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import util.Tuple;
import java.util.HashMap;

public final class StatisticalScheduler<T extends Message> extends VOQScheduler<T> {

	@Override
	public Set<Tuple<Node<T>, Node<T>>> createProgram(int time, DeferredSchedulingNode<T> node, network.VOQScheduler<T>.Tag tag) {

		//initialize pool of tuples to draw from, output set
		HashSet<Tuple<Node<T>, Node<T>>> pool = new HashSet<Tuple<Node<T>, Node<T>>>(tag.getAvailableVOQs());
		HashMap<Tuple<Node<T>, Node<T>>, Double> flows = new HashMap<Tuple<Node<T>, Node<T>>, Double>();
		
		// assign initial flow to each VOQ randomly from 0 to 1
		for (Tuple<Node<T>, Node<T>> link : pool) {
			Random rng = new Random();
			int temp = rng.nextDouble();
			Double flow = new Double(temp);
			flows.put(link, flow);
		}
		
		// inputs request access from each available output
		// requests maps OUT nodes to a SET of (IN,OUT) tuples.
		// the set contains all (IN,OUT) tuples for which IN has requested permission from OUT.
		HashMap<Node<T>, Set<Tuple<Node<T>,Node<T>>>> requests = new HashMap<Node<T>, Set<Tuple<Node<T>,Node<T>>>>();
		for(Tuple<Node<T>, Node<T>> link: pool){
			Node<T> src = link.first;
			Node<T> dest = link.second;
			Set<Tuple<Node<T>,Node<T>>> s = requests.get(dest);
			if(s == null){
				requests.put(dest,  s = new HashSet<Tuple<Node<T>,Node<T>>>());
			}
			s.add(link);
		}
		
		// for each set of requests that an OUT node has received,
		// it first goes through the set and marks each request as a potential grant with P = flow/B.
		// of the potential grants, the OUT node randomly picks one to grant permission.
		// grants maps an IN node to a SET of (IN,OUT) tuples for which OUT granted IN permission.
		grants = new HashMap<Node<T>, Set<Tuple<Node<T>, Node<T>>>>();
		for(Node<T> outputNode : requests.keySet()){
			Random rng = new Random();
			ArrayList<Tuple<Node<T>,Node<T>>> potentialGrants = new ArrayList<Tuple<Node<T>,Node<T>>>();
			for(Tuple<Node<T>,Node<T>> request : requests.get(outputNode)){
				double flow = flows.get(request).doubleValue();
				double random = rng.nextDouble();
				if (flow > random) {
					potentialGrants.add(request);
				}
			}
			
			Tuple<Node<T>, Node<T>> grant = null;
			int numPotentialGrants = potentialGrants.size();
			int randomIndex = rng.nextInt(numPotentialGrants);
			grant = potentialGrants.get(randomIndex);
			
			Set<Tuple<Node<T>,Node<T>>> sortedGrants = grants.get(grant.first);
			if(sortedGrants == null)
				sortedGrants = new HashSet<Tuple<Node<T>,Node<T>>>();
			sortedGrants.add(grant);
		}
		
		// for each IN node that received a set of grants,
		// each OUT node is assigned a random number M between 0 and the flow of (IN,OUT).
		// the IN node then potentially accepts a grant from OUT with P = M/(sum of Ms).
		// of all the potentially acceptable grants, the IN node picks one at random.
		Set<Tuple<Node<T>, Node<T>>> output = new HashSet<Tuple<Node<T>, Node<T>>>();
		for(Node<T> inputNode : grants.keySet()){
			HashMap<Tuple<Node<T>, Node<T>>, Double> acceptProbs = new HashMap<Tuple<Node<T>, Node<T>>();
			double totalAcceptProb = 0.0;
			Random rng = new Random();
			for (Tuple<Node<T>, Node<T>> grant : grants.get(inputNode)) {
				double acceptProb = rng.nextDouble()*flows.get(grant);
				totalAcceptProb += acceptProb;
				acceptProbs.put(grant, new Double(acceptProb));
			}
			
			ArrayList<Tuple<Node<T>, Node<T>>> potentialAccepts = new ArrayList<Tuple<Node<T>, Node<T>>>();
			for (Tuple<Node<T>, Node<T>> grant : grants.get(inputNode)) {
				double temp = rng.nextDouble();
				double acceptProb = acceptProbs.get(grant);
				if (acceptProb/totalAcceptProb > temp) {
					potentialAccepts.add(grant);
				}
			}
			
			Tuple<Node<T>, Node<T>> accept = null;
			int numPotentialAccepts = potentialAccepts.size();
			int randomIndex = rng.nextInt(numPotentialAccepts);
			accept = potentialAccepts.get(randomIndex);
			output.add(accept);
			
			//remove all instances of either the input or output node from pool
			HashSet<Tuple<Node<T>,Node<T>>> newPool = (HashSet<Tuple<Node<T>, Node<T>>>) pool.clone();
			for (Tuple<Node<T>,Node<T>> link: pool) {
				if (link.first == accept.first || link.second == accept.second){
					newPool.remove(link);
				}
			}
			pool = newPool;
		}
		
		return output;
	}
}
