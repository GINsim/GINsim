package org.ginsim.servicegui.tool.reg2dyn;

import org.ginsim.common.application.OptionStore;
import org.ginsim.common.application.Txt;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitialStatePanel;
import org.ginsim.gui.utils.data.ListEditionPanel;
import org.ginsim.gui.utils.data.ListPanelCompanion;
import org.ginsim.gui.utils.dialog.stackdialog.LogicalModelActionDialog;
import org.ginsim.gui.utils.dialog.stackdialog.StackDialog;
import org.ginsim.service.tool.reg2dyn.Reg2DynService;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * The panel to edit a simulation parameter.
 *
 */
public class SimulationParameterEditionPanel extends JPanel implements ListPanelCompanion<SimulationParameters, SimulationParameterList> {

    private final String[] simulationMethodsNames = {
    	SimulationStrategy.STG.toString(),
    	SimulationStrategy.SCCG.toString(),
    	SimulationStrategy.HTG.toString()
    };
    private SimulationParameterList paramList;
    private SimulationParameters currentParameter;

    private JPanel simulationStrategyPanel;
    private InitialStatePanel initStatePanel = null;

    private ChangeListener radioChangeListener = null;

    private JComboBox simulationMethodsComboBox;

    private ButtonGroup depthGrp = new ButtonGroup();
    private JRadioButton radioDephtFirst = null;
    private JRadioButton radioBreadthFirst = null;

    private final StackDialog stackDialog;

    private JTextField textMaxDepth = null;
    private JTextField textMaxNodes = null;
    private JLabel labelMaxDepth = null;
    private JLabel labelMaxNodes = null;
    private PrioritySelectionPanel selectPriorityClass;

    private Insets indentInset = new Insets(0, 30, 0, 0);

    boolean refreshing = false;

    public SimulationParameterEditionPanel(ListEditionPanel<SimulationParameters, SimulationParameterList> editPanel, StackDialog dialog) {
        super(new GridBagLayout());
        this.stackDialog = dialog;
        editPanel.addPanel(this, "MAIN");
        editPanel.showPanel("MAIN");
    }

    private void initialize() {
        // the simulation strategy part
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.4;
        add(getSimulationStrategyPanel(), c);

        c.gridx--;
        add(getPriorityClassSelector(), c);

        // initial state
        initStatePanel = new InitialStatePanel(paramList.graph, true);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        add(initStatePanel, c);

        updateSimulationMethod();
    }


    public JPanel getSimulationStrategyPanel() {
        if (simulationStrategyPanel == null) {
            simulationStrategyPanel = new JPanel( new GridBagLayout() );
            simulationStrategyPanel.setBorder(BorderFactory.createTitledBorder(Txt.t("STR_reg2dyn_mode")));

            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1;
            c.insets = indentInset;
            c.fill = GridBagConstraints.BOTH;

            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 4;
            simulationStrategyPanel.add(getSimulationMethodsComboBox(), c);

            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 2;
            simulationStrategyPanel.add(getRadioBreadthFirst(), c);
            c.gridx += 2;
            simulationStrategyPanel.add(getRadioDephtFirst(), c);

            // size and depth limits
            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 1;
            labelMaxDepth = new JLabel(Txt.t("STR_maximum_depth"));
            simulationStrategyPanel.add(labelMaxDepth, c);

            c.gridx++;
            c.weightx = 1;
            simulationStrategyPanel.add(getTextMaxDepth(), c);

            c.gridx++;
            c.weightx = 0;
            c.anchor = GridBagConstraints.WEST;
            labelMaxNodes = new JLabel(Txt.t("STR_maximum_nodes"));
            simulationStrategyPanel.add(labelMaxNodes, c);

            c.gridx++;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            simulationStrategyPanel.add(getTextMaxNodes(), c);
        }
        return simulationStrategyPanel;
    }

/* *************** INITIALISING THE WIDGETS (RADIO, COMBOBOX...) **********************/

