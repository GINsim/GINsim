package org.ginsim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.colomoto.biolqm.LQMLauncher;
import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.service.ExtensionLoader;
import org.colomoto.biolqm.service.LQMServiceManager;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.application.OptionStore;
import org.ginsim.common.callable.BasicProgressListener;
import org.ginsim.common.document.DocumentWriter;
import org.ginsim.common.document.LaTeXDocumentWriter;
import org.ginsim.common.document.OOoDocumentWriter;
import org.ginsim.common.document.XHTMLDocumentWriter;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.service.ServiceClassInfo;
import org.ginsim.core.graph.GraphFactory;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.objectassociation.GraphAssociatedObjectManager;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStateList;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesHandler;
import org.ginsim.core.graph.regulatorygraph.namedstates.NamedStatesManager;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.GSServiceManager;
import org.python.util.PythonInterpreter;

import javax.script.ScriptEngine;

/**
 * A helper when running GINsim in script mode.
 * It provides a proxy to common actions:
 * <ul>
 *   <li>open and save graphs</li>
 *   <li>run services</li>
 *   <li>create reports</li>
 * </ul>
 * 
 * @author Aurelien Naldi
 */
public class ScriptLauncher {

	private static boolean isInit = false;
	private boolean running = false;
	
	private final GSServiceManager services = new GSServiceManager();
	private final ObjectAssociationManager associated = ObjectAssociationManager.getInstance();
	
	/**
	 * Arguments passed to the script
	 */
	public String[] args;
	
	/** Access the LQM launcher as well */
    private LQMServiceManager lqm_launcher = null;
	
	/**
	 * Initialisation method for the script helper to ensure a working OptionStore.
	 */
	private static void initOptionStore() {
		if (isInit) {
			return;
		}
		isInit = true;

        // init the option store
		try {
			OptionStore.init( ScriptLauncher.class.getPackage().getName());
		} catch (Exception e) {
			LogManager.error("Could not init the Option Store");
		}
	}

