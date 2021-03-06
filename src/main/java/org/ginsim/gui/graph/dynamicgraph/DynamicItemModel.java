package org.ginsim.gui.graph.dynamicgraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import org.ginsim.common.application.LogManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.dynamicgraph.DynamicEdge;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.dynamicgraph.DynamicNode;
import org.ginsim.gui.GUIManager;

/**
 * table model to display a dynamic node or edge.
 * 
 * TODO: this should move to the GUI
 */
public class DynamicItemModel extends AbstractTableModel implements StateTableModel {

    private static final long serialVersionUID = 8860415338236400531L;
    private List nodeOrder;
    private String[] extraNames;
    DynamicGraph graph;
    private byte[] state, extraState;
    private byte[][] extraNext, extraPrev;
    private DynamicNode[] nextState;
    private DynamicNode[] prevState;
    private JButton[] go2Next;
    private int len, fullLength;
    private int nbNext;
    private int nbRelated;
    
    protected DynamicItemModel (DynamicGraph graph) {
        this.graph = graph;
        this.nodeOrder = graph.getNodeOrder();
        len = nodeOrder.size()+1;
        fullLength = len;

        extraNames = graph.getExtraNames();
        if (extraNames != null && extraNames.length > 0) {
			extraState = new byte[extraNames.length];
			fullLength += extraState.length;
        } else {
            extraState = null;
        }

    }
    
    public int getRowCount() {
        if (state == null) {
            return 0;
        }
        return nbRelated+1;
    }

    public int getColumnCount() {
        return fullLength;
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return JButton.class;
        }
        return super.getColumnClass(columnIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex >= fullLength) {
            return null;
        }
        if (columnIndex == 0) {
            if (rowIndex > 0) {
                return go2Next[rowIndex-1];
            }
            return "";
        }
        if (rowIndex == 0) {
            if (columnIndex >= len) {
                return "~"+extraState[columnIndex-len];
            }
            return ""+state[columnIndex-1];
        }
        if (rowIndex > nbNext) {
        	int r = rowIndex - nbNext;
        	if (prevState == null || r > prevState.length) {
        		return null;
        	}
            if (columnIndex >= len) {
                return "~"+extraPrev[r-1][columnIndex-len];
            }
        	return ""+prevState[r-1].state[columnIndex-1];
        }
        if (columnIndex >= len) {
            return "~"+extraNext[rowIndex-1][columnIndex-len];
        }
        return ""+nextState[rowIndex-1].state[columnIndex-1];
    }

    public String getColumnName(int column) {
        if (column >= fullLength) {
            return null;
        }
        if (column >= len) {
            return "~"+extraNames[column - len];
        }
        if (column == 0) {
            return "";
        }
        return nodeOrder.get(column-1).toString();
    }

    /**
     * @param obj the edited object
     */
    public void setContent(Object obj) {
    	nbRelated = 0;
        if (obj instanceof DynamicNode) {
        	DynamicNode node = (DynamicNode)obj;
            state = node.state;
            nextState = getRelatedNodes(graph.getOutgoingEdges(node), true);
            prevState = getRelatedNodes(graph.getIncomingEdges(node), false);
            nbNext = nextState != null ? nextState.length : 0;
            nbRelated = nbNext + ( prevState != null ? prevState.length : 0 );

            // fill in the extra values
            if (extraNames != null && extraNames.length > 0) {
                graph.fillExtraValues(state, extraState);
                extraNext = fillExtra(nextState);
                extraPrev = fillExtra(prevState);
            }

        } else if (obj instanceof Edge){
            Edge<DynamicNode> edge = (Edge)obj;
            state = edge.getSource().state;
            nextState = new DynamicNode[1];
            nextState[0] = (DynamicNode)edge.getTarget();
            prevState = null;
            nbNext = nextState != null ? nextState.length : 0;
            nbRelated = nbNext;

            // fill in the extra values
            if (extraNames != null && extraNames.length > 0) {
                graph.fillExtraValues(state, extraState);
                extraNext = fillExtra(nextState);
                extraPrev = null;
            }
        } else if (obj != null) {
        	LogManager.error("Invalid type of dynamic item: "+obj.getClass());
        }

        if (nbRelated == 0) {
            go2Next = null;
        } else {
            go2Next = new JButton[nbRelated];
            for (int i=0 ; i<nbNext ; i++) {
                go2Next[i] = new JButton("->");
                go2Next[i].addActionListener(new Go2ActionListener(graph, nextState[i]));
            }
            if (prevState != null) {
	            for (int i=0 ; i<prevState.length ; i++) {
	                go2Next[nbNext+i] = new JButton("<-");
	                go2Next[nbNext+i].addActionListener(new Go2ActionListener(graph, prevState[i]));
	            }
            }
        }
        fireTableDataChanged();
    }

    private byte[][] fillExtra(DynamicNode[] states) {
        if (states == null) {
            return null;
        }
        byte[][] extraStates = new byte[states.length][extraNames.length];
        for (int i=0 ; i< states.length ; i++) {
            extraStates[i] = graph.fillExtraValues(states[i].state, extraStates[i]);
        }
        return extraStates;
    }

    private DynamicNode[] getRelatedNodes(Collection<DynamicEdge> l_related, boolean target) {
        if (l_related == null || l_related.size() == 0) {
            return null;
        }
        DynamicNode[] ret = new DynamicNode[l_related.size()];
        int i=-1;
        for (Edge<DynamicNode> edge: l_related) {
        	i++;
        	if (target) {
        		ret[i] = edge.getTarget();
        	} else {
        		ret[i] = edge.getSource();
        	}
        }
        return ret;
    }

	public boolean isOutgoing(int row) {
		return row > 0 && row<=nbNext;
	}

	@Override
	public byte[] getState(int index) {
		if (index == 0) {
			return state;
		}
		
        if (index > nbNext) {
        	int r = index - nbNext;
        	if (prevState == null || r > prevState.length) {
        		return null;
        	}
        	return prevState[r-1].state;
        }
        
        return nextState[index-1].state;
	}

	@Override
	public int getComponentCount() {
		return nodeOrder.size();
	}

	@Override
	public String getComponentName(int index) {
		return nodeOrder.get(index).toString();
	}
}

class Go2ActionListener implements ActionListener {

    DynamicGraph graph;
    DynamicNode node;
    
    /**
     * @param graph
     * @param node
     */
    public Go2ActionListener(DynamicGraph graph, DynamicNode node) {
        this.graph = graph;
        this.node = node;
    }

    public void actionPerformed(ActionEvent e) {
        GUIManager.getInstance().getGraphGUI(graph).getSelection().selectNode(node);
    }
    
}
