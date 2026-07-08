package org.ginsim.core.graph.regulatorygraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.metadata.Annotator;
import org.ginsim.common.application.GsException;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.BooleanParser;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeElement;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeParam;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.io.parser.GinmlHelper;
import org.ginsim.core.io.parser.GsXMLHelper;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.notification.resolvable.InvalidFunctionResolution;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Load a regulatory graph from GINML.
 *
 * @author Aurelien Naldi.
 */
public final class RegulatoryParser extends GsXMLHelper {

    private static final int POS_OUTSIDE = -1;  // outside of the graph (or in an ignored one)
	private static final int POS_OUT = 0;       // in the graph, outside of all vertices/edges
    private static final int POS_FILTERED = 50;
    private static final int POS_GRAPH_NOTES = 1;
    private static final int POS_GRAPH_NOTES_LINKLIST = 2;
    private static final int POS_VERTEX = 10;
    private static final int POS_VERTEX_VS = 11;
    private static final int POS_VERTEX_NOTES = 12;
    private static final int POS_VERTEX_NOTES_LINKLIST = 13;
    private static final int POS_EDGE = 20;
    private static final int POS_EDGE_VS = 21;
    private static final int POS_EDGE_NOTES = 22;
    private static final int POS_EDGE_NOTES_LINKLIST = 23;

    private int pos = POS_OUTSIDE;
    private RegulatoryGraph graph;

    private RegulatoryNode vertex = null;
    private NodeAttributesReader vareader = null;
    private EdgeAttributesReader ereader = null;
    private StyleManager<RegulatoryNode, RegulatoryMultiEdge> styleManager;
    private RegulatoryEdge edge = null;
    
    private Annotator<NodeInfo> annotator = null;
    
	private Map<String, RegulatoryEdge> m_edges = new HashMap();
	private Map<String, String> m_validIDs = new HashMap();
    private List v_waitingInteractions = new ArrayList();
    private String s_nodeOrder;
    private Set set;

    private Map<RegulatoryNode, Map> values;
    private List v_function;

    /** some more stuff to check consistency of "old" models (with explicit and free maxvalue) */
    Map<RegulatoryEdge, Integer> m_checkMaxValue;

    /**
     */
    public RegulatoryParser() {
    }

    /**
     * @param set  the set
     * @param attributes  the attribute
     * @param s_dtd  string
     * @throws GsException
     */
    public RegulatoryParser(Set set, Attributes attributes, String s_dtd) throws GsException {
    	
        graph = GSGraphManager.getInstance().getNewGraph( RegulatoryGraph.class);
        this.set = set;
		s_nodeOrder = attributes.getValue("nodeorder");
        if (s_nodeOrder == null) {
            throw new GsException( GsException.GRAVITY_ERROR, "missing nodeOrder");
        }
		try {
			graph.setGraphName(attributes.getValue("id"));
		} catch (GsException e) {
			throw new GsException(GsException.GRAVITY_ERROR, "invalidGraphName");
		}

		styleManager = graph.getStyleManager();
		vareader = graph.getNodeAttributeReader();
		ereader = graph.getEdgeAttributeReader();
		annotator = this.graph.getAnnotator();
        pos = POS_OUT;
        values = new Hashtable();
    }

