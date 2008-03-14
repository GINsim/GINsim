package fr.univmrs.tagc.GINsim.connectivity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import fr.univmrs.tagc.GINsim.data.GsDirectedEdge;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.GINsim.gui.GsParameterPanel;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraphOptionPanel;
import fr.univmrs.tagc.GINsim.xml.GsGinmlHelper;
import fr.univmrs.tagc.common.GsException;
import fr.univmrs.tagc.common.manageressources.Translator;
import fr.univmrs.tagc.common.xml.XMLWriter;

/**
 * reduced Graph.
 */
public class GsReducedGraph extends GsGraph {

	private ReducedParameterPanel parameterPanel = null;
    private JPanel optionPanel = null;

    public static final String zip_mainEntry = "connectedComponent.ginml";
    
	/**
	 * @param parent
	 */
	public GsReducedGraph(GsGraph parent) {
	    this((String)null);
        setAssociatedGraph(parent);
	}
	
    protected String getGraphZipName() {
    	return zip_mainEntry;
    }

	/**
	 * @param map
	 * @param file
	 */
	public GsReducedGraph(Map map, File file) {
	    this(file.getAbsolutePath());
        GsReducedGraphParser parser = new GsReducedGraphParser();
        parser.parse(file, map, this);
        graphManager.ready();
	}

	/**
	 * @param filename
	 */
	public GsReducedGraph(String filename) {
        super(GsReducedGraphDescriptor.getInstance(), filename);
	}
	/**
     * 
     */
    public GsReducedGraph() {
        this((String)null);
    }

    /*
	 * @see fr.univmrs.tagc.GINsim.graph.GsGraph#doInteractiveAddVertex(int)
	 */
	protected Object doInteractiveAddVertex(int param) {
		return null;
	}

	/*
	 * @see fr.univmrs.tagc.GINsim.graph.GsGraph#doInteractiveAddEdge(java.lang.Object, java.lang.Object, int)
	 */
	protected Object doInteractiveAddEdge(Object source, Object target, int param) {
		return null;
	}

