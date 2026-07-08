package org.ginsim.service.tool.avatar.domain;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.utils.AvaMath;
import org.ginsim.service.tool.avatar.utils.NaturalOrderComparator;

public class Result {

	/** Associated type of simulation (e.g. Avatar, FF, MonteCarlo) **/
	public EnumAlgorithm strategy;
	/** Complex attractors (terminal cycles) **/
	public Map<String, AbstractStateSet> complexAttractors;
	/** Stable states **/
	public Map<String, State> pointAttractors;
	/** Pattern representation of complex attractors **/
	public Map<String, List<byte[]>> complexAttractorPatterns;
	/** Number of occurrences per attractor **/
	public Map<String, Integer> attractorsCount;
	/** Lower probability bound of attractors **/
	public Map<String, Double> attractorsLowerBound;
	/** Upper probability bound of attractors **/
	public Map<String, Double> attractorsUpperBound;
	/**
	 * Transient cycles
	 * public List of AbstractStateSet transients;
	 */
	public int maxTransientSize = -1;
	/** Depth of attractors (from a well-defined portion of the state space) **/
	public Map<String, List<Integer>> attractorsDepths;
	/** Charts to be plotted **/
	public Map<String, BufferedImage> charts;
	/** Simulation log **/
	public String log;
	/** Maximum number of iterations **/
	public int transientMinSize = -1;
	/** Maximum number of iterations **/
	public int runs = -1;
	/** Number of truncated iterations **/
	public int truncated = -1;
	/** Number of performed iterations **/
	public int performed = -1;
	/** Simulation time (miliseconds) **/
	public long time;
	/** Simulation memory (Mbytes) **/
	public int memory;
	/** FireFront residual probability **/
	public double residual;
	/** Fine name **/
	public String filename;
	/** Simulation name **/
	public String name;
	/** Parameters description **/
	public String parameters;
	/** Names of components **/
	public String nodes;
	/** Initial states associated with these results **/
	public List<byte[]> iconditions;
	/** Applied perturbations **/
	public String perturbation = null;
	/** Applied reductions **/
	public String reduction = null;
	/** Convergence **/
	public boolean convergence = true;

	private int complexAttID = 0;

	public Result() {
		attractorsDepths = new HashMap<String, List<Integer>>();
		complexAttractors = new HashMap<String, AbstractStateSet>();
		complexAttractorPatterns = new HashMap<String, List<byte[]>>();
		pointAttractors = new HashMap<String, State>();
		attractorsCount = new HashMap<String, Integer>();
		attractorsLowerBound = new HashMap<String, Double>();
		attractorsUpperBound = new HashMap<String, Double>();
		charts = new HashMap<String, BufferedImage>();
	}

	/**
	 * Adds a stable state to results
	 * 
	 * @param s
	 *            stable state to be added
	 */
	public void add(State s) {
		pointAttractors.put(s.key, s);
		attractorsCount.put(s.key, 1);
		attractorsDepths.put(s.key, new ArrayList<Integer>());
	}

	/**
	 * Adds a complex attractor (terminal cycle) to results
	 * 
	 * @param s
	 *            complex attractor to be added
	 */
	public void add(AbstractStateSet s) {
		s.setKey("CA" + (++complexAttID));
		complexAttractors.put(s.getKey(), s);
		attractorsCount.put(s.getKey(), 1);
		attractorsDepths.put(s.getKey(), new ArrayList<Integer>());
		// System.out.println("ADDED to:"+complexAttractors.keySet());
	}

	/**
	 * Adds a stable state to results
	 * 
	 * @param s
	 *            stable state to be added
	 * @param steps
	 *            depth of the stable state from a portion of the state space
	 */
	public void add(State s, int steps) {
		if (contains(s)) {
			attractorsCount.put(s.key, attractorsCount.get(s.key) + 1);
		} else {
			pointAttractors.put(s.key, s);
			attractorsCount.put(s.key, 1);
			attractorsDepths.put(s.key, new ArrayList<Integer>());
		}
		attractorsDepths.get(s.key).add(steps);
	}

	/**
	 * Adds a complex attractor (terminal cycle) to results
	 * 
	 * @param s
	 *            complex attractor to be added
	 * @param steps
	 *            depth of the complex attractor from a portion of the state space
	 */
	public void add(AbstractStateSet s, int steps) {
		add(s);
		attractorsDepths.get(s.getKey()).add(steps);
	}

	public void incrementComplexAttractor(String key, int steps) {
		attractorsCount.put(key, attractorsCount.get(key) + 1);
		attractorsDepths.get(key).add(steps);
	}

