package org.ginsim.servicegui.tool.circuit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.ginsim.common.application.Txt;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.gui.utils.data.models.MaxSpinModel;
import org.ginsim.gui.utils.data.models.MinMaxSpinModel;
import org.ginsim.gui.utils.data.models.MinSpinModel;
import org.ginsim.gui.utils.widgets.EnhancedJTable;
import org.ginsim.service.tool.circuit.CircuitSearchStoreConfig;


/**
 * configure the circuit-search.
 */
public class CircuitConfigureSearch extends JPanel {
    private static final long serialVersionUID = 412805818821147248L;

    private CircuitSearchStoreConfig config;
    
    private JScrollPane jsp = null;
    private JTable jtable = null;
    private GsCircuitConfigModel model;
    private CircuitFrame circuitFrame;
    private GsCircuitSpinModel smodel;
    private JButton buttonReset;
    private List nodeOrder;
    
    /**
     * create the configuration window.
     * 
     * @param frame
     * @param config
     * @param nodeOrder 
     */
    public CircuitConfigureSearch(CircuitFrame frame, CircuitSearchStoreConfig config, List nodeOrder) {
        this.circuitFrame = frame;
        this.nodeOrder = nodeOrder;
        this.config = config;
        smodel = new GsCircuitSpinModel(config, frame);
        initialize();
        setVisible(true);
    }

    private void initialize() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel(Txt.t("STR_min")), c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        add(new JLabel(Txt.t("STR_max")), c);
        
        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        add(getButtonReset(), c);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(smodel.getSMin(), c);

        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(smodel.getSMax(), c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        add(getJsp(), c);
    }
    
    
    private JScrollPane getJsp() {
        if (jsp == null) {
            jsp = new JScrollPane();
            jsp.setViewportView(getJTable());
        }
        return jsp;
    }
    
    private JTable getJTable() {
        if (jtable == null) {
            model = new GsCircuitConfigModel(circuitFrame, config.v_list, config.t_status);
            jtable = new EnhancedJTable(model);
        }
        return jtable;
    }
    
    private JButton getButtonReset() {
        if (buttonReset == null) {
            buttonReset = new JButton(Txt.t("STR_reset"));
            buttonReset.addActionListener(new java.awt.event.ActionListener() { 
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    reset();
                }
            });
        }
        return buttonReset;
    }
    protected void reset() {
    	int len = nodeOrder.size();
    	smodel.setMinValue(1);
    	smodel.setMaxValue(len);
        for (int i=0 ; i<len ; i++) {
            config.t_status[i] = 3;
        }
        model.fireTableRowsUpdated(0, len);
    }

}


class GsCircuitConfigModel extends DefaultTableModel {
    private static final long serialVersionUID = -8900180159435512429L;
 
    private List<RegulatoryNode> v_list;
    private byte[] t_status;
    private CircuitFrame frame;
    
    /**
     * 
     * @param frame
     * @param v_list
     * @param t_status
     * @param t_constraint 
     */
    public GsCircuitConfigModel(CircuitFrame frame, List<RegulatoryNode> v_list, byte[] t_status) {
        this.frame = frame;
        this.v_list = v_list;
        this.t_status = t_status;
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Component";
            case 1:
                return "Required";
            case 2:
                return "Excluded";
        }
        return super.getColumnName(column);
    }

    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
            case 2:
                return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }

    public int getRowCount() {
        if (t_status == null) {
            return 0;
        }
        return t_status.length;
    }

    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return v_list.get(row);
            case 1:
            case 2:
                return t_status[row] == column ? Boolean.TRUE : Boolean.FALSE;
        }
        return super.getValueAt(row, column);
    }
    
    public void setValueAt(Object aValue, int row, int column) {
        if (column > 0 && column <= 3) {
        	if (aValue == Boolean.FALSE) {
                t_status[row] = (byte)3;
        	} else {
        		t_status[row] = (byte)column;
        	}
            fireTableRowsUpdated(row, row);
            frame.updateStatus(CircuitGUIStatus.NONE);
        }
    }
}

class GsCircuitSpinModel implements MinMaxSpinModel {

    private CircuitSearchStoreConfig config;
    private MinSpinModel m_min;
    private MaxSpinModel m_max;

    private JSpinner smin = null;
    private JSpinner smax = null;
    
    CircuitFrame frame;
    
    /**
     * @param config
     * @param frame
     */
    public GsCircuitSpinModel(CircuitSearchStoreConfig config, CircuitFrame frame) {
        this.config = config;
        this.frame = frame;
        m_min = new MinSpinModel(this);
        m_max = new MaxSpinModel(this);
    }
    
    public Object getNextMaxValue() {
        if (config.maxlen < config.v_list.size()) {
            config.maxlen++;
        }
        return getMaxValue();
    }

    public Object getPreviousMaxValue() {
        if (config.maxlen > 1) {
            if (config.maxlen == config.minlen) {
                config.minlen--;
                updateMin();
            }
            config.maxlen--;
        }
        return getMaxValue();
    }

    public Object getMaxValue() {
        return ""+config.maxlen;
    }

    public void setMaxValue(Object value) {
    	if (value == null) {
    		return;
    	}
    	try {
    		int val = Integer.parseInt( value.toString());
    		setMaxValue(val);
    	} catch (Exception e) {}
    }
    
    public void setMaxValue(int val) {
       if (val > 0 && val <= config.v_list.size()) {
           config.maxlen = val;
           if (val < config.minlen) {
               config.minlen = val;
               updateMin();
           }
          updateMax();
       }
    }

    public Object getNextMinValue() {
        if (config.minlen == config.maxlen) {
            if (config.maxlen < config.v_list.size()) {
                config.maxlen++;
                config.minlen++;
                updateMax();
            }
        } else {
            config.minlen++;
        }
        return getMinValue();
    }

    public Object getPreviousMinValue() {
        if (config.minlen > 1) {
            config.minlen--;
        }
        return getMinValue();
    }

    public Object getMinValue() {
        return ""+config.minlen;
    }

    public void setMinValue(int val) {
    	if (val > 0 && val <= config.v_list.size()) {
    		config.minlen = val;
    		if (val > config.maxlen) {
    			config.maxlen = val;
    			updateMax();
    		}
            updateMin();
    	}
    }
    
    public void setMinValue(Object value) {
        if (value == null) {
        	return;
        }
        try {
             int val = (byte)Integer.parseInt(value.toString());
             setMinValue(val);
        } catch (NumberFormatException e) {}
    }

    public JSpinner getSMin() {
        if (smin == null) {
            smin = new JSpinner(m_min);
            smin.setSize(90, smin.getHeight());
    		JTextField jtf = ((DefaultEditor)smin.getEditor()).getTextField();
    		jtf.setEditable(true);
    		jtf.setColumns(2);
        }
        return smin;
    }

    public JSpinner getSMax() {
        if (smax == null) {
            smax = new JSpinner(m_max);
            smax.setSize(90, smax.getHeight());
            
    		JTextField jtf = ((DefaultEditor)smax.getEditor()).getTextField();
    		jtf.setEditable(true);
    		jtf.setColumns(2);
            
        }
        return smax;
    }
    
    private void updateMin() {
        m_min.update();
        frame.updateStatus(CircuitGUIStatus.NONE);
    }
    private void updateMax() {
        m_max.update();
        frame.updateStatus(CircuitGUIStatus.NONE);
    }

	public String getMaxName() {
		return Txt.t("STR_max");
	}
	public String getMinName() {
		return Txt.t("STR_min");
	}
	public void setEditedObject(Object rawValue) {
	}
}
