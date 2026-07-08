package org.ginsim.core.graph.hierarchicaltransitiongraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.application.GsException;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.style.StyleManager;
import org.ginsim.core.io.parser.GinmlHelper;
import org.ginsim.core.io.parser.GsXMLHelper;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationStrategy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Parse XML files defining hierarchical graphs.
 *
 * @author Duncan Berenguier
 */
public class HierarchicalTransitionGraphParser extends GsXMLHelper {
    
    private static final int POS_OUT = 0;
    private static final int POS_COMPACT = 5;
    private static final int POS_COMPACT_B = 6;
    private static final int POS_FILTERED = 50;
    private static final int POS_GRAPH_NOTES = 1;
    private static final int POS_GRAPH_NOTES_LINKLIST = 2;
    private static final int POS_VERTEX = 10;
    private static final int POS_VERTEX_VS = 11;
    private static final int POS_VERTEX_TYPE = 12;
    private static final int POS_VERTEX_TYPE_S = 13;
    private static final int POS_VERTEX_STATES = 14;
    private static final int POS_VERTEX_STATES_S = 15;
    private static final int POS_EDGE = 20;
    private static final int POS_EDGE_VS = 21;

    private int pos = POS_OUT;
    private HierarchicalTransitionGraph htg;
    
    private HierarchicalNode vertex = null;
    private DecisionOnEdge edge = null;
	private StyleManager<HierarchicalNode, DecisionOnEdge> styleManager;
    private NodeAttributesReader vareader = null;
    private EdgeAttributesReader ereader = null;
    private Annotation annotation = null;
    private Set<String> nodeToParse;
    private Map<String, HierarchicalNode> oldIdToNode = new HashMap<String, HierarchicalNode>();
	private byte[] childCount;
    
    /**
     * Constructor
     * @param nodeToParse the set of node to parse, the others will be filtered
     * @param attributes
     * @param s_dtd
     */
    public HierarchicalTransitionGraphParser(Set<String> nodeToParse, Attributes attributes, String s_dtd) throws GsException{
    	
        this.htg = GSGraphManager.getInstance().getNewGraph( HierarchicalTransitionGraph.class);
        
    	this.nodeToParse = nodeToParse;
		styleManager = htg.getStyleManager();
		vareader = htg.getNodeAttributeReader();
		ereader = htg.getEdgeAttributeReader();
		
        if (attributes.getValue("type")!=null) {
            if (attributes.getValue("type").equals("scc")) {
                htg.setSimulationStrategy(SimulationStrategy.SCCG);
            } else {
                htg.setSimulationStrategy(SimulationStrategy.HTG);
            }
        } else {
            htg.setSimulationStrategy(SimulationStrategy.HTG);
        }
        
		try {
			htg.setGraphName(attributes.getValue("id"));
		} catch (GsException e) {
			throw new GsException(GsException.GRAVITY_ERROR, "invalidGraphName");
		}
		String[] t_nodeOrder = attributes.getValue("nodeorder").split(" ");
		List<NodeInfo> nodeOrder = new ArrayList<NodeInfo>(t_nodeOrder.length);
		childCount = new byte[t_nodeOrder.length];
		for (int i=0 ; i<t_nodeOrder.length ; i++) {
			String[] args = t_nodeOrder[i].split(":");
			byte max = 1;
            if (args.length > 1) {
                try {
                    max = Byte.parseByte(args[1]);
                } catch(NumberFormatException e) {}
            }
		    nodeOrder.add( new NodeInfo( args[0], max));
		    childCount[i] = (byte)(max+1);
		}
		htg.setNodeOrder(nodeOrder);
		htg.setChildsCount(childCount);
	}

