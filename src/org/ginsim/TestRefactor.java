package org.ginsim;

import org.ginsim.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.graph.regulatorygraph.RegulatoryVertex;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.shell.AboutDialog;

import fr.univmrs.tagc.common.managerresources.ImageLoader;
import fr.univmrs.tagc.common.managerresources.Translator;

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

		initGUI();
		RegulatoryGraph lrg = GUIManager.getInstance().newFrame();

		RegulatoryVertex v1 = lrg.addVertex();
		RegulatoryVertex v2 = lrg.addVertex();
		RegulatoryVertex v3 = lrg.addVertex();

		lrg.addEdge(v1, v2, 1);
		lrg.addEdge(v1, v3, -1);
	}

	/**
	 * Init method for GINsim GUI.
	 * This method will only load all required resources, it will not create the first window.
	 */
	private static void initGUI() {
		Translator.pushBundle("org.ginsim.gui.resources.messages");
		ImageLoader.pushSearchPath("/org/ginsim/gui/resources/icons");
		AboutDialog.setDOAPFile("/org/ginsim/gui/resources/GINsim-about.rdf");

		// detect the current directory
		// It will be needed for plugins, dynamic classpath...
		Class<?> cl = TestRefactor.class;
		String clname = cl.getName().replace(".",	"/") + ".class";
		String path = cl.getClassLoader().getResource(clname).toString();
		System.out.println();
	}

}
