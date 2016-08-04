package org.ginsim.service.tool.avatar.simulation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.logicalmodel.StatefulLogicalModel;
import org.colomoto.logicalmodel.io.avatar.AvatarUtils;
import org.colomoto.logicalmodel.tool.simulation.avatar.FirefrontUpdater;
import org.ejml.simple.SimpleMatrix;
import org.ginsim.service.tool.avatar.domain.AbstractStateSet;
import org.ginsim.service.tool.avatar.domain.ApproximateFinalPaths;
import org.ginsim.service.tool.avatar.domain.CompactStateSet;
import org.ginsim.service.tool.avatar.domain.CycleGraph;
import org.ginsim.service.tool.avatar.domain.ExhaustiveFinalPaths;
import org.ginsim.service.tool.avatar.domain.FinalPaths;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.domain.State;
import org.ginsim.service.tool.avatar.domain.StateSet;
import org.ginsim.service.tool.avatar.utils.AvaException;
import org.ginsim.service.tool.avatar.utils.AvaMath;
import org.ginsim.service.tool.avatar.utils.ChartGNUPlot;

import com.panayotis.gnuplot.JavaPlot;

/**
 * Avatar simulation for the discovery of point and complex attractors.<br>
 * Class providing all the functionalities to explore STGs, extend and rewire cycles.
 * 
 * @author Rui Henriques
 * @author Pedro Monteiro, Nuno Mendes
 * @version 1.0
 */
public class AvatarSimulation extends Simulation {

	/** enum specifying the different behavioral strategies of Avatar */
	public enum AvatarStrategy { MatrixInversion, Approximate, RandomExit }
	
	/****************/
	/** PARAMETERS **/
	/****************/
	
	/** cycle expansion rate */
    public int tauInit;
	/** number of iterations */
    public int runs;
	/** maximum depth */
    public int maxSteps;
	/** maximum number of visited transitions to stop expansion 
	    approximated by #states within the growing cycle times #exits */
    public int maxPSize;
	/** maximum number of states within a cycle to be rewired */
    public int maxRewiringSize;
	/** minimum cycle size for rewiring */
    public int minCSize;
	/** size of small state spaces for the inflationary mode */
    public int smallStateSpace;
	/** minimum size of a transient cycles to be stored */
    public int minTransientSize;
	/** whether transient cycles should be kept between iterations */
	public boolean keepTransients;
	/** whether oracles should be kept between iterations */
	public boolean keepOracle;
	/** whether charts should be created and plotted */
	public boolean plots = false;
	/** Avatar rewiring strategy */
    public AvatarStrategy strategy;
	/** maximum depth for the approximate rewiring strategy */
    public int approxDepth = -1;
	
	protected FirefrontUpdater exhaustiveUpdater;
	//public AvatarUpdater sequentialUpdater;

	/**
	 * Instantiates an Avatar simulation based on a logical model
	 * @param _model a stateful logical model possibly defining a set of initial states and oracles
	 */
	public AvatarSimulation(StatefulLogicalModel _model) {
		super(_model);
		exhaustiveUpdater = new FirefrontUpdater(model);
		//sequentialUpdater = new AvatarUpdater(model);
	}

	/***************/
	/** MAIN CODE **/
	/***************/
	