    /**
     * 
     */
    public HierarchicalTransitionGraphParser() {
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
			        annotation.setComment(curval);
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
            case POS_VERTEX_TYPE_S:
                if (qName.equals("string")) {
                    pos = POS_VERTEX_TYPE;
                    vertex.setTypeFromString(curval);
                	curval = null;
                }
                break; // POS_VERTEX_TYPE_S
            case POS_COMPACT_B:
                if (qName.equals("bool")) {
                    pos = POS_COMPACT;
                    htg.setMode(curval.trim().equalsIgnoreCase("true"));
                	curval = null;
                }
                break; // POS_COMPACT_B
            case POS_COMPACT:
                if (qName.equals("attr")) {
                    pos = POS_OUT;
                }
                break; // POS_COMPACT
            case POS_VERTEX_TYPE:
                if (qName.equals("attr")) {
                    pos = POS_VERTEX;
                }
                break; // POS_VERTEX_TYPE
            case POS_VERTEX_STATES_S:
                if (qName.equals("string")) {
                    pos = POS_VERTEX_STATES;
                    vertex.parse(curval);
                	curval = null;
                }
                break; // POS_VERTEX_STATES_S
            case POS_VERTEX_STATES:
                if (qName.equals("attr")) {
                    pos = POS_VERTEX;
                }
                break; // POS_VERTEX_STATES
			case POS_EDGE:
			    if (qName.equals("edge")) {
			    	edge = null;
			        pos = POS_OUT;
			    }
			    break; // POS_EDGE
			case POS_EDGE_VS:
			    if (qName.equals("edgevisualsetting")) {
			        pos = POS_EDGE;
			    }
			    break; // POS_EDGE_VS
        }
        super.endElement(uri, localName, qName);
    }
    
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        switch(pos) {
        	case POS_OUT:
        		if (qName.equals("nodestyle") || qName.equals("edgestyle")) {
                	styleManager.parseStyle(qName, attributes);
        		} else if (qName.equals("node")) {
                    String id = attributes.getValue("id");
                    if (nodeToParse == null || nodeToParse.contains(id)) {
                        pos = POS_VERTEX;
                        vertex = new HierarchicalNode(htg);
                        oldIdToNode.put(id, vertex);
                        htg.addNode(vertex);
                    } else {
                        pos = POS_FILTERED;
                    }
                } else if (qName.equals("edge")) {
                    String s_from = attributes.getValue("from");
                    String s_to = attributes.getValue("to");
                    if (nodeToParse == null || nodeToParse.contains(s_from) && nodeToParse.contains(s_to)) {
                        pos = POS_EDGE;
                        edge = htg.addEdge(oldIdToNode.get(s_from), oldIdToNode.get(s_to));
                    } else {
                        pos = POS_FILTERED;
                    }
                } else if (qName.equals("graph")) {
            			if (!HierarchicalTransitionGraphFactory.KEY.equals(attributes.getValue("class"))) {
            				throw new SAXException( new GsException( GsException.GRAVITY_ERROR, "STR_HTG_NotHierarchicalTransitionGraph"));
            			}
                        if (attributes.getValue("type") != null && attributes.getValue("type").equals("scc")) {
                            htg.setSimulationStrategy(SimulationStrategy.SCCG);
                        } else {
                            htg.setSimulationStrategy(SimulationStrategy.HTG);
                        }
            			try {
							htg.setGraphName( attributes.getValue("id"));
						} catch (GsException e) {
							throw new SAXException( new GsException(GsException.GRAVITY_ERROR, "STR_InvalidGraphName"));
						}
						try {
							String[] t_nodeOrder = attributes.getValue("nodeorder").split(" ");
							List<NodeInfo> nodeOrder = new ArrayList<NodeInfo>(t_nodeOrder.length);
							byte[] childCount = new byte[t_nodeOrder.length];
							for (int i=0 ; i<t_nodeOrder.length ; i++) {
								String[] args = t_nodeOrder[i].split(":");
							    nodeOrder.add( new NodeInfo( args[0]));
							    childCount[i] = (byte) (Byte.parseByte(args[1])+1);
							}
							htg.setNodeOrder(nodeOrder);
							htg.setChildsCount(childCount);
						} catch (NumberFormatException e) {
							throw new SAXException( new GsException( "STR_InvalidNodeOrder", e));
						}
						
						// DEPRECATED support old fashion storage of compaction mode
						int mode = Integer.parseInt(attributes.getValue("iscompact"));
						htg.setMode(mode==2);
                } else if (qName.equals("attr") && attributes.getValue("name").equals("isCompact")) {
                	pos = POS_COMPACT;
                } else if (qName.equals("link")) {
                    htg.setAssociatedGraphID(attributes.getValue("xlink:href"));
                } else if (qName.equals("annotation")) {
                    pos = POS_GRAPH_NOTES;
                    annotation = this.htg.getAnnotation();
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
                    annotation.addLink(attributes.getValue("xlink:href"), htg);
                }
                break; // POS_GRAPH_NOTES_LINKLIST

            case POS_VERTEX:
            	if (qName.equals("attr") && attributes.getValue("name").equals("type")) {
            		pos = POS_VERTEX_TYPE;
                } else if (qName.equals("attr") && attributes.getValue("name").equals("states")) {
                    pos = POS_VERTEX_STATES;
                } else if (vareader != null && qName.equals("nodevisualsetting")) {
                	vareader.setNode(vertex);
                	if (GinmlHelper.loadNodeStyle(styleManager, vareader, attributes)) {
                		pos = POS_VERTEX_VS;
            		}
                }
                break; // POS_VERTEX

            case POS_VERTEX_TYPE:
                if (qName.equals("string")) {
                    pos = POS_VERTEX_TYPE_S;
                    curval = "";
                }
                break; // POS_VERTEX_TYPE
                
            case POS_COMPACT:
                if (qName.equals("bool")) {
                    pos = POS_COMPACT_B;
                    curval = "";
                }
                break; // POS_VERTEX_TYPE
                
            case POS_VERTEX_STATES:
                if (qName.equals("string")) {
                    pos = POS_VERTEX_STATES_S;
                    curval = "";
                }
                break; // POS_VERTEX_STATES
                
            case POS_EDGE:
                if (qName.equals("edgevisualsetting")) {
                    ereader.setEdge(edge);
                	if (GinmlHelper.loadEdgeStyle(styleManager, ereader, attributes)) {
                		pos = POS_EDGE_VS;
                	}
                }
                break; // POS_EDGE
                
            case POS_EDGE_VS:
            	ereader.setEdge(edge);
            	GinmlHelper.applyEdgeVisualSettings(edge, styleManager, ereader, vareader, qName, attributes);
                break; // POS_EDGE_VS
            case POS_VERTEX_VS:
            	GinmlHelper.applyNodeVisualSettings(vareader, styleManager, qName, attributes);
                break; // POS_VERTEX_VS
        }
    }

    public Graph getGraph() {
    	
        return htg;
    }
}