    /**
     * create a new regulatory graph from a file.
     *
     * @param file the file to read.
     * @param set
     * @param graph the graph to fill with this data.
     */
    public void parse(File file, Set set, Graph<?,?> graph)  throws GsException{
    	this.graph = (RegulatoryGraph) graph;
    	this.set = set;
    	styleManager = this.graph.getStyleManager();
    	vareader = graph.getNodeAttributeReader();
    	ereader = graph.getEdgeAttributeReader();
		annotator = this.graph.getAnnotator();
    	startParsing(file);
   	}

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        switch (pos) {
			case POS_FILTERED:
			    if (qName.equals("node") || qName.equals("edge")) {
			        pos = POS_OUT;
			    }
			    break;
			case POS_VERTEX:
			    if (qName.equals("node")) {
			        pos = POS_OUT;
			    }
			    break; // POS_VERTEX
			case POS_GRAPH_NOTES:
			    if (qName.equals("annotation")) {
			    		pos = POS_OUT;
			    } else if (qName.equals("comment")) {
			        annotator.setNotes(curval);
			        curval = null;
			    }
			    break; // POS_GRAPH_NOTES
			case POS_GRAPH_NOTES_LINKLIST:
			    if (qName.equals("linklist")) {
			        pos = POS_GRAPH_NOTES;
			    }
			    break; // POS_GRAPH_NOTES_LINKLIST
			case POS_VERTEX_VS:
			    if (qName.equals("nodevisualsetting")) {
			        pos = POS_VERTEX;
			    }
			    break; // POS_VERTEX_VS
			case POS_EDGE:
			    if (qName.equals("edge")) {
			        pos = POS_OUT;
			    }
			    break; // POS_EDGE
			case POS_EDGE_VS:
			    if (qName.equals("edgevisualsetting")) {
			        pos = POS_EDGE;
			    }
			    break; // POS_EDGE_VS
			case POS_VERTEX_NOTES:
			    if (qName.equals("annotation")) {
			        pos = POS_VERTEX;
			    } else if (qName.equals("comment")) {
			        annotator.setNotes(curval);
			        curval = null;
			    }
			    break; // POS_VERTEX_NOTES
			case POS_EDGE_NOTES:
			    if (qName.equals("annotation")) {
			        pos = POS_EDGE;
			    } else if (qName.equals("comment")) {
		    		annotator.setNotes(curval);
			        curval = null;
			    }
			    break; // POS_EDGE_NOTES
			case POS_VERTEX_NOTES_LINKLIST:
			    if (qName.equals("linklist")) {
			        pos = POS_VERTEX_NOTES;
			    }
			    break; // POS_VERTEX_NOTES_LINKLIST
			case POS_EDGE_NOTES_LINKLIST:
			    if (qName.equals("linklist")) {
			        pos = POS_EDGE_NOTES;
			    }
			    break; // POS_EDGE_NOTES_LINKLIST
			case POS_OUT:
				if (qName.equals("graph")) {
					placeInteractions();
					placeNodeOrder();
                    if (!values.isEmpty()) {
                    	parseBooleanFunctions();
                    }
					for (RegulatoryNode vertex : graph.getNodeOrder()) {
						vertex.getV_logicalParameters().cleanupDup();
					}
                    pos = POS_OUTSIDE;
				}
				break;
        }
        super.endElement(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        super.startElement(uri, localName, qName, attributes);

        switch(pos) {
            case POS_OUTSIDE:
                if (qName.equals("graph")) {
                    if (!"regulatory".equals(attributes.getValue("class"))) {
                        throw new SAXException( new GsException( GsException.GRAVITY_ERROR, "STR_LRG_NotRegulatoryGraph"));
                    }
                    s_nodeOrder = attributes.getValue("nodeorder");
                    try {
                        graph.setGraphName(attributes.getValue("id"));
                    } catch (GsException e) {
                        throw new SAXException( new GsException( "STR_InvalidGraphName", e));
                    }
                }
                pos = POS_OUT;
                break;
        	case POS_OUT:
				switch (qName) {
					case "nodestyle":
					case "edgestyle":
						styleManager.parseStyle(qName, attributes);
						break;
					case "node":
						String id = attributes.getValue("id");
						if (set == null || set.contains(id)) {
							String cleanId = XMLWriter.deriveValidId(id);
							if (!cleanId.equals(id)) {
								m_validIDs.put(id, cleanId);
							}
							pos = POS_VERTEX;
							try {
								byte maxvalue = (byte) Integer.parseInt(attributes.getValue("maxvalue"));
								String name = attributes.getValue("name");
								vertex = graph.addNewNode(cleanId, name, maxvalue);
								vertex.getV_logicalParameters().setUpdateDup(false);
								String s_basal = attributes.getValue("basevalue");
								if (s_basal != null) {
									byte basevalue = (byte) Integer.parseInt(s_basal);
									if (basevalue != 0) {
										vertex.addLogicalParameter(new LogicalParameter(basevalue), true);
									}
								}
								String input = attributes.getValue("input");
								if (input != null) {
									vertex.setInput(input.equalsIgnoreCase("true") || input.equals("1"), graph);
								}
								values.put(vertex, new Hashtable());
							} catch (NumberFormatException e) {
								throw new SAXException(new GsException("STR_LRG_MalformedNodeParameters", e));
							}
						} else {
							pos = POS_FILTERED;
						}
						break;
					case "edge":
						String from = attributes.getValue("from");
						if (m_validIDs.containsKey(from)) {
							from = m_validIDs.get(from);
						}
						String to = attributes.getValue("to");
						if (m_validIDs.containsKey(to)) {
							to = m_validIDs.get(to);
						}
						if (set == null || set.contains(from) && set.contains(to)) {
							pos = POS_EDGE;
							try {
								id = attributes.getValue("id");
								String effects = attributes.getValue("effects");
								if (effects != null) {
									String[] teffects = effects.split(" ");
									for (String s : teffects) {
										String[] t = s.split(":");
										byte minvalue = (byte) Integer.parseInt(t[0]);
										edge = graph.addNewEdge(from, to, minvalue, t[1]);
										m_edges.put(id + ":" + minvalue, edge);
									}
								} else {
									byte minvalue = (byte) Integer.parseInt(getAttributeValueWithDefault(attributes, "minvalue", "1"));
									String smax = getAttributeValueWithDefault(attributes, "maxvalue", "-1");
									byte maxvalue = -2;
									String sign = attributes.getValue("sign");
									edge = graph.addNewEdge(from, to, minvalue, sign);
									if (smax.startsWith("m")) {
										maxvalue = -1;
									} else {
										maxvalue = (byte) Integer.parseInt(smax);
									}
									storeMaxValueForCheck(edge, maxvalue);
									m_edges.put(id, edge);
									edge.me.rescanSign(graph);
									ereader.setEdge(edge.me);
								}
							} catch (Exception e) {
								throw new SAXException(new GsException("STR_LRG_MalformedInteractionParameters", e));
							}
						} else {
							pos = POS_FILTERED;
						}
						break;
					case "annotation":
						pos = POS_GRAPH_NOTES;
						annotator.onModel();
						break;
					case "attr":
						String name = attributes.getValue("name");
						String value = attributes.getValue("value");
						if (name != null && value != null) {
							graph.setAttribute(name, value);
						}
						break;
				}
                break; // POS_OUT
            case POS_GRAPH_NOTES:
                if (qName.equals("linklist")) {
                    pos = POS_GRAPH_NOTES_LINKLIST;
			    } else if (qName.equals("comment")) {
			        curval = "";
			    }
            	break; // POS_GRAPH_NOTES
            case POS_GRAPH_NOTES_LINKLIST:
                if (qName.equals("link")) {
                    try {
						String uriString = attributes.getValue("xlink:href");
						annotator.annotate( uriString);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                break; // POS_GRAPH_NOTES_LINKLIST
                
            case POS_VERTEX:
                if (vareader != null && qName.equals("nodevisualsetting")) {
            		vareader.setNode(vertex);
                	if (GinmlHelper.loadNodeStyle(styleManager, vareader, attributes)) {
                		pos = POS_VERTEX_VS;
            		}
                } else if (qName.equals("annotation")) {
                    pos = POS_VERTEX_NOTES;
					annotator.node(vertex.getNodeInfo());
                } else if (qName.equals("parameter")) {
                		v_waitingInteractions.add(vertex);
                		v_waitingInteractions.add(attributes.getValue("val"));
                		v_waitingInteractions.add(attributes.getValue("idActiveInteractions"));
                } else if (qName.equals("value")) {
                	v_function = new ArrayList();
                	((Hashtable)values.get(vertex)).put(attributes.getValue("val"), v_function);
                } else if (qName.equals("exp")) {
                	v_function.add(attributes.getValue("str"));
                }
                break; // POS_VERTEX

            case POS_EDGE:
                if (qName.equals("edgevisualsetting")) {
                	ereader.setEdge(edge.me);
                	if (GinmlHelper.loadEdgeStyle(styleManager, ereader, attributes)) {
                		pos = POS_EDGE_VS;
                	}
                } else if (qName.equals("annotation")) {
                    pos = POS_EDGE_NOTES;
                    try {
                    	RegulatoryNode node1 = edge.me.getSource();
                    	RegulatoryNode node2 = edge.me.getTarget();
						annotator.edge(node1.getNodeInfo(), node2.getNodeInfo());
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
                break; // POS_EDGE

            case POS_EDGE_VS:
            	GinmlHelper.applyEdgeVisualSettings(edge.me, styleManager, ereader, vareader, qName, attributes);
                break; // POS_EDGE_VS
            case POS_VERTEX_VS:
            	GinmlHelper.applyNodeVisualSettings(vareader, styleManager, qName, attributes);
                break; // POS_VERTEX_VS
            case POS_VERTEX_NOTES:
                if (qName.equals("linklist")) {
                    pos = POS_VERTEX_NOTES_LINKLIST;
			    } else if (qName.equals("comment")) {
			        curval = "";
                }
                break; // POS_VERTEX_NOTES
            case POS_EDGE_NOTES:
                if (qName.equals("linklist")) {
                    pos = POS_EDGE_NOTES_LINKLIST;
			    } else if (qName.equals("comment")) {
			        curval = "";
                }
                break; // POS_EDGE_NOTES
            case POS_VERTEX_NOTES_LINKLIST:
			case POS_EDGE_NOTES_LINKLIST:
                if (qName.equals("link")) {
                    String uriString = attributes.getValue("xlink:href");
					annotator.annotate(uriString);
                }
                break; // POS_VERTEX_NOTES || POS_EDGE_NOTES
        }
    }

    private void storeMaxValueForCheck(RegulatoryEdge key, byte maxvalue) {
    	if (m_checkMaxValue == null) {
    		m_checkMaxValue = new HashMap();
    	}
    	m_checkMaxValue.put(key, (int) maxvalue);
	}

	/**
     * use the constructed v_waitingInteraction to add the accurate interaction to the nodes.
     */
    private void placeInteractions() throws SAXException{
    	// check the maxvalues of all interactions first
    	if (m_checkMaxValue != null) {
        	Map<Entry<RegulatoryEdge,Integer>,String> m = null;
			for (Entry<RegulatoryEdge, Integer> entry : m_checkMaxValue.entrySet()) {
				byte m1 = entry.getKey().getMax();
				byte m2 = entry.getValue().byteValue();
				byte max = entry.getKey().me.getSource().getMaxValue();
				if (m1 != m2) {
					if (m == null) {
						m = new HashMap<Entry<RegulatoryEdge, Integer>, String>();
					}
					if (m1 == -1 && m2 == max || m2 == -1 && m1 == max) {
						m.put(entry, "");
					} else {
						m.put(entry, null);
					}
				}
			}
    		if (m != null) {
    			
    			LogManager.error( "Interaction inconsistency detected.");
    			for( Entry<RegulatoryEdge,Integer> entry : m.keySet()){
    				LogManager.error( " Edge = " + entry.getKey().getShortDetail());
    				LogManager.error( "   Passed max value = " + entry.getValue());
    			}
    			// TODO: should we report something to the user here?
    			// throw new SAXException( new GsException( GsException.GRAVITY_ERROR, "Interaction inconsistency detected"));
    		}
    	}

    	for (int i=0 ; i<v_waitingInteractions.size() ; i+=3) {
    		RegulatoryNode vertex = (RegulatoryNode)v_waitingInteractions.get(i);
    		LogicalParameter gsi = new LogicalParameter(Integer.parseInt( (String)v_waitingInteractions.get(i+1)));
    		String s_interactions = (String) v_waitingInteractions.get(i+2);
    		if (s_interactions != null) {
	    		String[] t_interactions = s_interactions.split(" ");

				for (String t_interaction : t_interactions) {
					String s_interaction = t_interaction.trim();
					if (s_interaction.length() == 0) {
						continue;
					}
					RegulatoryEdge e = m_edges.get(s_interaction);

					if (e == null) {
						int idx = s_interaction.lastIndexOf(':');
						if (idx > 0) {
							e = m_edges.get(s_interaction.substring(0, idx));
						}
					}
					if (e == null) {
						int idx = s_interaction.lastIndexOf('#');
						if (idx > 0) {
							e = m_edges.get(s_interaction.substring(0, idx));
						}
					}

					if (e != null) {
						gsi.addEdge(e);
					} else {
						LogManager.error("Failed to find a matching interaction for " + t_interaction);
					}
				}
    		}
    		vertex.addLogicalParameter(gsi, true);
    	}
    }

    /**
     * install the correct nodeOrder in the graph: it should match the saved one.
     */
    private void placeNodeOrder() {
    		List v_order = new ArrayList();
    		String[] t_order = s_nodeOrder.split(" ");
    		boolean ok = true;
    		if (set == null) {
				for (String s : t_order) {
					String sid = s;
					if (m_validIDs.containsKey(sid)) {
						sid = m_validIDs.get(sid);
					}
					RegulatoryNode vertex = graph.getNodeByName(sid);
					if (vertex == null) {
						ok = false;
						break;
					}
					v_order.add(vertex);
				}
    		} else {
				for (String s : t_order) {
					if (set.contains(s)) {
						RegulatoryNode vertex = graph.getNodeByName(s);
						if (vertex == null) {
							ok = false;
							break;
						}
						v_order.add(vertex);
					}
				}
    		}
    		if (!ok || v_order.size() != graph.getNodeCount()) {
    			// error
                LogManager.debug("incoherent nodeOrder, not restoring it");
    		} else {
    			graph.setNodeOrder(v_order);
    		}
    }

    private void parseBooleanFunctions() {
      Collection<RegulatoryMultiEdge> allowedEdges;
      String value, exp;
      try {
        for (RegulatoryNode vertex: values.keySet()) {
          allowedEdges = graph.getIncomingEdges(vertex);
          if (allowedEdges.size() > 0) {
            for (Enumeration enu_values = ((Hashtable)values.get(vertex)).keys(); enu_values.hasMoreElements(); ) {
              value = (String)enu_values.nextElement();
			  for (Object o : (List) values.get(vertex).get(value)) {
				  exp = (String) o;
				  addExpression(Byte.parseByte(value), vertex, exp);
			  }
            }
            vertex.getInteractionsModel().parseFunctions();
            if (vertex.getMaxValue() + 1 == values.get(vertex).size()) {
              ((TreeElement)vertex.getInteractionsModel().getRoot()).setProperty("add", false);
            }
          }
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    public void addExpression(byte val, RegulatoryNode vertex, String exp) {
    	try {
        BooleanParser tbp = new BooleanParser( graph.getIncomingEdges(vertex));
        TreeInteractionsModel interactionList = vertex.getInteractionsModel();
        // TODO: cleanup formulas after renamed nodes
		for (String sid: m_validIDs.keySet()) {
			String cleanID = m_validIDs.get(sid);
			exp = exp.replaceAll(sid, cleanID);
		}
        if (!tbp.compile(exp, graph, vertex)) {
        	Object[] data = new Object[3];
        	data[0] = val;
        	data[1] = vertex;
        	data[2] = exp;
        	
        	NotificationManager.publishResolvableWarning( this, "Invalid formula in node "+vertex+": " + exp, graph, data, new InvalidFunctionResolution());
        }
        else {
          interactionList.addExpression(val, vertex, tbp);
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    public void addParam(byte val, RegulatoryNode vertex, String par) throws Exception {
      TreeInteractionsModel interactionList = vertex.getInteractionsModel();
//      Set<GsDirectedEdge> l = interactionList.getGraph().getGraphManager().getIncomingEdges(vertex);
      TreeParam param = interactionList.addEmptyParameter(val, vertex);
      String[] t_interaction = par.split(" ");
      List v = new ArrayList();
      String srcString, indexString;
	  for (String s : t_interaction) {
			if (s.lastIndexOf("_") != -1) {
				srcString = s.substring(0, s.lastIndexOf("_"));
				indexString = s.substring(s.lastIndexOf("_") + 1);
			} else {
				srcString = s;
				indexString = "1";
			}
			Collection<RegulatoryMultiEdge> edges;
			edges = interactionList.getGraph().getIncomingEdges(vertex);
			for (RegulatoryMultiEdge e : edges) {
				if (e.getSource().getId().equals(srcString)) {
					// FIXME: edge definition changed, consistency should be checked
					// FIXED ... I hope
					v.add(e.getEdge(Integer.parseInt(indexString) - 1));
					break;
				}
			}
		}
      param.setEdgeIndexes(v);
    }

    public Graph getGraph() {
        return graph;
    }
}

