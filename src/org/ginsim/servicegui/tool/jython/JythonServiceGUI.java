package org.ginsim.servicegui.tool.jython;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.ginsim.common.utils.IOUtils;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.servicegui.ServiceGUI;
import org.ginsim.servicegui.common.StandaloneGUI;
import org.ginsim.servicegui.common.ToolAction;
import org.mangosdk.spi.ProviderFor;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;




/**
 * main method for the reg2dyn plugin
 */
@ProviderFor(ServiceGUI.class)
@StandaloneGUI
public class JythonServiceGUI implements ServiceGUI {

	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new JythonAction(graph));
		return actions;
	}

}

class JythonAction extends ToolAction {

	private final Graph<?, ?> graph;

	public JythonAction(Graph<?, ?> graph) {
		super("STR_jython", "STR_jython_descr");
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
        new JythonConsole( graph);
	}
	
}

class JythonConsole extends Thread {
	
	static final String pypath;
	
	static {

		String consolebase = "/" + JythonConsole.class.getPackage().getName().replace('.', '/')+"/console";
		
		
        URL url = IOUtils.class.getResource(consolebase);
        
    	String path = url.getPath();
    	// if running from a jar, this looks like "file:path/to/file.jar!/fr/..../console"
    	if (path.startsWith("file:") && path.endsWith(".jar!"+consolebase)) {
    		path = path.substring(5, path.length() - consolebase.length()-1) + consolebase;
    	}
    	pypath = path;

        PySystemState.initialize();
	}
	
	Graph graph;
	JythonFrame frame;
	
    public JythonConsole( Graph graph) {
    	this.graph = graph;
    	this.frame = new JythonFrame();
    	this.start();
    }
    public void run() {
        PythonInterpreter pyi = new PythonInterpreter();   
        pyi.exec("import sys");    
        // you can pass the python.path to java to avoid hardcoding this
        // java -Dpython.path=/path/to/jythonconsole-0.0.6 EmbedExample
        
    	pyi.exec("sys.path.append(r'"+pypath + "')");
        pyi.exec("from console import Console");
        // stuff some objects into the namespace
        pyi.set("current_graph", graph);
        pyi.set("namespace", pyi.getLocals());
        pyi.set("frame", frame);
        pyi.set("sp", frame.consoleScrollPanel);

        pyi.exec("console = Console(namespace)");
        pyi.exec("sp.setViewportView(console.text_pane)");
        
        // namespace cleanups
        pyi.exec("del sp, frame, namespace, Console, sys, console, __doc__, __name__");
        frame.setVisible(true);
    }
}

class JythonFrame extends JFrame {
	private static final long serialVersionUID = -3652232265843777029L;
	JScrollPane consoleScrollPanel = new JScrollPane();
	
	public JythonFrame() {
		setTitle("GINsim: jython console");
		setSize(300, 400);
		getContentPane().add(consoleScrollPanel);
	}
	public void close() {
		setVisible(false);
		dispose();
	}
}