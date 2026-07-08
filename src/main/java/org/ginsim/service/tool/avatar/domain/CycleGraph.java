package org.ginsim.service.tool.avatar.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ginsim.service.tool.avatar.utils.AvaMath;

/**
 * Class with functionalities to explore a cycle of states
 * 
 * @author Rui
 * @version 1.0
 */
public class CycleGraph {

    // Map each vertex to adjacent vertices.
    private Map<String,Collection<String>> cycle;
    private List<String> outL;
    
	private boolean avoidCycles = false;
	private int maxDepth, minOut; 
	private  Set<Integer> outChecked;
	//Map<String,Integer> inChecked;

    /**
     * Creates a cycle
     * @param outList the cycle exit states
     */
    public CycleGraph(List<String> outList){
    	cycle = new HashMap<String,Collection<String>>();
    	outL = outList;   
    	minOut = outL.size();
    }
        
	/**
	 * Adds a set of successor states from a state within the cycle
	 * @param key the identifier of the state in the cycle
	 * @param succs the set of successor states accessible from the given state
	 */
	public void add(String key, AbstractStateSet succs) {
		cycle.put(key, succs.getKeys());		
	}
    
    /**
     * Computes the exit probabilities associated with the given state
     * @param state of the state to compute the exit probabilities
     * @param approxDepth the maximum depth of the search (if no exit is found up to this depth, exploration continues without revisiting paths)
     * @return the exit probabilities associated with the given state
     */
    public double[] getExitProbs(String state, int approxDepth){
    	if(approxDepth>0) maxDepth = approxDepth;
    	double[] probs = new double[outL.size()];
    	
    	if(avoidCycles) getExitProbsStopCycles(probs,1.0,state,new HashSet<String>(),0);
    	else {
        	maxDepth = 5;//Math.max(10, (int)Math.sqrt(cycle.size())); 
        	outChecked = new HashSet<Integer>();
        	//inChecked = new HashMap<String,Integer>(); 
        	//System.out.println("==S:"+state);
    		getExitProbsStopDepth(probs,1.0,state,0);
    		if(outChecked.size()==0)
    			getExitProbsStopCycles(probs,1.0,state,new HashSet<String>(),0);
        		//System.out.println("Depth:"+maxDepth+"\nProbs:"+AvatarUtils.toString(probs));
    		//System.out.println("==>>>\n"+AvatarUtils.toString(probs));
    	}
    	double sum = AvaMath.sum(probs);
    	for(int i=0, l=probs.length; i<l; i++) probs[i]/=sum;
    	return probs;
    }
    
    private void getExitProbsStopCycles(double[] probs, double jointProb, String state, Set<String> seen, int depth){
    	Collection<String> succs = cycle.get(state);
		jointProb /= (double)succs.size();
		List<String> newones = new ArrayList<String>();
		for(String succ : succs){
			if(seen.contains(succ)) continue;
			if(!cycle.containsKey(succ))
				probs[outL.indexOf(succ)]+=jointProb;
			else newones.add(succ);
		}
		seen.add(state);
		seen.addAll(newones);
		if(depth>=maxDepth) return;
		for(String succ : newones)
			getExitProbsStopCycles(probs,jointProb,succ,seen,depth+1);
	}

    private void getExitProbsStopDepth(double[] probs, double jointProb, String state, int depth){
		//if(outChecked.size()>=minOut) return;
		/*if(!inChecked.containsKey(state)) inChecked.put(state,0);
		else inChecked.put(state, inChecked.get(state)+1); 
		if(inChecked.get(state)>=4) return;*/
		if(depth>=maxDepth) return;

    	Collection<String> succs = cycle.get(state);
		jointProb /= (double)succs.size();
		for(String succ : succs){
			if(!cycle.containsKey(succ)){
				int index = outL.indexOf(succ);
				outChecked.add(index);
				probs[index]+=jointProb;
			}
			else getExitProbsStopDepth(probs,jointProb,succ,depth+1);
		}
	}
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
    	return "Digraph:\n\tgraph="+cycle+"\n\tindexes="+outL;
    }
}
