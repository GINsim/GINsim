package org.ginsim;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import org.ginsim.graph.AbstractGraphFrontend;
import org.ginsim.graph.Edge;
import org.ginsim.graph.Graph;
import org.ginsim.graph.GraphBackend;
import org.ginsim.graph.JgraphtBackendImpl;
import org.ginsim.graph.regulatoryGraph.RegulatoryEdge;
import org.ginsim.graph.regulatoryGraph.RegulatoryGraph;
import org.ginsim.graph.regulatoryGraph.RegulatoryGraphImpl;
import org.ginsim.graph.regulatoryGraph.RegulatoryVertex;
import org.ginsim.gui.graph.GraphGUI;
import org.ginsim.gui.graph.JgraphGUIImpl;
import org.ginsim.gui.graph.helper.GraphGUIHelper;
import org.ginsim.gui.graph.helper.GraphGUIHelperFactory;

import fr.univmrs.tagc.common.widgets.Frame;

/**
 * Simple, stupid launcher to test the ongoing refactoring
 * 
 * @author Aurelien Naldi
 */
public class TestRefactor {

	/**
	 * @param args
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		RegulatoryGraph lrg = new RegulatoryGraphImpl();

		RegulatoryVertex v1 = lrg.addVertex(0);
		RegulatoryVertex v2 = lrg.addVertex(0);
		RegulatoryVertex v3 = lrg.addVertex(0);

		lrg.addEdge(v1, v2, 0);
		lrg.addEdge(v1, v3, 0);
		
		GraphGUI<RegulatoryVertex, RegulatoryEdge> graphGUI = getGraphGUI(lrg);
		JFrame frame = new Frame("test", 800, 600) {
			@Override
			public void doClose() {
				System.exit(0);
			}
		};
		Container panel = frame.getContentPane();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 0;
		cst.weightx = 1;
		cst.weighty = 1;
		cst.fill = GridBagConstraints.BOTH;
		panel.add(graphGUI.getGraphComponent(), cst);
		frame.setVisible(true);
	}
	
	public static <V,E extends Edge<V>> GraphGUI<V,E> getGraphGUI(Graph<V,E> graph) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		// get a graph GUI helper
		GraphGUIHelper helper = GraphGUIHelperFactory.getFactory().getGraphGUIHelper(graph);
		
		// find the GUI component and show the graph...
		GraphGUI<V,E> graphGUI = null;
		if (graph instanceof AbstractGraphFrontend) {
			GraphBackend<V,E> graph_backend = ((AbstractGraphFrontend<V, E>)graph).getBackend();
			if (graph_backend instanceof JgraphtBackendImpl) {
				graphGUI = new JgraphGUIImpl<V,E>((JgraphtBackendImpl<V,E>) graph_backend, helper);
			}
		}

		return graphGUI;
	}
}
