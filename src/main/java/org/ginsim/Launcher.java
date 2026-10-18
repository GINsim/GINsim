package org.ginsim;

import org.colomoto.biolqm.LQMLauncher;
import org.colomoto.biolqm.service.ExtensionLoader;
import org.ginsim.common.application.*;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.commongui.utils.ImageLoader;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.GraphFactory;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.service.GSServiceManager;
import org.ginsim.core.service.ServiceClassInfo;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.GSServiceGUIManager;
import org.ginsim.gui.shell.AboutDialog;
import py4j.GatewayServer;

import java.awt.Desktop;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple launcher: parse command line arguments, show some help, open files or run the script mode accordingly.
 * 
 * @author Aurelien Naldi
 */
public class Launcher {

	/**
	 * True if the developers extras (toolkit menu, under-developement...) should be enabled
	 */
	public static boolean developer_mode = false;
	
	/**
	 * The main "main" function, launching GINsin GUI or script handler.
	 * 
	 * @param args
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<String> open = new ArrayList<String>();

        /*
         * parse arguments:
         *  - open files
         *  - script mode
         *  - help
         */
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-lm") || args[i].equals("-lqm")) {
                String[] lmargs = new String[args.length-(i+1)];
                System.arraycopy(args, i+1, lmargs, 0, lmargs.length);
                LQMLauncher.main(lmargs);
                return;
            } else if (args[i].equals("-s")) {
        		ScriptLauncher script = new ScriptLauncher();
                if (args.length == i+1) {
                	script.help();
                    return;
                }
                
                i++;
                String[] pyargs = new String[args.length-(i+1)];
                System.arraycopy(args, i+1, pyargs, 0, pyargs.length);
                script.run(args[i], pyargs);
                
                // force exit as jython seems to often delay it
                System.exit(0);
                return;
            } else if (args[i].equals("-py")) {
        		ScriptLauncher script = new ScriptLauncher();
            	GatewayServer pygw = new GatewayServer(script, 0);
            	pygw.start();
            	System.out.println(pygw.getListeningPort());
            	System.out.println("Started the Py4J gateway (<ctrl>-c to interrupt)");
            	return;
            } else if (args[i].equals("-n")) {
        		open.add(null);
        		continue;
            } else if (args[i].equals("--dev")) {
                developer_mode = true;
                continue;
            } else if (args[i].equals("--devinfo")) {
                developer_mode = true;
                devInfo();
                return;
            } else if (args[i].startsWith("-")) {
            	if (!args[i].equals("-h")) {
                    System.out.println("Unknown option: "+args[i]);
            	}
                System.out.println("Available options:");
                System.out.println("\t<file>: open <file> on startup.");
                System.out.println("\t-n: start with a new regulatory graph.");
                System.out.println("\t-s: display the script help message.");
                System.out.println("\t-s <file>: run \"script\" from <file>. Extra arguments are script arguments.");
                System.out.println("\t-py: launch a server for the py4j python gateway.");
                System.out.println("\t-h:  display this message.");
                System.out.println("\t--dev : enable the developer's options.");
                System.out.println("\t-lm: bioLQM mode: takes the same arguments as the bioLQM conversion tool.");
                return;
            }
        	
            File f = new File(args[i]);
            if (f.exists()) {
                open.add(args[i]);
            } else {
            	LogManager.error("Required file does not exist: " + f);
            }
        }
		
		initGUI();
		GUIManager.getInstance().startup( open);
	}

	/*
	 * Initialise GINsim core: detect the current directory, load services
	 */
	static {
        ExtensionLoader.loadExtensions("extensions", Launcher.class);
		String basedir = null;
		
		Class<?> cl = Launcher.class;
		String clname = cl.getName().replace(".",	"/") + ".class";
		String path = cl.getClassLoader().getResource(clname).toString();
		if (path.startsWith("file:")) {
			basedir = path.substring(5,  path.length() - clname.length());
		} else if (path.startsWith("jar:file:")) {
			File jar = new File(path.substring(9,  path.length() - clname.length() - 2));
			basedir = jar.getParent();
        }

        if (basedir != null) {
            try {
                basedir = URLDecoder.decode(basedir, "UTF-8");
            } catch (Exception e) {
                basedir = null;
            }
        }

        if (basedir == null) {
            basedir = System.getProperty("user.dir");
        }

        // make sure that services are loaded at init time
        GSServiceManager.getAvailableServices();
	}
	
	/**
	 * Initialisation method for GINsim GUI.
	 * This method will only load all required resources, it will not create the first window.
	 */
	private static void initGUI() {
		Txt.push("org.ginsim.messages");
		ImageLoader.pushSearchPath("/org/ginsim/icon");
		ImageLoader.pushSearchPath("/org/ginsim/icon/action");
		AboutDialog.setDOAPFile("/GINsim-about.rdf");
		try {
			OptionStore.init(Launcher.class.getPackage().getName());
		} catch (Exception e) {
			GUIMessageUtils.openErrorDialog(e, null);
		}
		
    	// register OSX callback if appropriate
        if (CurrentOS.CURRENT_OS == CurrentOS.MACOSX && Desktop.isDesktopSupported()) {
            try {
                GUIManager guiManager = GUIManager.getInstance();
                Desktop desktop = Desktop.getDesktop();
                desktop.setAboutHandler(e -> guiManager.about());
                // quit() exits by itself once every window is closed, so always cancel the OS-driven quit
                desktop.setQuitHandler((e, response) -> {
                    guiManager.quit();
                    response.cancelQuit();
                });
                desktop.setOpenFileHandler(e -> {
                    for (File f : e.getFiles()) {
                        guiManager.loadGINMLfile(f.getAbsolutePath());
                    }
                });
            } catch (Exception e) {
                System.err.println("Error while registering the macOS desktop handlers:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Print information for developers: list of loaded services
     */
    private static void devInfo() {

        // load everything
        GSGraphManager graphManager = GSGraphManager.getInstance();
        ObjectAssociationManager assocManager = ObjectAssociationManager.getInstance();

        System.out.println("Graphs");
        for (ServiceClassInfo info: graphManager.getGraphsInfo()) {
            System.out.println(info);
        }
        System.out.println();

        System.out.println("Data handlers");
        for (GraphFactory factory: graphManager.getGraphFactories()) {
            Class cl = factory.getGraphClass();
            System.out.println("# "+cl.getSimpleName());
            for (ServiceClassInfo info: assocManager.getDataManagerInfo(cl)) {
                System.out.println(info);
            }
            System.out.println();
        }

        for (ServiceClassInfo info: assocManager.getDataManagerInfo(null)) {
            System.out.println(info);
        }
        System.out.println();

        System.out.println("Services");
        for (ServiceClassInfo info: GSServiceManager.getServicesInfo()) {
            System.out.println(info);
        }
        System.out.println();

        System.out.println("GUI for services");
        for (ServiceClassInfo info: GSServiceGUIManager.getServicesInfo()) {
            System.out.println(info);
        }
    }
 }