	public Result runSimulation() throws Exception {
		
		output("Strategy:"+strategy.toString());
		
		/** I: Initializations **/
		Result result = new Result();
	    int performed=0, truncated=0, steps=0;
	    int stateSpaceSize=1, space=Math.max(runs/100, 1), psize=0;
	    for(NodeInfo comp : model.getNodeOrder()) stateSpaceSize *= comp.getMax()+1;
	    List<AbstractStateSet> savedTransients = new ArrayList<AbstractStateSet>();
	    Map<String,List<Double>> plotProbs = new HashMap<String,List<Double>>();
		if(!quiet) output("Quiet="+quiet+"\nNode order: "+model.getNodeOrder()+"\nPSize="+psize+",maxPSize="+maxPSize);
	    
	    /** II: Simulation **/
		
	    for(int sn=1, i=0, t=0, tau=tauInit; sn<=runs; sn++, i=0, t=0, steps=0, psize=0, tau=tauInit) {
	    	
			State istate = SimulationUtils.getRandomState(model, model.getInitialStates(),false);
			Map<String,Integer> discoveryTime = new HashMap<String,Integer>();
			if(!quiet) output("\n\n====== Iteration "+sn+"/"+runs+" istate="+istate+" =====");
			if(isGUI) publish("Iteration "+sn+"/"+runs+" state="+istate);
	    	
	    	/** A: Initialize Simulation **/
			
			FinalPaths exitProbs = strategy.equals(AvatarStrategy.RandomExit) ? new ApproximateFinalPaths() : new ExhaustiveFinalPaths();
	        StateSet D = new StateSet(), F = new StateSet(istate), exitStates = new StateSet(); 
			List<StateSet> temporaryTransients = new ArrayList<StateSet>(); //C = new ArrayList<StateSet>(): C cycles in incarnation t - no need! just Call and Ct
		    //boolean inflation = stateSpaceSize<=smallStateSpace; //constitutive inflationary mode for small state spaces

	        /** B: Do Reincarnations **/ 
		    
	        while(!F.isEmpty()){
	        	State s = F.getProbableRandomState();
	        	if(s==null) continue;
	            if(!quiet) output("Popped state="+s.toString()+" Sim="+sn+", Reincarnation="+t+", #F="+F.size()+", #D="+D.size()+", #A="+result.attractorsCount.keySet().size());

		        /** C: Check whether state belongs a terminal or transient cycle **/ 

	            if(keepTransients){
		            for(AbstractStateSet trans : savedTransients){
		            	if(trans.contains(s)){
		            		if(strategy.equals(AvatarStrategy.RandomExit)) 
		            			s = ((StateSet)trans).getExitStateSet().getProbableRandomState();
		            		else s = ((StateSet)trans).getProbableExitState(s); 
		            		if(!quiet) output("Identified transient and getting out of it through state = "+s);
		            		break; 
		            	}
		            }
	            }
	            if(keepOracle){
		            boolean complex = false;
		            for(AbstractStateSet trans : result.complexAttractors.values()){
		            	if(trans.contains(s)){
		            		result.incrementComplexAttractor(trans.getKey(),steps);
		            		complex = true;
		            		if(!quiet) output("Incrementing attractor!");
		            		break; 
		            	}
		            }
		            if(!complex){
			            for(CompactStateSet trans : oracle){
			            	if(trans.contains(s)){
			            		result.add(trans,steps);
			            		complex = true;
			            		if(!quiet) output("Incrementing attractor!");
			            		break; 
			            	}
			            }
		            }
		            if(complex) break;
	            }
	        	if(s==null) continue;
	            
	            /** D: Cycle to Rewire **/

	            if(D.contains(s)){
	            	StateSet Ct = new StateSet();
	            	int time = discoveryTime.get(s.key);
	            	for(State ds : D.getStates()) 
	            		if(discoveryTime.get(ds.key)>=time) Ct.add(ds); // && dsTime<=i
	            	
	            	/** D1: Extend Cycle: check whether larger can be identified **/
	            	
	                int prev_cycle_size = 0;
	                double exitRatio = 0;
            		tau = (tauInit>0) ? tauInit : 2;
            		StateSet cycleToRewire = Ct, exitStatesRewiring = new StateSet();
	                do {
                        prev_cycle_size = Ct.size();
                        if(!quiet) output("\tTau updated from "+tau+" to "+(tau*2)+" (prev cycle=#"+prev_cycle_size+")");
	                    if(prev_cycle_size>0 && !quiet) output("\tTrying another round of cycle extension..");

                        StateSet res = new StateSet();
                        extendCycle(null/*istate*/, Ct, res, i, tau, new HashMap<String,Integer>(), i);
                    	tau *= 2;
                        Ct = res;
                        	
                        exitStates = new StateSet(); //for exits
	                    for(State v : Ct.getStates()){
	                        for(State successor : generateSuccessors(v,exitProbs.getPaths(v.key),exitStates,Ct).getStates())
	                        	if(!Ct.contains(successor)) exitStates.add(successor);
	                    }
                        if(!quiet) output("Cycle extended from #"+prev_cycle_size+" to #"+Ct.size()+"states (#"+exitStates.size()+" exits)");
                        publish("Cycle extended from #"+prev_cycle_size+" to #"+Ct.size()+"states (#"+exitStates.size()+" exits)");
                		F = new StateSet(exitStates);
	                    exitRatio = ((double)exitStates.size())/(double)Ct.size();
	                    
	                    if(Ct.size()<maxRewiringSize){
	                    	cycleToRewire = Ct;
	                    	exitStatesRewiring = exitStates;
	                    }
	                    
	                } while(exitRatio>0 && prev_cycle_size<Ct.size() && Ct.size()<maxPSize);
	                
                    steps += Math.ceil((double)tau/2.0);
	                
	                if(!quiet) output("\tDone extensions: cycle with exitRatio="+exitRatio);
	                if(isGUI) publish("Extended cycle with #"+Ct.size()+" states and exitRatio="+exitRatio);
	                D = Ct;
	                
	                /** D2: Rewire Graph **/
	                
	            	//C.add(Ct);
	                if(exitStates.isEmpty()){
	                    if(!quiet) output("\tIdentified an attractor!");
	                    result.add(new StateSet(Ct)); //calculateComplexAttractor(C,t);
	                } else { 
	                	//printGraph(Ct,exitStates,exitProbs);
	                	Ct = cycleToRewire;
	                	exitStates = exitStatesRewiring;
                        if(!quiet) output("Rewiring cycle");
                        if(isGUI) publish("Rewiring");
                        rewriteGraph(Ct,exitStates,exitProbs);
                        if(!quiet) output("Rewired cycle with #"+Ct.size()+" states");
                        if(isGUI) publish("Rewired cycle with #"+Ct.size()+" states");
                        
	                    //StateSet transi = calculateComplexAttractor(C,t);
			            if(Ct.size()>minTransientSize){
			            	StateSet transi = new StateSet(Ct);
	                    	transi.setExitStates(exitStates);
			            	temporaryTransients.add(transi);
			            }
    	            	F = generateSuccessors(s,exitProbs.getPaths(s.key),exitStates,Ct);
    	            	if(!quiet) output("Successors of "+s.toString()+" => "+F.toString());
	                    //if(F.isEmpty()) throw new AvaException("F is empty after re-writing a cycle with successors: Unknown error!");
	                }
	                D = new StateSet(); 
	                t++;
	                i=0;
	            	discoveryTime.put(s.key, i);
	                
	            } else { //D does not contain s: new state never seen before

		            /** E: Non-Cycle: Keep On **/
	            	
		            D.add(s);  
	            	discoveryTime.put(s.key, i++);
	            	F = generateSuccessors(s,exitProbs.getPaths(s.key),exitStates,new StateSet());
	                if(F.isEmpty()){ 
	                    result.add(s,steps);
		            	for(StateSet transi : temporaryTransients){
	                    	if(!quiet) output("\tSaving transient (#"+transi.size()+")");
		                    if(!strategy.equals(AvatarStrategy.RandomExit)) transi.setProbPaths(exitProbs);
		                	savedTransients.add(transi);
		            	}
	                    /*if(t>0){
	                    	StateSet transi = calculateComplexAttractor(C,t-1);
	                    	if(!quiet) output("\tIdentified transient:"+transi);
			                if(transi.size()>minTransientSize){
			                	if(!quiet) output("\tSaving transient (#"+transi.size()+")");
		                    	transi.setExitStates(exitStates);
		                    	if(!strategy.equals(AvatarStrategy.RandomExit)) transi.setProbPaths(exitProbs);
			                	savedTransients.add(transi);
			                }
	                    }*/
		            }
	            }
	            
                /** F: Finish Iteration **/

	            steps++;
		        if(maxSteps>0 && steps>=maxSteps){
		        	output("\tReached maximum depth: quitting current simulation");
		        	truncated++;
		            break; //last;
		        }
	        }
	        
	        /** G: Out of Reincarnation **/
	        
	        if(plots && (sn+1)%space==0){
	    		Set<String> allkeys = new HashSet<String>();
	    		allkeys.addAll(result.complexAttractors.keySet());
	    		allkeys.addAll(result.pointAttractors.keySet());
	        	for(String key : allkeys){
	        		if(!plotProbs.containsKey(key)) plotProbs.put(key,new ArrayList<Double>());
	        		plotProbs.get(key).add((double)result.attractorsCount.get(key));
	        	}
	        }
		    if(!quiet) output("\tOut of iteration!");
		    performed++;
	    }
	    
	    /** H: plots **/

	    if(plots && plotProbs.size()>0){
	    	int max=0, i=0;
		    if(!quiet) output("Plotting charts");
		    if(isGUI) publish("Plotting charts");
		    for(String key : plotProbs.keySet()) max=Math.max(max,plotProbs.get(key).size());
		    double[][] dataset = new double[plotProbs.size()][max];
		    for(List<Double> vec : plotProbs.values()){
		    	for(int k=0, j=max-vec.size(), l=vec.size(); j<l; j++, k++) dataset[i][j]=vec.get(k);
		    	i++;
		    }
		    String title = "Convergence of probability estimates";
	    	JavaPlot chart = ChartGNUPlot.getConvergence(AvaMath.normalizeColumns(dataset), space, title, "#Iterations", "Attractors");
	    	BufferedImage img = ChartGNUPlot.getImage(chart);
	    	result.addPlot(title,img);
	    	if(!isGUI){
		    	outputDir = (outputDir.startsWith("/")) ? new File("").getAbsolutePath()+outputDir+"/" : outputDir+"\\";
		    	String filename = outputDir+model.getName()+"_Convergence.png";
	    		ChartGNUPlot.writePNGFile(img, new File(filename));
	    	}
	    	List<String> depthRemovals = new ArrayList<String>();
	    	for(String key : result.attractorsDepths.keySet())
	    		if(result.attractorsDepths.get(key).size()==0) depthRemovals.add(key);
	    	for(String key : depthRemovals) result.attractorsDepths.remove(key);
	    	
	    	if(result.attractorsDepths.size()>0){
		    	//System.out.println("::"+result.attractorsDepths);
		    	title = "Depth of attractors";
		    	System.out.println(result.attractorsDepths);
		    	chart = ChartGNUPlot.getErrorBars(result.attractorsDepths, "Depth of attractors", "Attractors", "Trajectory length");
		    	BufferedImage img2 = ChartGNUPlot.getImage(chart);
		    	result.addPlot(title,img2);
		    	if(!isGUI){
			    	String filename = outputDir+"0"+model.getName()+"_depths.png";
		    		ChartGNUPlot.writePNGFile(img2, new File(filename));
		    	}
	    	}
	    }
	    	    
	    /** I: update results **/
	    
	    if(isGUI) publish("Creating compact patterns of the found attractors");
		for(String key : result.complexAttractors.keySet()){
			if(result.complexAttractors.get(key) instanceof StateSet)
				result.complexAttractorPatterns.put(key,SimulationUtils.getStatePatterns(model.getNodeOrder(),(StateSet)result.complexAttractors.get(key)));
		}
	    result.strategy = "Avatar";
	    result.transientMinSize = minTransientSize; 
	    result.transients = savedTransients;
	    if(!quiet) output("Simulations asked: "+runs+", performed: "+performed+", truncated: "+truncated);
	    result.log = "AVATAR\nModel: "+model.getName()+"\n"+saveOutput();
		return result;
	}