	/**
	 * Checks whether a stable state is stored
	 * 
	 * @param s
	 *            stable state to be checked
	 * @return true if the attractor is stored
	 */
	public boolean contains(State s) {
		return pointAttractors.containsKey(s.key);
	}

	/**
	 * Checks whether a complex attractor is stored
	 * 
	 * @param s
	 *            complex attractor to be checked
	 * @return true if the attractor is stored public boolean
	 *         contains(AbstractStateSet s) { return
	 *         complexAttractors.containsKey(s.getKey()); }
	 */

	/**
	 * Increments the number of occurrences for a stable state (assumes the results
	 * contain the attractor)
	 * 
	 * @param s
	 *            stable state whose occurrence is to be accounted
	 */
	public void increment(State s) {
		attractorsCount.put(s.key, attractorsCount.get(s.key) + 1);
	}

	/**
	 * Increments the number of occurrences for a complex attractor (assumes the
	 * results contain the attractor)
	 * 
	 * @param s
	 *            complex attractor whose occurrence is to be accounted
	 */
	public void increment(StateSet s) {
		attractorsCount.put(s.getKey(), attractorsCount.get(s.getKey()) + 1);
	}

	/**
	 * Bounds the probability of a given attractor
	 * 
	 * @param attractor
	 *            the attractor whose probability is to be bounded
	 * @param lower
	 *            lower bound
	 * @param upper
	 *            upper bound
	 */
	public void setBounds(String attractor, double lower, double upper) {
		attractorsLowerBound.put(attractor, Math.max(0, lower));
		attractorsUpperBound.put(attractor, Math.min(1.0, upper));
	}

	/**
	 * Adds a plotted chart to results
	 * 
	 * @param title
	 *            the name of the chart
	 * @param img
	 *            the chart to be stored
	 */
	public void addPlot(String title, BufferedImage img) {
		charts.put(title, img);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "\n" + toHTMLString().replace("<br>", "\n").replace("<b>", "").replace("</b>", "")
				.replace("&nbsp;", "  ").replace("&plusmn;", "+/-");
	}