	/**
	 * Run a script: the method is called by the main launcher to launch a GINsim script
	 * 
	 * @param filename script filename
	 * @param args additional arguments passed on the command line
	 */
	public synchronized void run(String filename, String[] args) {
		if (running) {
			throw new RuntimeException("Script should run only once");
		}
		running = true;
		this.args = args;

        if (filename.equals("doc")) {
            if (args == null || args.length == 0) {
                doc("script_doc.html", "apidocs");
            } else if (args.length == 2) {
                doc(args[0], args[1]);
            } else {
                System.out.println("arguments for the doc command: [<output file> <javadoc path>]");
            }
            return;
        }

        File f = new File(filename).getAbsoluteFile();
        if (!f.exists()) {
            LogManager.error( "No such script: "+filename);
        	return;
        }
		
        initOptionStore();


		// Explicit Jython support: faster than using Java's scripting API
		// and allows to extends the python classpath as well
		Class cl_PI = null;
		try {
			cl_PI = ExtensionLoader.getClassLoader().loadClass("org.python.util.PythonInterpreter");
			System.out.println("Jython is available!");
		} catch (Exception e) {
			System.out.println("Jython not found :(");
		}
        if (cl_PI != null && filename.endsWith(".py")) {
            // select a base directory for script files:
            // the first parent of the selected script without a __init__.py file
            File dir = f.getParentFile();
            while (true) {
                File finit = new File(dir, "__init__.py");
                if (!finit.exists()) {
                    break;
                }
                dir = dir.getParentFile();
            }

            // Create and set up the python interpreter:
            //  * add the selected folder to the classpath
            //  * add a "gs" object pointing to this script launcher
			PythonInterpreter pi = new PythonInterpreter();
            pi.getSystemState().path.add(0, dir.getAbsolutePath());
            pi.set("gs", this);
            pi.set("lm", LQM());
            pi.set("lqm", LQM());
            pi.execfile(filename);
            return;
        }

		// Generic scripting support through ScriptEngine
		// This method also supports python scripts, but is slower and allows less tweaks
		try {
			ScriptEngine engine = LQMLauncher.loadEngine(filename);
			engine.put("gs", this);
			engine.put("lm", LQM());
			engine.put("lqm", LQM());

			engine.eval(new java.io.FileReader(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}

        // reset the running status: should not be needed but may be convenient later
        running = false;
	}

	/**
	 * Grab a proxy to the bioLQM API
	 * 
	 * @return a LQM script launcher
	 */
	public LQMServiceManager LQM() {
		if (lqm_launcher == null) {
			lqm_launcher = new LQMServiceManager(args);
		}
		return lqm_launcher;
	}

	/**
	 * Open a graph from a file.
	 *
	 * @param filename the string filename
	 * @return the parsed graph
	 * @throws GsException the graph exception
	 */
	public Graph<?, ?> load(String filename) throws GsException {
		File file = new File(filename);
		return GSGraphManager.getInstance().open( file);
	}

	public RegulatoryGraph load(LogicalModel model) throws GsException {
		return LogicalModel2RegulatoryGraph.importModel(model);
	}

	/**
	 * Open
	 * @deprecated see {@link #load(String)}
	 * @param filename  the string filename
	 * @throws GsException the exception graph
	 */
	@Deprecated
	public Graph<?, ?> open(String filename) throws GsException {
		return load(filename);
	}

	/**
	 * Lookup a named state matching the given state.
	 * Marked as deprecated as it should be part of the initial state service.
	 * 
	 * @param state
	 * @param graph
	 * @return the name or null
	 */
	@Deprecated
	public String nameState(byte[] state, RegulatoryGraph graph) {
        // FIXME: adapt it to deal with input configs !!
        NamedStateList init = ((NamedStatesHandler)associated.getObject(graph, NamedStatesManager.KEY, false)).getInitialStates();
        if (init != null) {
            List<RegulatoryNode> no = graph.getNodeOrder();
        	return init.nameState(state, no);
        }
        return null;
	}

	/**
	 * Get a service instance
	 * 
	 * @param cl the service class
	 * @return the service or null if not found
	 */
	public Service service(Class<Service> cl) {
		return services.get(cl);
	}

	/**
	 * Get a service instance by name
	 * 
	 * @param name the service name (class name or alias)
	 * @return the service or null if not found
	 */
	public Service service(String name) {
		return services.get(name);
	}

	/**
	 * Get (existing) associated data
	 * 
	 * @param g
	 * @param key
	 * @return the associated object or null if it was not created.
	 */
	public Object associated(Graph g, String key) {
		return associated(g, key, false);
	}

	/**
	 * Get or create associated data.
	 * 
	 * @param g the graph
	 * @param key the key string
	 * @param create boolean if creation
	 * @return the associated object, created if needed or null if the key is invalid
	 */
	public Object associated(Graph<?,?> g, String key, boolean create) {
		return associated.getObject(g, key, create);
	}

	/**
	 * Create a report file, this creates the file and DocumentWriter, don't forget to close it.
	 * 
	 * @param path the string path values
	 * @param properties map string of properties
	 * @return a new DocumentWriter
	 */
	public DocumentWriter createReport(String path, Map<String, String> properties) {
		DocumentWriter dw;
		if (path.endsWith("html")) {
			dw = XHTMLDocumentWriter.FACTORY.getDocumentWriter();
		} else if (path.endsWith(".odt")) {
			dw = OOoDocumentWriter.FACTORY.getDocumentWriter();
		} else if (path.endsWith("tex")) {
			dw = LaTeXDocumentWriter.FACTORY.getDocumentWriter();
		} else {
			return null;
		}
		
		try {
			dw.setOutput(new File(path));
			if (properties != null) {
				dw.setDocumentProperties(properties);
			}
			dw.startDocument();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dw = null;
		}
		return dw;
	}
	
	/**
	 * Show some help about services and data available for scripts
	 */
	public void help() {
		System.out.println("Available services:");
		for (Class<Service> srv: GSServiceManager.getAvailableServices()) {
			System.out.print("   * ");
			Alias alias = srv.getAnnotation(Alias.class);
			if (alias != null) {
				System.out.print(alias.value() + " -- ");
			}
			System.out.println(srv.getName());
		}
		System.out.println();

		List<GraphAssociatedObjectManager> managers = associated.getObjectManagerList();
		if (managers != null && managers.size() > 0) {
			System.out.println("Available data managers:");
			for (GraphAssociatedObjectManager amanager: managers) {
				System.out.print("   * ");
				Alias alias = amanager.getClass().getAnnotation(Alias.class);
				if (alias != null) {
					System.out.print(alias.value() + " -- ");
				}
				System.out.println(amanager.getObjectName());
			}
		}
		System.out.println("Available specialised data managers:");
		for (Entry<Class, List<GraphAssociatedObjectManager>> entry: associated.getManagedClasses()) {
			System.out.println("   * " + entry.getKey());
			for (GraphAssociatedObjectManager amanager: entry.getValue()) {
				System.out.print("     - ");
				System.out.println(amanager.getObjectName());
			}
		}
		System.out.println();
	}

	/**
	 * Write some ad-hoc documentation.
	 * It creates a simple HTML version of the help, with links to the javadoc.
	 * 
	 * @param filename output filename
	 * @param javadocbase path to the javadoc
	 */
	private void doc(String filename, String javadocbase) {
        System.out.println("Generating script documentation in "+filename+".");
		DocumentWriter doc = createReport(filename, null);
		try {
            doc.writeText("In a script, the ");
			doc.addLink(getJavadocLink(javadocbase, getClass()), "Script Manager");
            doc.writeText("is used as proxy to the ");
            doc.addLink(javadocbase+"/index.html", "GINsim API");
            doc.writeText(". It allows to load graphs, access their associated data, and services.");
            doc.writeText("Each graph type and service has its own API, linked below.");

            writeDoc(doc, javadocbase, "Graph types", GSGraphManager.getInstance().getGraphsInfo());
			writeDoc(doc, javadocbase, "Services", GSServiceManager.getServicesInfo());

			// TODO: names for associated data managers
            ObjectAssociationManager objMgr = ObjectAssociationManager.getInstance();
            writeDoc(doc, javadocbase, "Associated data", objMgr.getDataManagerInfo(null));

            for (GraphFactory factory: GSGraphManager.getInstance().getGraphFactories()) {
                writeDoc(doc, javadocbase, "Associated data for "+factory.getGraphType(), objMgr.getDataManagerInfo(factory.getGraphClass()));
            }
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<GraphAssociatedObjectManager> getDataManagers() {
		List datamanagers = new ArrayList();
		List<GraphAssociatedObjectManager> managers = associated.getObjectManagerList();
		if (managers != null) {
			datamanagers.addAll(managers);
		}
		
		Collection<Entry<Class, List<GraphAssociatedObjectManager>>> managed = associated.getManagedClasses();
		if (managed != null) {
			for (Entry<Class, List<GraphAssociatedObjectManager>> e: managed) {
				datamanagers.addAll(e.getValue());
			}
		}
		return datamanagers;
	}
	
	private void writeDoc(DocumentWriter doc, String javadocbase, String title, ServiceClassInfo[] objects) throws IOException {
		
		if (objects == null || objects.length == 0) {
			return;
		}
		
		doc.openHeader(1, title, null);
        doc.openTable("", "", new String[]{"", "", ""});
        doc.openTableRow();
        doc.openTableCell("Alias", true);
        doc.openTableCell("Package", true);
        doc.openTableCell("Class", true);
		for (ServiceClassInfo info: objects) {
            doc.openTableRow();
            doc.openTableCell(info.alias);

            // javadoc links
            String pkg = info.cl.getPackage().getName();
            String cl = info.cl.getSimpleName();

            doc.openTableCell("");
            doc.addLink(getJavadocLink(javadocbase, pkg, null), pkg);

            doc.openTableCell("");
            doc.addLink(getJavadocLink(javadocbase, pkg, cl), cl);

		}
		doc.closeTable();
	}

    private String getJavadocLink(String javadocbase, Class cl) {
        return getJavadocLink(javadocbase, cl.getPackage().getName(), cl.getSimpleName());
    }

    private String getJavadocLink(String javadocbase, String pkg, String className) {
		String scl = pkg.replace('.', '/');
        if (className != null) {
            scl += "/"+className+".html";
        } else {
            scl += "/package-summary.html";
        }
		return javadocbase+"/?"+scl;
	}

	/**
	 * Instancite a new BasicProgressListener
	 * @return a BasicProgressListener
	 */
	public BasicProgressListener progressListener() {
		return new BasicProgressListener();
	}
	
}