	/***********************/
	/** AUXILIARY METHODS **/
	/***********************/


	/**
	 * Method for generating the successor states of a given state (comprising knowledge of rewirings)
	 * @param s the state to be expanded
	 * @param exitProbs knowledge of exit transitions
	 * @param exitStates knowledge of exit states
	 * @param intraNull knowledge of cycle that the state is possibly in
	 * @return the correct successor states
	 */
	private StateSet generateSuccessors(State s, Map<String,Double> exitProbs, StateSet exitStates, StateSet intraNull){
        List<byte[]> successors = exhaustiveUpdater.getSuccessors(s.state);
        //for(byte[] succ : successors) if(succ[succ.length-2]==0) System.out.println(">>"+s+"|"+new State(succ));
        
        double prob = s.probability*(1.0/(double)successors.size());
        if(exitProbs==null) return new StateSet(successors,prob);
        else if(!quiet) output("\tExits of "+s+" => "+exitProbs);
        
		StateSet succSet = new StateSet();
        for(byte[] succ : successors){
        	State u = new State(succ,prob);
        	if(intraNull.contains(u)) continue;
        	else if(exitProbs.containsKey(u.key))
        		u.probability=exitProbs.get(u.key);
        	succSet.add(u);
        }
        for(String sKey : exitProbs.keySet()){
        	if(!succSet.contains(sKey) && exitStates.contains(sKey)){
        		State u = exitStates.getState(sKey);
        		u.probability = exitProbs.get(sKey);
        		succSet.add(u);
        	}
        }
		return succSet;
	}

