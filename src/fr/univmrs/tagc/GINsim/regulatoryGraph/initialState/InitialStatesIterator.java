package fr.univmrs.tagc.GINsim.regulatoryGraph.initialState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;

public class InitialStatesIterator implements Iterator {

	StatesIterator it_states;
	Iterator it_input = null;
	List nodeOrder;

        
	public InitialStatesIterator(List nodeOrder, GsInitialStateStore store) {
	    this(nodeOrder, store.getInputState(), store.getInitialState());
	}
    public InitialStatesIterator(List nodeOrder, Map m_input, Map m_init) {
        it_input = new StatesIterator(nodeOrder, m_input, null);
        it_states = new StatesIterator(nodeOrder, m_init, (short[])it_input.next());
	}

   public boolean hasNext() {
        return it_states.hasNext();
    }

    public Object next() {
        Object ret = it_states.next();
        if (!it_states.hasNext() && it_input.hasNext()) {
            it_states.reset((short[])it_input.next());
        }
        return ret;
    }

    public void remove() {
        // not supported
    }
}

class StatesIterator implements Iterator {
    Iterator helper;
    Iterator helperIterator = null;
    List nodeOrder;
    Map m_init;
    short[] refLine;

    public StatesIterator(List nodeOrder, Map m_initstates, short[] refLine) {
		this.nodeOrder = nodeOrder;
		this.m_init = m_initstates;
		reset(refLine);
	}
	
    public void reset(short[] refLine) {
        this.refLine = refLine;
        if (m_init == null || m_init.size() < 1) {
            List v = new ArrayList();
            v.add(new GsInitialState());
            helperIterator = v.iterator();
        } else {
            helperIterator = m_init.keySet().iterator();
        }
        helper = new Reg2DynStatesIterator(nodeOrder, 
                ((GsInitialState)helperIterator.next()).getMap(), refLine);
    }
    
	public boolean hasNext() {
		return helper.hasNext();
	}

	public Object next() {
		Object ret = helper.next();
		if (!helper.hasNext() && helperIterator != null && helperIterator.hasNext()) {
			helper = new Reg2DynStatesIterator(nodeOrder,
					((GsInitialState)helperIterator.next()).getMap(), refLine);
		}
		return ret;
	}

	public void remove() {
		// not supported
	}
}


/**
 * this iterator generates some initial states
 * they are constructing from list of value for each node...
 */
final class Reg2DynStatesIterator implements Iterator {
	
	short[] state;
	short[] using;
	int nbGenes;
	List nodeOrder;
	int[][] line;
	boolean goon;

    public Reg2DynStatesIterator(List nodeOrder, Map m_line, short[] refLine) {
		this.nodeOrder = nodeOrder;
        
		line = new int[nodeOrder.size()][];
		for (int i=0 ; i<nodeOrder.size() ; i++) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex)nodeOrder.get(i);
			List v_val = (List)m_line.get(vertex);
			if (v_val == null || v_val.size() == 0) {
			    if (refLine != null && refLine[i] == -1) {
    				line[i] = new int[vertex.getMaxValue()+1];
    				for (int j=0 ; j<line[i].length ; j++) {
    					line[i][j] = j;
    				}
			    } else {
                    line[i] = new int[1];
                    line[i][0] = refLine == null ? -1 : refLine[i];
                } 
			} else {
				line[i] = new int[v_val.size()];
				for (int j=0 ; j<line[i].length ; j++) {
					line[i][j] = ((Integer)v_val.get(j)).shortValue();
				}
			}
		}
        
		nbGenes = nodeOrder.size();
		if (nbGenes < 1 | line.length != nbGenes) {
			goon = false;
			return;
		}
		goon = true;
		
		state = new short[nbGenes];
		using = new short[nbGenes];
		for(int i=0 ; i<nbGenes ; i++){
		    // initialize all genes on their first value
		    state[i] = (short) line[i][0];
		}
	}
	
	/**
	 * 
	 * @return true if other state can be generated
	 */
	public boolean hasNext() {
		return goon;
	}

	/**
	 * 
	 * @return the next state
	 */
	public Object next() {
		if (!goon) {
			return null;
		}

		short[] ret = new short[nbGenes];
        for (int i=0 ; i<nbGenes ; i++) {
            ret[i] = state[i];
        }

		// go to the next one
		goon = false;
		for (int i=0 ; i<nbGenes ; i++) {
				
			if (using[i] < line[i].length-1) {
				using[i]++;
				state[i] = (short) line[i][using[i]];
				for (int j=0 ; j<i ; j++) {
					using[j] = 0;
                    state[j] = (short) line[j][using[j]];
				}
				goon = true;
				break;
			}
		}
		return ret;
	}

	public void remove() {
		// not implemented
	}
}