	/**
	 * Serializes the results into HTML
	 * 
	 * @return HTML text describing the gathered results
	 */
	public String toHTMLString() {

		String result = "<b>Model name: " + filename + "<br>" + name + "</b><br><br><b>Parameters</b><br>"
				+ parameters.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;") + "<br>";
		if (perturbation == null)
			result += "<br>No perturbations applied<br>";
		else
			result += "<br>Applied perturbation: " + perturbation + "<br>";
		if (reduction == null)
			result += "No reductions applied<br>";
		else
			result += "Name of the selected reduction: " + reduction + "<br>";

		result += "<br><b>Nodes</b>=[" + nodes + "]<br>";
		if (iconditions != null) {
			result += "<br><b>Initial conditions</b><br>";
			for (byte[] s : iconditions)
				result += "&nbsp;&nbsp;&nbsp;" + AvatarUtils.toString(s).replace("-1", "*") + "<br>";
		}

		int sum = AvaMath.sumCollection(attractorsCount.values());
		double probleft = 0;
		result += "<br><b>Time</b>=" + (((double) time) / 1000.0) + "s";
		if (strategy.equals(EnumAlgorithm.AVATAR)) {
			probleft = ((double)(runs-sum))/(double)runs;
			result += "<br><b>Support</b>: " + sum + " successful runs (below max depth) out of " + runs
					+ "</br>";
		} else if (strategy.equals(EnumAlgorithm.FIREFRONT)) {
			if (!convergence) {
				result += "<br><b>Warning</b>: firefront could not converge before reaching the maximum specified depth "
						+ "(probabilities in F higher than beta).<br>";
			} else {
				result += "<br>Success: the simulation converged before reaching the maximum depth.<br>";
			}
			result += "<br/><br/><b>Probability of state sets</b>:";
			result += "<br/>&nbsp;&nbsp;&nbsp;Neglected set: " + AvatarUtils.round(this.residual);
			double attSum = 0;
			for (Double val : attractorsLowerBound.values()) {
				attSum += val;
			}
			result += "<br/>&nbsp;&nbsp;&nbsp;Attractor set: " + AvatarUtils.round(attSum) + "<br/>";
			
		} else {
			result += "<br><b>Support</b>: " + performed + " successful runs (below max depth) out of " + runs
					+ "</br>";
		}

		/** A: print the discovered attractors */
		if (pointAttractors.size() > 0) {
			result += "<br><b>Stable states</b>:<br>";
			List<String> lTmp = new ArrayList<String>(pointAttractors.keySet());
			Collections.sort(lTmp);
			for (int i = 0; i < lTmp.size(); i++) {
				String key = lTmp.get(i);
				result += "&nbsp;&nbsp;&nbsp;SS" + (i+1) + "&nbsp;=>&nbsp;"
						+ pointAttractors.get(key).toShortString();
				if (!strategy.equals(EnumAlgorithm.FIREFRONT)) {
					if (!strategy.equals(EnumAlgorithm.AVATAR)) {
						result += "&nbsp;prob=[" + AvatarUtils.round(attractorsLowerBound.get(key)) + "," 
									+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]";
						result += "&nbsp;depth=" + AvatarUtils.round(AvaMath.mean(attractorsDepths.get(key)), 1) + "&plusmn;"
								+ AvatarUtils.round(AvaMath.std(attractorsDepths.get(key)), 1) + "<br>";
					} else {
						double prob = AvatarUtils.round((double)(attractorsCount.get(key)/(double)runs));
						result += "&nbsp;prob=[" + prob + "," + (prob+probleft) + "]";
						//result += "&nbsp;prob=" + AvatarUtils.round((double) attractorsCount.get(key) / sum);
						// result+="&nbsp;counts="+attractorsCount.get(key);
						result += "<br>";
					}
				} else {
					result += "&nbsp;&nbsp;prob=[" + AvatarUtils.round(attractorsLowerBound.get(key)) + ","
							+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]";
					result += "&nbsp;depth=" + ((int) AvaMath.mean(attractorsDepths.get(key))) + "<br>";
				}
			}
		}
		if (complexAttractors.size() > 0) {
			result += "<br><b>Complex attractors</b>:<br>";
			// int i=0;
			List<String> lsCAs = new ArrayList<String>(complexAttractors.keySet());
			Collections.sort(lsCAs, new NaturalOrderComparator());
			for (String key : lsCAs) {
				if (complexAttractorPatterns.size() == 0)
					result += "&nbsp;&nbsp;&nbsp;" + key + "&nbsp;=>&nbsp;" + complexAttractors.get(key).toString();
				else
					result += "&nbsp;&nbsp;&nbsp;" + key + "&nbsp;=>&nbsp;"
							+ AvatarUtils.toString(complexAttractorPatterns.get(key)).replace("-1", "*");
				if (strategy.equals(EnumAlgorithm.FIREFRONT)) {
					result += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prob=["
							+ AvatarUtils.round(attractorsLowerBound.get(key)) + ","
							+ AvatarUtils.round(attractorsUpperBound.get(key)) + "]";
					result += "&nbsp;depth=" + (int) AvaMath.mean(attractorsDepths.get(key)) + "<br>";
				} else {
					double prob = AvatarUtils.round((double)(attractorsCount.get(key)/(double)runs));
					result += "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prob=[" + prob + "," + (prob+probleft) + "]";
					result += "&nbsp;size=" + complexAttractors.get(key).size();
					if (!strategy.equals(EnumAlgorithm.AVATAR) && attractorsDepths.containsKey(key))
						result += "&nbsp;depth=" + AvatarUtils.round(AvaMath.mean(attractorsDepths.get(key)),1) + "&plusmn;"
								+ AvatarUtils.round(AvaMath.std(attractorsDepths.get(key)),1) + "<br>";
					else
						result += "<br>";
				}
			}
		}

		/** B: prints statistics */
		if (strategy.equals(EnumAlgorithm.AVATAR)) {
			/*
			 * result+="<br><b>Countings</b>:&nbsp;{"; for(String k :
			 * attractorsCount.keySet()) result+=k+"="+attractorsCount.get(k)+",";
			 * if(attractorsCount.size()>0) result=result.substring(0,result.length()-1);
			 */
			if (maxTransientSize > 0)
				result += "<br><b>Transient found</b>: #" + maxTransientSize + " states";
			/*
			 * if(transients.size()>0){ result+=" with sizes {"; for(AbstractStateSet s :
			 * transients) result+=s.size()+",";
			 * result=result.substring(0,result.length()-1)+"}"; }
			 */
		}
		// result+="<br>Runs:"+runs+" truncated:"+truncated+"
		// performed:"+performed+"</br>";
		// result+="<br><b>Probability bounds</b> per attractor:<br>";
		return result;
	}

	/**
	 * Serializes the log into HTML
	 * 
	 * @return HTML text describing the stored log
	 */
	public String logToHTMLString() {
		return log.replace("\n", "<br>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	public String toCSVString() {
		String result = name + "\n\nParameters\n" + parameters.replace("\t", ",").replace("=", ",") + "\n\n";
		if (perturbation == null)
			result += "No perturbations applied\n";
		else
			result += "Applied perturbation," + perturbation + "\n";
		if (reduction == null)
			result += "No reductions applied\n";
		else
			result += "Applied reduction," + reduction + "\n";

		result += "Time," + (((double) time) / 1000.0) + ",secs\n\n";

		/** A: print the discovered attractors */

		int sum = AvaMath.sumCollection(attractorsCount.values());
		if (pointAttractors.size() > 0) {
			result += "Stable states\n," + nodes;
			if (strategy.equals(EnumAlgorithm.AVATAR)) {
				result += ",prob\n";
			} else if (strategy.equals(EnumAlgorithm.FIREFRONT)) {
				result += ",lowerbound,upperbound,depth\n";
			} else {
				result += ",prob,depth\n";
			}
			List<String> lTmp = new ArrayList<String>(pointAttractors.keySet());
			Collections.sort(lTmp);
			for (int i = 0; i < lTmp.size(); i++) {
				String key = lTmp.get(i);
				result += "SS" + (i+1) + "," + AvatarUtils.toOpenString(pointAttractors.get(key).state);
				if (!strategy.equals(EnumAlgorithm.FIREFRONT)) {
					result += "," + format("%.5f", ((double) attractorsCount.get(key)) / (double) sum);
					// result+=","+attractorsCount.get(key);
					if (!strategy.equals(EnumAlgorithm.AVATAR)) {
						result += "," + format("%.1f", AvaMath.mean(attractorsDepths.get(key))) + "+-"
								+ format("%.1f", AvaMath.std(attractorsDepths.get(key))) + "\n";
					} else {
						result += "\n";
					}
				} else {
					result += "," + format("%.5f", attractorsLowerBound.get(key)) + ","
							+ format("%.5f", attractorsUpperBound.get(key));
					result += "," + ((int) AvaMath.mean(attractorsDepths.get(key))) + "\n";
				}
			}
		}
		if (complexAttractors.size() > 0) {
			result += "\nComplex attractors\n," + nodes;
			if (strategy.equals(EnumAlgorithm.AVATAR)) {
				result += ",prob,size\n";
			} else if (strategy.equals(EnumAlgorithm.AVATAR)) {
				result += ",lowerbound,upperbound,depth\n";
			} else {
				result += ",prob,depth\n";
			}
			for (String key : complexAttractors.keySet()) {
				if (complexAttractorPatterns.size() == 0)
					result += "," + key + "," + complexAttractors.get(key).toString();
				else
					result += key + ",";
				boolean first = true;
				for (byte[] s : complexAttractorPatterns.get(key)) {
					result += AvatarUtils.toOpenString(s).replace("-1", "*");
					if (first) {
						first = false;
						if (strategy.equals(EnumAlgorithm.FIREFRONT)) {
							result += "," + format("%.5f", attractorsLowerBound.get(key)) + ","
									+ format("%.5f", attractorsUpperBound.get(key)) + ","
									+ (int) AvaMath.mean(attractorsDepths.get(key));
						} else {
							result += "," + format("%.5f", ((double) attractorsCount.get(key)) / (double) sum);// +","+attractorsCount.get(key);
							if (!strategy.equals(EnumAlgorithm.AVATAR) && attractorsDepths.containsKey(key)) {
								result += "," + format("%.1f", AvaMath.mean(attractorsDepths.get(key))) + "+-"
										+ format("%.1f", AvaMath.std(attractorsDepths.get(key))) + "\n";
							} else {
								result += "," + complexAttractors.get(key).size();
							}
						}
					}
					result += "\n,";
				}
			}
		}

		/** B: prints statistics */
		if (strategy.equals(EnumAlgorithm.AVATAR)) {
			if (maxTransientSize > 0)
				result += "\n,Max transient size," + maxTransientSize;
			/*
			 * if(transients.size()>0){ for(AbstractStateSet s : transients)
			 * result+=s.size()+";"; result=result.substring(0,result.length()-1)+";"; }
			 */
			result += "\n,Successful runs," + sum + "\n";
		} else {
			if (strategy.equals(EnumAlgorithm.FIREFRONT)) {
				if (performed == 0) {
					result += "\n,WARNING:;firefront could not converge before reaching the maximum specified depth. "
							+ "Please increase the maximum depth for a more precise analysis of stable states.\n";
				}
				// else result+="\n;Success:;the simulation converged before reaching the
				// inputted maximum depth.\n";
			} else {
				result += "\n,Support:,Successful runs," + performed + ",Total runs," + runs + "\n";
			}
		}
		return result;
	}

	private String format(String pattern, double value) {
		return String.format(pattern, value).replace(",", ".");
	}
}