	/**
	 * Calculates a complex attractor based on the knowledge of previous incarnations
	 * @param C terminal cycles from all incarnations
	 * @param t the current time
	 * @return the revised complex attractor
	 */
	public StateSet calculateComplexAttractor(List<StateSet> C, int t) {
	    StateSet Cstar = new StateSet();
	    StateSet L = C.get(t); 
	    if(!quiet) output("\tVisiting all reincarnations to discover the master attractor ...");
	    while(!L.isEmpty()){
	    	State s = L.getFirstState();
	    	L.remove(s);
	        Cstar.add(s);
	        for(int k=0; k<t; k++){
	            if(C.get(t).contains(s)){
	                for(State v : C.get(k).getStates()){
	                    if(Cstar.contains(v)) continue;
	                    if(!quiet) output("\tAdding state "+v.toString()+" from reincarnation "+k);
	                    L.add(v);
	                }
	            }
	        }
	    }
	    return Cstar;
	}

	
	/**
	 * Method for rewiring a cycle
	 * @param cycle the cycle (state-set) to be rewired
	 * @param out the exit states
	 * @param pi the transitions between cycle and exit states whose probability is to be adjusted
	 * @throws Exception
	 */
	public void rewriteGraph(StateSet cycle, StateSet out, FinalPaths pi) throws Exception {

        if(cycle.size()<=minCSize){ //dO not small cycles
        	if(!quiet) output("\tRefusing to rewrite cycle with less than "+minCSize+" elements");
        	return; 
        } else if(!quiet) output("\tCycle has "+out.size()+" exits");

		List<String> cycleL = new ArrayList<String>(cycle.getKeys()); 
		List<String> outL = new ArrayList<String>(out.getKeys());
        
        if(!strategy.equals(AvatarStrategy.RandomExit)){
			
			/** I: MATRIX INVERSION **/
			
			if(strategy.equals(AvatarStrategy.MatrixInversion)){
				
				final double[][] qMatrix, rMatrix;
				/** A: Computing q and r **/
				try {
					qMatrix = new double[cycle.size()][cycle.size()];
					rMatrix = new double[cycle.size()][out.size()];
		    	} catch (OutOfMemoryError e) {
		    		throw new Exception(e);
		    	}
		
				for(State s : cycle.getStates()){
					StateSet set = generateSuccessors(s,pi.getPaths(s.key),out,cycle);
					double p = 1.0/(double)set.size();
					int index = cycleL.indexOf(s.key);
			        for(State v : set.getStates()){
			        	if(cycle.contains(v.key)) qMatrix[index][cycleL.indexOf(v.key)]=-p; //pi(s,v)
			        	else if(out.contains(v.key)) rMatrix[index][outL.indexOf(v.key)]=p; //pi(s,v)
			        	else {
			        		if(!quiet) output(v+" is not in cycle and not in cycle successors");
			                throw new AvaException("Unclear situation of state "+v);
			        	}
			        }
				}
				if(!quiet) output("QMatrix\n"+AvatarUtils.toString(qMatrix));
				if(!quiet) output("RMatrix\n"+AvatarUtils.toString(rMatrix));
				//System.out.println(">>Q\n"+AvatarUtils.toString(qMatrix));
				//System.out.println(">>R\n"+AvatarUtils.toString(rMatrix));
			
				/** B: Computing (I-q)^-1 * r **/
				
				for(int i=0, l=cycle.size(); i<l; i++) qMatrix[i][i]+=1;
		    	
				final double[][] rewrittenMatrix = new double[qMatrix.length][rMatrix[0].length];
	    		boolean ejml = true;
	    		if(ejml){
	    	    	SimpleMatrix RMatrix = new SimpleMatrix(rMatrix);
	    	    	try{
	    		    	RMatrix = new SimpleMatrix(qMatrix).invert().mult(RMatrix);
	    	    	} catch (OutOfMemoryError e) {
	    	    		throw new Exception(e);
	    	    	}
	    	        for(int i=0, l1=RMatrix.numRows(); i<l1; i++)
	    	            for(int j=0, l2=RMatrix.numCols(); j<l2; j++) rewrittenMatrix[i][j]=RMatrix.get(i,j);
	    	        if(!quiet) output("Final Matrix EJML:\n"+AvatarUtils.toString(rewrittenMatrix));
	    		} else { 
	    	    	RealMatrix RM = MatrixUtils.createRealMatrix(rMatrix), QM = MatrixUtils.createRealMatrix(qMatrix);
	    	    	double[][] resMatrix = new LUDecomposition(QM).getSolver().getInverse().multiply(RM).getData();
	    	    	for(int i=0, l1=rewrittenMatrix.length; i<l1; i++)
		    	    	for(int j=0, l2=rewrittenMatrix[0].length; j<l2; j++) rewrittenMatrix[i][j]=resMatrix[i][j];
	    	    	if(!quiet) output("Final Matrix Commons:\n"+AvatarUtils.toString(rewrittenMatrix));
	    		}
	
				/*CycleGraph digraph = new CycleGraph(outL); 
				for(State s : cycle.getStates())
					digraph.add(s.key,generateSuccessors(s,pi.getPaths(s.key),out,cycle));
				double[][] probs = new double[cycle.size()][];
				int i=0;
				for(String s : cycleL)
					probs[i++] = digraph.getExitProbs(s);				
				System.out.println("cycle:"+cycleL);
				System.out.println("out:"+outL);
				System.out.println("graph:"+digraph.toString());
				System.out.println("===\n"+AvatarUtils.toString(probs));
				System.out.println(">>>\n"+AvatarUtils.toString(rewrittenMatrix));*/
				
				/** C: Adjusting Probabilities **/
				pi.addOutputPaths(cycleL, outL, rewrittenMatrix);
		    	//psize+=cycleL.size()*outL.size();

			} else { 
				
				/** II: QUASI-OPTIMAL SOLUTION **/
				
				CycleGraph digraph = new CycleGraph(outL); 
				for(State s : cycle.getStates())
					digraph.add(s.key,generateSuccessors(s,pi.getPaths(s.key),out,cycle));
		    	double[][] probs = new double[cycle.size()][];
				int i=0;
				for(String s : cycleL) probs[i++] = digraph.getExitProbs(s,approxDepth);

				/*System.out.println("cycle:"+cycleL);
				System.out.println("out:"+outL);
				System.out.println("graph:"+digraph.toString());
				System.out.println("===\n"+AvatarUtils.toString(probs));*/
				pi.addOutputPaths(cycleL, outL, probs);
		    	//psize+=cycleL.size()*outL.size();
			}
        } else {
        	
			/** III: APPROXIMATE SOLUTION **/

	    	double prob = 1.0/out.size();
	    	//psize+=cycle.size()*out.size();
	    	pi.addOutputPaths(cycleL, outL, prob);
        } 
        if(!quiet) output("\tCycle pivot has "+out.size()+" exists");
	}

