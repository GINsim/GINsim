package org.ginsim.core.graph.regulatorygraph.perturbation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.colomoto.logicalmodel.NodeInfo;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

/**
 * The list of perturbations.
 * It behaves like a single list, but it contains two separate lists for
 * perturbations of single components and multiple perturbations.
 * As a result, not all list operations are supported,
 * iterator() and get(int) are the main intended uses.
 * 
 * perturbations should be added using specialised methods.
 * 
 * @author Aurelien Naldi
 */
public class RegulatoryMutants implements Iterable<Perturbation> {

	private final List<Perturbation> simplePerturbations = new ArrayList<Perturbation>();
	private final List<Perturbation> multiplePerturbations = new ArrayList<Perturbation>();

	private final RegulatoryGraph lrg;
	
	public RegulatoryMutants(RegulatoryGraph lrg) {
		this.lrg = lrg;
	}
	
	/**
	 * Add a perturbation to fix a component.
	 * 
	 * @param component
	 * @param value
	 * @return
	 */
	public Perturbation addFixedPerturbation(NodeInfo component, int value) {
		Perturbation p = new PerturbationFixed(component, value);
		return addSimplePerturbation(p);
	}

	/**
	 * Add a perturbation to fix the value of a component in a range.
	 * 
	 * @param component
	 * @param min
	 * @param max
	 * @return
	 */
	public Perturbation addRangePerturbation(NodeInfo component, int min, int max) {
		if (min == max) {
			return addFixedPerturbation(component, min);
		}
		
		Perturbation p = new PerturbationRange(component, min, max);
		return addSimplePerturbation(p);
	}

	public Perturbation addMultiplePerturbation(List<Perturbation> perturbations) {
		if (!simplePerturbations.containsAll(perturbations)) {
			LogManager.debug("unknown perturbations when adding multiple...");
		}
		Perturbation p = new PerturbationMultiple(perturbations);
		multiplePerturbations.add(p);
		return p;
	}
	
	/**
	 * Add a new simple perturbation.
	 * First lookup if it exists to avoid duplicates.
	 * 
	 * @param p
	 * @return the added perturbation or an existing equivalent one.
	 */
	private Perturbation addSimplePerturbation(Perturbation p) {
		if (p == null) {
			throw new RuntimeException("Can not add an undefined perturbation");
		}
		
		// for for an existing perturbation
		for (Perturbation other: simplePerturbations) {
			if (other.equals(p)) {
				return other;
			}
		}
		
		// no equivalent perturbation way found: add it
		simplePerturbations.add(p);
		return p;
	}
	
	public List<Perturbation> getSimplePerturbations() {
		return simplePerturbations;
	}
	public List<Perturbation> getMultiplePerturbations() {
		return multiplePerturbations;
	}
	public List<Perturbation> getAllPerturbations() {
		if (multiplePerturbations.size() < 1) {
			return simplePerturbations;
		}
		
		List<Perturbation> all = new ArrayList<Perturbation>(simplePerturbations);
		all.addAll(multiplePerturbations);
		return all;
	}
	

	public int size() {
		return simplePerturbations.size() + multiplePerturbations.size();
	}

	public Perturbation get(int index) {
		int nbsimple = simplePerturbations.size();
		if (index < nbsimple) {
			return simplePerturbations.get(index);
		}
		
		return multiplePerturbations.get(index-nbsimple);
	}


	@Override
	public Iterator<Perturbation> iterator() {
		if (multiplePerturbations.size() == 0) {
			return simplePerturbations.iterator();
		}
		return new JoinedIterator<Perturbation>(simplePerturbations.iterator(), multiplePerturbations.iterator());
	}

	public void toXML(XMLWriter out) throws IOException {
		
        out.openTag("mutantList");
        for (Perturbation p: simplePerturbations) {
        	// wrap them
            out.openTag("mutant");
            out.addAttr("name", p.toString());
            p.toXML(out);
            out.closeTag();
        }
        for (Perturbation p: multiplePerturbations) {
            p.toXML(out);
        }
        out.closeTag();

		
	}

	public NodeInfo[] getNodes() {
		List<RegulatoryNode> nodes = lrg.getNodeOrder();
		NodeInfo[] ret = new NodeInfo[nodes.size()];
		int idx = 0;
		for (RegulatoryNode node: nodes) {
			ret[idx++] = node.getNodeInfo();
		}
		return ret;
	}

}


class JoinedIterator<T> implements Iterator<T> {

	private final Iterator<T> it1, it2;

	public JoinedIterator(Iterator<T> it1, Iterator<T> it2) {
		this.it1 = it1;
		this.it2 = it2;
	}
	
	@Override
	public boolean hasNext() {
		return it1.hasNext() || it2.hasNext();
	}

	@Override
	public T next() {
		if (it1.hasNext()) {
			return it1.next();
		}
		return it2.next();
	}

	@Override
	public void remove() {
		throw new RuntimeException("Remove not supported");
	}
	
}