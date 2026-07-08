package org.ginsim.gui.graph.regulatorygraph.initialstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.io.avatar.AvatarUtils;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedState;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.common.utils.ListTools;
import org.ginsim.gui.utils.widgets.EnhancedJTable;


/**
 * model for the initState table.
 * "help" the user to select initial states for his simulation.
 */
public class InitStateTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -1553864043658960569L;
	private List<RegulatoryNode> nodeOrder;
    private Map m_initState = null;
    private Set<NamedState> m_selection = null;
    private NamedStateList imanager;
    private InitialStatePanel panel;
    private boolean several;
    private int columnShift = 1;
    private JTable theTable;
    //private List<Boolean> enabledEdition;

	/**
	 * simple constructor
	 * 
	 * @param panel
     * @param imanager
     * @param several
	 */	
    public InitStateTableModel(InitialStatePanel panel, NamedStateList imanager, boolean several) {
		super();
        this.panel = panel;
        this.imanager = imanager;
        this.several = several;
        //if(oracle) this.m_oracle = new HashSet<NamedState>();
        this.m_selection = new HashSet<NamedState>();
        nodeOrder = imanager.getNodeOrder();
        //enabledEdition = new ArrayList<Boolean>();
        //for(int i=0,l=getRowCount(); i<l; i++) enabledEdition.add(true);
	}

	public int getRowCount() {
        if (imanager == null ) return 0;
		return imanager.size()+1;
	}
	
	boolean disabled = false;
	
	public void setDisabling(boolean disabling) {
		disabled = disabling;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		//System.out.println("selection["+rowIndex+","+columnIndex+"]");
		/*if(columnShift==3 && columnIndex==2){
			if(rowIndex<imanager.size()){
				System.out.println(imanager.get(rowIndex));
				System.out.println(m_selection);
				if(m_selection.contains(imanager.get(rowIndex))) return true;
			}
			return false;
		}
		if(columnShift==3 && columnIndex>=3){
			if(rowIndex<imanager.size()){
				//System.out.println("OK!");
				System.out.println(imanager.get(rowIndex));
				System.out.println(m_selection);
				if(m_selection.contains(imanager.get(rowIndex))){
					//System.out.println("YYYEEEESSS!");
					return false;
				}
			}
			return true;
		}*/
			/*if(rowIndex<enabledEdition.size()) return enabledEdition.get(rowIndex);
			else return true;*/
		if(columnIndex<columnShift && rowIndex>=imanager.size()) return false;
		if(disabled && columnIndex>=2) return false;
		return true;
	}
	
	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 1 && m_initState != null) {
			return Boolean.class;
		}
		/*if (columnIndex == 2 && m_oracle != null) {
			return Boolean.class;
		}*/
		return String.class;
	}
	
	/**
	  * @see javax.swing.table.TableModel#getValueAt(int, int)
	  * 
	  * here, each cell contains a vector of integer to represent a list of ranges/values
	  * 
	  * we want to show the vector [1 ; 2 ; 3 ; 4 ;  6 ; 7] as : "1-4 ; 6-7"
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		List element;

		if (columnIndex == 0) {
			if (rowIndex >= imanager.size()) {
				return "";
			}
			return imanager.get(rowIndex).getName();
		}
		if (m_initState != null && columnIndex == 1) {
			if ( rowIndex >= imanager.size()) {
				return Boolean.FALSE;
			}
			if ( m_initState.containsKey(imanager.get(rowIndex))) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		/*if (m_oracle != null && columnIndex == 2) {
			if ( rowIndex >= imanager.size()) {
				return Boolean.FALSE;
			}
			if (m_oracle.contains(imanager.get(rowIndex))) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}*/
		
		int ci = columnIndex - columnShift;
		if (imanager == null || rowIndex >= imanager.size()) {
			return "";
		}
        Map m_row = imanager.get(rowIndex).getMaxValueTable();
        element = (List)m_row.get(nodeOrder.get(ci).getNodeInfo());
        return showValue(element, nodeOrder.get(ci).getMaxValue());
    }
    
    /**
     * 
     * @param element
     * @param maxvalue
     * @return a formated String showing values for this gene
     */
    public static Object showValue(List element, int maxvalue) {
        if (element == null) {
            return "*";
        }
		
        String ret = "";
		int i=1;
		
		int len = element.size()-1;
		int precval =((Integer)element.get(0)).intValue();
		int nextval = precval+1;
		int val;

		if (precval == maxvalue) {
			ret += "M"+maxvalue+"";
		} else {
			ret += precval;
		}
		nextval = precval+1;
		for ( ; i<len ; i++) {
			val = ((Integer)element.get(i)).intValue();
			if ( val == nextval ) {  // we are still in the same serial
				nextval++;
			}
			else {
				// close previous serial
				if ( nextval != precval+1) {
					if (precval-1 == maxvalue) {
					} else {
						if (nextval-1 == maxvalue) {
							ret += "-M"+maxvalue+"";
						} else {
							ret += "-" + (nextval-1);
						}
					}
				}
				// open a new serial
				if (val == maxvalue) {
					ret += " ; M"+maxvalue+"";
				}else {
					ret += " ; " + val;
				}
				precval = val;
				nextval = val+1;
			}
		}
		i = element.size()-1;
		if ( i > 0) {  // we have to threat the last val differently !
			val = ((Integer)element.get(i)).intValue();
			if ( val == nextval ) {
				if (val == maxvalue) {
					ret += "-M"+maxvalue+"";
				} else {
					ret += "-" + val;
				}
			} else {
				// close previous serial
				if ( nextval != precval+1) {
					if (nextval-1 == maxvalue) {
						ret += "-M"+maxvalue+"";
					} else {
						ret += "-" + (nextval-1);
					}
				}
				//write the last element
				if (val == maxvalue) {
					ret += " ; M"+maxvalue+"";
				} else {
					ret += " ; " + val;
				}
			}
		}
		return ret;
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 * 
	  * here, each cell contains a vector of integer to represent a list of ranges/values
	  * 
	  * here we have to parse strings like "1-4 ; 6-7" to construct vector like [1 ; 2 ; 3 ; 4 ;  6 ; 7]
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		panel.setMessage("");
		if (rowIndex > getRowCount() || (columnIndex-columnShift)>=nodeOrder.size()) {
			return;
		}
		
		if (columnIndex == 0) {
			if (rowIndex >= imanager.size()) {
				return;
			}
			imanager.get(rowIndex).setName((String)aValue);
			// FIXME: the name should be unique
			return;
		}
		if (columnIndex == 1 && m_initState != null) {
			if (rowIndex >= imanager.size()) {
				return;
			}
			if (aValue == Boolean.TRUE) {
				if (!several) {
					m_initState.clear();
					//fireTableDataChanged();
				}
				Object o = imanager.get(rowIndex);
                m_initState.put(o, null);
			} else {
				m_initState.remove(imanager.get(rowIndex));
				// set it to null if empty ? probably _not_ a good idea
			}
			return;
		}
		/*if (columnIndex == 2 && m_oracle != null) {
			if (rowIndex >= imanager.size()) return;
			if (aValue == Boolean.TRUE) {
				if (!several) m_oracle.clear();
				m_oracle.add(imanager.get(rowIndex));
			} else {
				m_oracle.remove(imanager.get(rowIndex)); //set it to null if empty ? probably _not_ a good idea
			}
			return;
		}*/
		
		int[] r_sel = theTable.getSelectedRows();
		int[] c_sel = theTable.getSelectedColumns();
		for (int i=0 ; i<r_sel.length ; i++) {
			for (int j=0 ; j<c_sel.length ; j++) {
				doSetValueAt(aValue, r_sel[i], c_sel[j]);
			}
		}
	}
	
	public void doSetValueAt(Object aValue, int rowIndex, int columnIndex) {
		int ci = columnIndex - columnShift;
		int maxvalue = nodeOrder.get(ci).getMaxValue();
        if (aValue == null || ((String)aValue).trim().equals("") || ((String)aValue).trim().equals("*")) {
            if (rowIndex >= 0 && rowIndex < getRowCount()-1) {
                Map m_line = ((NamedState)imanager.get(rowIndex)).getMaxValueTable();
                m_line.remove(nodeOrder.get(ci));
                if (m_line.size() == 0) {
                    imanager.remove(rowIndex);
                    fireTableStructureChanged();
                }
            }
            return;
        }
        
		// change the value if appropriate
        List newcell = new ArrayList(1);
		try {
			String[] values = ((String)aValue).split(";");
			for (int i=0 ; i<values.length ; i++) {
				String[] minmax = values[i].split("-");
				if (minmax.length > 2) {
					panel.setMessage("syntax error");
					return;
				}
				// remove spaces !!
				int min;
				int max;
				String s_tmp = minmax[0].trim();
				if (s_tmp.equalsIgnoreCase("m") || s_tmp.equalsIgnoreCase("max")) {
					if (minmax.length>1) {
						panel.setMessage("interval starting at max !!");
						return;
					}
					min = maxvalue;
				} else {
					min = Integer.parseInt(s_tmp);
				}
				if (minmax.length == 2) {
					s_tmp = minmax[1].trim(); 
					if (s_tmp.equalsIgnoreCase("m") || s_tmp.equalsIgnoreCase("max")) {
						max = maxvalue;
					} else {
						max = Integer.parseInt(s_tmp);
					}
					if (max < min) {
						panel.setMessage("bad interval");
						return; 
					}
				} else {
					max = min;
				}
				if (max > maxvalue || min > maxvalue || max<0 ||min<0 ) {
					panel.setMessage("bad value: out of range");
					return;
				}
				while (min <= max) {
					newcell.add(new Integer(min));
					min++;
				}
			}
            // if on the last line: create a new line an check it
            if (rowIndex == imanager.size()) {
            	imanager.add();
            	fireTableRowsInserted(rowIndex, rowIndex);
            	if (m_initState != null) {
            		setValueAt(Boolean.TRUE, rowIndex, 1);
            	}
            	/*if (m_oracle != null) {
            		setValueAt(Boolean.FALSE, rowIndex, 2);
            	}*/
            }
            Map m_line = imanager.get(rowIndex).getMaxValueTable();
            m_line.put(nodeOrder.get(ci).getNodeInfo(),newcell);
			fireTableCellUpdated(rowIndex,ci+2);
		} catch (Exception e) {}
	}

	public void copyLine(int line) {
		Map m_line = imanager.get(line).getMaxValueTable();
		NamedState newState = imanager.get(imanager.add());
		newState.setMaxValueTable( new HashMap(m_line));
		fireTableDataChanged();
	}
	
	/**
	 * Move the selection.
	 * 
	 * @param sel
	 * @param direction
	 */
	public void moveLine(int[] sel, int direction) {
		ListTools.moveItems(imanager, sel, direction);
		fireTableDataChanged();
	}
	
	public String getColumnName(int columnIndex) {
		if ((columnIndex-columnShift)>=nodeOrder.size()) {
			return null;
		}
		if (columnIndex == 0) {
			return "name";
		}
		if (columnIndex == 1 && m_initState != null) {
			return "use";
		}
		/*if (columnIndex == 2 && m_oracle != null) {
			return "oracle";
		}*/
		RegulatoryNode node = nodeOrder.get(columnIndex-columnShift);
		if (node.isOutput()) {
			return "[O] "+node.getId();
		}
		return node.getId();
	}

	public int getColumnCount() {
		return nodeOrder.size()+columnShift;
	}
    
	/**
	 * empty the table of init states.
	 *
	 */
	public void reset() {
        if(m_initState != null) m_initState.clear();
        //if(m_oracle != null) m_oracle.clear();
	    //if(m_initState != null || m_oracle != null) fireTableStructureChanged();
	}
	
	/**
	 * delete a row
	 * @param row the index of the row to delete
	 */
	public void deleteRow(int row) {
        if (imanager == null || row < 0 || row == getRowCount()-1) {
            return;
        }
        imanager.remove(row);
        fireTableRowsDeleted(row, row);
	}

    /**
     * reverse job of the "getContent" method
     * @param param
     */
    public void setParam(Map param) {
        this.m_initState = param;
        if (param == null)  columnShift = 1;
        //else if(m_oracle!=null) columnShift = 3;
        else columnShift = 2;
        fireTableStructureChanged();
    }
    
	public void disabledEdition(List<NamedState> states) {
		for(NamedState s : states) m_selection.add(s);
	}
	
	public void setEdition(boolean[] s) {
		//System.out.println("#"+AvatarUtils.toString(s));
		if(imanager==null || imanager.size()==0) return;
		for(int i=0, l=s.length; i<l; i++) 
			if(s[i]) m_selection.add(imanager.get(i));
		//System.out.println(m_selection);
	}
	public boolean[] getEdition() {
		//System.out.println("Call:"+imanager.size());
		boolean[] selection = new boolean[imanager.size()];
		for(NamedState s : m_selection) selection[imanager.indexOf(s)]=true;
		return selection;
	}

	public void setTable(EnhancedJTable tableInitStates) {
		theTable = tableInitStates;
	}

    public void toggleSelectAll() {
        if (m_initState == null) {
            return;
        }

        if (m_initState.size() > 0) {
            m_initState.clear();
            fireTableDataChanged();
        } else {
            for (int i=0 ; i<getRowCount() ; i++) {
                setValueAt(Boolean.TRUE, i, 1);
            }
            fireTableDataChanged();
        }
    }


}