	/**
	 * Method for extending cycles before rewiring
	 * @param v the state being expanded (null at the start)
	 * @param cycle the state-set representing the initial cycle
	 * @param result the extended cycle
	 * @param i the current time
	 * @param tau the expansion rate
	 * @param time structure maintaining the time/depth of the included states 
	 * @param originalTime the original time
	 */
	public void extendCycle(State v, StateSet cycle, StateSet result, int i, int tau, Map<String,Integer> time, int originalTime){
	    if(!quiet) output("\t\tExtending tau="+tau+" cycle="+cycle.getKeys());
	    StateSet Q = new StateSet();
	    if(v==null){
            //if(!quiet) output("cycle="+cycle);
	        for(State u : cycle.getStates()){
	            time.put(u.key,i);
	            result.add(u);
	            for(State s : new StateSet(exhaustiveUpdater.getSuccessors(u.state)).getStates())
	            	if(!cycle.contains(s)) Q.add(s);
	        }
	    } else {
	    	if(!quiet) output("("+i+")V="+v.key);
            time.put(v.key,i);
            result.add(v);
            Q.addAll(new StateSet(exhaustiveUpdater.getSuccessors(v.state)));
	    }
	    i++;
		StateSet additions = new StateSet();
	    if(tau>0){
	    	if(!quiet) output("Tau>0 and Q="+Q.getKeys());
	        for(State w : Q.getStates()){
	            if(!time.containsKey(w.key)){
	            	extendCycle(w, cycle, result, i, tau-1, time, originalTime);
	                if(v!=null) time.put(v.key,Math.min(time.get(v.key),time.get(w.key)));
	            } else if(v!=null) time.put(v.key,Math.min(time.get(v.key),time.get(w.key))); 
	            	//if(result.contains(w) && v!=null){
	            //output("\tw:"+w.key+" d:"+d.get(w.key)+" l:"+lambda.get(w.key));
	            //if(v!=null) output("\t\tv:"+v.key+" d:"+d.get(v.key)+" l:"+lambda.get(v.key));
	        }
	    }
	    if(v!=null && time.get(v.key)>originalTime){
	    	if(!quiet) output("V:"+v.key+"("+time.get(v.key)+")=>remove at i="+i);
	    	result.remove(v);
	    } else result.addAll(additions);
	    	//if(v!=null) output("V:"+v.key+"("+lambda.get(v.key)+"|"+d.get(v.key)+")=>add at i="+i);
	}
	
	@Override
	public void dynamicUpdateValues() {
		List<NodeInfo> components =model.getNodeOrder();
		int allStates = 1;
		for(NodeInfo comp : components) allStates *= comp.getMax()+1; 

		//non-fixed
		runs=Math.max(10000, allStates);
		maxSteps=allStates;
		approxDepth=components.size();
		maxPSize=allStates;

		int sumStates = 0;
		for(NodeInfo comp : components) sumStates += comp.getMax()+1; 
		minTransientSize=(int)Math.pow(sumStates, 1.5);

		//fixed
		quiet=true;
		plots=true;
		tauInit=3;
		strategy=AvatarStrategy.MatrixInversion;
		keepTransients=true;
		keepOracle=true;
		minCSize=4;
	}

}