    /**
     * This method initializes radioDephtFirst
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getRadioDephtFirst() {
        if (radioDephtFirst == null) {
            radioDephtFirst = new JRadioButton(Txt.t("STR_depth_first"));
            radioDephtFirst.setSelected(true);
            depthGrp.add(radioDephtFirst);
            radioDephtFirst.addChangeListener(getRadioChangeListener());
        }
        return radioDephtFirst;
    }
    /**
     * This method initializes radioBreadthFirst
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getRadioBreadthFirst() {
        if (radioBreadthFirst == null) {
            radioBreadthFirst = new JRadioButton(Txt.t("STR_breadth_first"));
            depthGrp.add(radioBreadthFirst);
            radioBreadthFirst.addChangeListener(getRadioChangeListener());
        }
        return radioBreadthFirst;
    }

    private JComboBox getSimulationMethodsComboBox() {
        if (simulationMethodsComboBox == null) {
            simulationMethodsComboBox = new JComboBox(simulationMethodsNames);
            try {
				String selected = (String) OptionStore.getOption("simulation.defaultMethod");
				if (selected != null) {
					simulationMethodsComboBox.setSelectedItem(selected);
				}
			} catch (ClassCastException e) {
            	// Legacy .config/org.ginsim.xml files with integers
				Integer selectedIndex = (Integer) OptionStore.getOption("simulation.defaultMethod");
				if (selectedIndex != null) {
					int v = selectedIndex.intValue();
					simulationMethodsComboBox.setSelectedIndex(v);
				}
            }
            simulationMethodsComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateSimulationMethod();
                }
            });
        }
        return simulationMethodsComboBox;
    }

    protected void updateSimulationMethod() {
        if (currentParameter == null) {
            return;
        }
        currentParameter.strategy = SimulationStrategy.fromString((String) simulationMethodsComboBox.getSelectedItem());
        boolean depthControl = (currentParameter.strategy == SimulationStrategy.STG);
        radioBreadthFirst.setEnabled(depthControl);
        radioDephtFirst.setEnabled(depthControl);
        textMaxDepth.setEnabled(depthControl);
        textMaxNodes.setEnabled(depthControl);
        labelMaxDepth.setEnabled(depthControl);
        labelMaxNodes.setEnabled(depthControl);
        OptionStore.setOption("simulation.defaultMethod", 
        		(String) simulationMethodsComboBox.getSelectedItem());
    }


    private PrioritySelectionPanel getPriorityClassSelector() {
        if (selectPriorityClass == null) {
            selectPriorityClass = new PrioritySelectionPanel(stackDialog, paramList.pcmanager);
        }
        return selectPriorityClass;
    }

    /**
     * This method initializes textMaxDepth
     *
     * @return javax.swing.JTextField
     */
    private JTextField getTextMaxDepth() {
        if (textMaxDepth == null) {
            textMaxDepth = new JTextField();
            textMaxDepth.setMinimumSize(new Dimension(50, 20));
            textMaxDepth.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    updateMaxDepth();
                }
                public void focusGained(FocusEvent e) {
                }
            });
            textMaxDepth.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateMaxDepth();
                }
            });
        }
        return textMaxDepth;
    }

    /**
     * This method initializes textMaxNodes
     *
     * @return javax.swing.JTextField
     */
    private JTextField getTextMaxNodes() {
        if (textMaxNodes == null) {
            textMaxNodes = new JTextField();
            textMaxNodes.setMinimumSize(new Dimension(50, 20));
            textMaxNodes.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    updateMaxNodes();
                }
                public void focusGained(FocusEvent e) {
                }
            });
            textMaxNodes.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateMaxNodes();
                }
            });
        }
        return textMaxNodes;
    }

/* *************** LISTENERS **********************/

    /**
     * Update the <b>currentParameter.maxdepth</b> value when the corresponding widget is changed.
     */
    protected void updateMaxDepth() {
        setMessage("");
        if (refreshing || currentParameter == null) {
            return;
        }
        try {
            String s = textMaxDepth.getText().trim();
            if ("".equals(s)) {
                currentParameter.maxdepth = 0;
            } else {
                currentParameter.maxdepth = Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            setMessage("should be an integer");
        }
        textMaxDepth.setText(currentParameter.maxdepth > 0 ? ""+currentParameter.maxdepth : "");
    }


    /**
     * Update the <b>currentParameter.maxnodes</b> value when the corresponding widget is changed.
     */
    protected void updateMaxNodes() {
        setMessage("");
        if (refreshing || currentParameter == null) {
            return;
        }
        try {
            String s = textMaxNodes.getText().trim();
            if ("".equals(s)) {
                currentParameter.maxnodes = 0;
            } else {
                currentParameter.maxnodes = Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            setMessage("should be an integer");
        }
        textMaxNodes.setText(currentParameter.maxnodes > 0 ? ""+currentParameter.maxnodes : "");
    }

    private ChangeListener getRadioChangeListener() {
        if (radioChangeListener == null) {
            radioChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    setMessage("");
                    if (refreshing || currentParameter == null) {
                        return;
                    }
                    if (!(e.getSource() instanceof JRadioButton)) {
                        return;
                    }
                    if (!((JRadioButton)e.getSource()).isSelected()) {
                        return;
                    }
                    currentParameter.breadthFirst = radioBreadthFirst.isSelected();
                }
            };
        }
        return radioChangeListener;
    }

    private void refresh() {
        refreshing = true;
        if (currentParameter == null) {
            textMaxDepth.setEnabled(false);
            textMaxNodes.setEnabled(false);
            initStatePanel.setEnabled(false);
            setUserID(null);
        } else {
            textMaxDepth.setText(currentParameter.maxdepth > 0 ? ""+currentParameter.maxdepth : "");
            textMaxNodes.setText(currentParameter.maxnodes > 0 ? ""+currentParameter.maxnodes : "");
            textMaxDepth.setEnabled(true);
            textMaxNodes.setEnabled(true);
            setUserID(Reg2DynService.KEY+"::"+currentParameter.getName());
        }
        updateSimulationMethod();
        refreshing = false;
    }

    private void setMessage(String msg) {
        if (stackDialog != null) {
            stackDialog.setMessage(msg);
        }
    }

    private void setUserID(String uid) {
        if (stackDialog instanceof LogicalModelActionDialog) {
            ((LogicalModelActionDialog)stackDialog).setUserID(uid);
        }
    }

    @Override
    public void setParentList(SimulationParameterList list) {
        this.paramList = list;
        this.currentParameter = null;
        initialize();
        refresh();
    }

    @Override
    public void selectionUpdated(int[] sel) {
        if (sel.length != 1) {
            currentParameter = null;
        } else {
            currentParameter = paramList.get(sel[0]);
            initStatePanel.setParam(currentParameter);
            selectPriorityClass.setStore(currentParameter);
            if (currentParameter.breadthFirst) {
                radioBreadthFirst.setSelected(true);
            } else {
                radioDephtFirst.setSelected(true);
            }
        }
        refresh();

    }
}