	/*
	 * @see fr.univmrs.tagc.GINsim.graph.GsGraph#doSave(java.lang.String, int, boolean)
	 */
	protected void doSave(OutputStreamWriter os, int mode, boolean selectedOnly) throws GsException {
        try {
            XMLWriter out = new XMLWriter(os, dtdFile);
	  		out.write("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
			out.write("\t<graph id=\"" + graphName + "\"");
			out.write(" class=\"reduced\">\n");
			saveNode(out, mode, selectedOnly);
			saveEdge(out, mode, selectedOnly);
            if (gsAnnotation != null) {
                gsAnnotation.toXML(out, null, 0);
            }
            // save the ref of the associated regulatory graph!
            if (associatedGraph != null) {
                associatedID = associatedGraph.getSaveFileName();
            }
            if (associatedID != null) {
                out.write("<link xlink:href=\""+associatedID+"\"/>\n");
            }
	  		out.write("\t</graph>\n");
	  		out.write("</gxl>\n");
        } catch (IOException e) {
            throw new GsException(GsException.GRAVITY_ERROR, Translator.getString("STR_unableToSave")+": " +e.getMessage());
        }
	}

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveEdge(XMLWriter out, int mode, boolean selectedOnly) throws IOException {
        Iterator it;
        if (selectedOnly) {
        		it = graphManager.getSelectedEdgeIterator();
        } else {
        		it = graphManager.getEdgeIterator();
        }
        switch (mode) {
        	default:
		        while (it.hasNext()) {
		        	Object o_edge = it.next();
		        	if (o_edge instanceof GsDirectedEdge) {
		        		GsDirectedEdge edge = (GsDirectedEdge)o_edge;
			            String source = edge.getSourceVertex().toString();
			            String target = edge.getTargetVertex().toString();
			            out.write("\t\t<edge id=\""+ source +"_"+target+"\" from=\""+source+"\" to=\""+target+"\"/>\n");
		        	}
		        }
		        break;
        }
    }

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveNode(XMLWriter out, int mode, boolean selectedOnly) throws IOException {
    	Iterator it;
    	if (selectedOnly) {
    		it = graphManager.getSelectedVertexIterator();
    	} else {
    		it = graphManager.getVertexIterator();
    	}
        	GsVertexAttributesReader vReader = null;
        	switch (mode) {
	    		case 1:
	    			vReader = graphManager.getVertexAttributesReader();
	                while (it.hasNext()) {
	                    Object vertex = it.next();
	                    String content = ((GsNodeReducedData)vertex).getContentString();
	                    out.write("\t\t<node id=\""+vertex+"\">\n");
                        out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
	                    out.write(GsGinmlHelper.getShortNodeVS(vReader));
	                    out.write("\t\t</node>\n");
	                }
	    			break;
				case 2:
					vReader = graphManager.getVertexAttributesReader();
	                while (it.hasNext()) {
	                    Object vertex = it.next();
	                    vReader.setVertex(vertex);
	                    String content = ((GsNodeReducedData)vertex).getContentString();
	                    out.write("\t\t<node id=\""+vertex+"\">\n");
                        out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
	                    out.write(GsGinmlHelper.getFullNodeVS(vReader));
	                    out.write("\t\t</node>\n");
	                }
	    			break;
        		default:
        	        while (it.hasNext()) {
        	            Object vertex = it.next();
	                    String content = ((GsNodeReducedData)vertex).getContentString();
	                    out.write("\t\t<node id=\""+vertex+"\">\n");
                        out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
                        out.write("</node>");
        	        }
        }
    }
	
	protected FileFilter doGetFileFilter() {
		return null;
	}

	public GsParameterPanel getEdgeAttributePanel() {
		return null;
	}

	public GsParameterPanel getVertexAttributePanel() {
		if (parameterPanel == null) {
			parameterPanel = new ReducedParameterPanel();
		}
		return parameterPanel;
	}

	public void changeVertexId(Object vertex, String newId) throws GsException {
	}

	public void removeEdge(Object obj) {
	}
	/**
	 * add a vertex to this graph.
	 * @param vertex the vertex to add.
	 */
	public void addVertex(GsNodeReducedData vertex) {
		graphManager.addVertex(vertex);
	}
	/**
	 * add an edge to this graph.
	 * @param source source vertex of this edge.
	 * @param target target vertex of this edge.
	 */
	public void addEdge(Object source, Object target) {
		graphManager.addEdge(source, target, null);
	}
	public List getSpecificLayout() {
		return GsReducedGraphDescriptor.getLayout();
	}
	public List getSpecificExport() {
		return GsReducedGraphDescriptor.getExport();
	}
	public List getSpecificAction() {
		return GsReducedGraphDescriptor.getAction();
	}
    public List getSpecificObjectManager() {
        return GsReducedGraphDescriptor.getObjectManager();
    }
    protected GsGraph getCopiedGraph() {
        return null;
    }
    protected List doMerge(GsGraph otherGraph) {
        return null;
    }
    protected GsGraph doCopySelection(Vector vertex, Vector edges) {
        // no copy for reduced graphs
        return null;
    }

    protected void setCopiedGraph(GsGraph graph) {
    }

    /**
     * @return a map referencing all real nodes in the selected CC
     */
    public Map getSelectedMap() {
        Map map = new HashMap();
        Iterator it = graphManager.getSelectedVertexIterator();
        while (it.hasNext()) {
            GsNodeReducedData node = (GsNodeReducedData) it.next();
            Vector content = node.getContent();
            for (int i=0 ; i<content.size() ; i++) {
                map.put(content.get(i).toString(), null);
            }
        }
        return map;
    }

	protected JPanel doGetFileChooserPanel() {
		return getOptionPanel();
	}
    
	private JPanel getOptionPanel() {
		if (optionPanel == null) {
            Object[] t_mode = { Translator.getString("STR_saveNone"), 
                    Translator.getString("STR_savePosition"), 
                    Translator.getString("STR_saveComplet") };
            optionPanel = new GsRegulatoryGraphOptionPanel(t_mode, mainFrame != null ? 2 : 0);
		}
		return optionPanel;
	}
    
    protected boolean isAssociationValid(GsGraph graph) {
        // blindly accept all associations
        return true;
    }
}