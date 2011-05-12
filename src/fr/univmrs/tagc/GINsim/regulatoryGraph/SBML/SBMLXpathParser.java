package fr.univmrs.tagc.GINsim.regulatoryGraph.SBML;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import JSci.io.MathMLExpression;
import JSci.io.MathMLParser;
import fr.univmrs.tagc.GINsim.global.GsEnv;
import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsGraphNotificationAction;
import fr.univmrs.tagc.GINsim.graph.GsGraphNotificationMessage;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsLogicalParameter;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryEdge;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryMultiEdge;// just to get rescanSign() method
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.GsBooleanParser;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsModel;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeExpression;
import fr.univmrs.tagc.common.GsException;
import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.widgets.StackDialog;

public final class SBMLXpathParser {
	/** Creates a new instance of SbmlXpathParser */
	private GsRegulatoryGraph graph;
	protected File _FilePath;
	private String s_nodeOrder = "";
	private GsRegulatoryVertex vertex = null;
	public GsRegulatoryEdge edge = null;
	private GsVertexAttributesReader vareader = null;
	private GsEdgeAttributesReader ereader = null;

	private int vslevel = 0;

	private Map m_edges = new HashMap();
	private Map map;
	Map m_checkMaxValue;

	private Hashtable values;
	private Vector v_function;

	public SBMLXpathParser() {
	}

	public SBMLXpathParser(String filename) {
		this.graph = new GsRegulatoryGraph();
		this._FilePath = new File(filename);
		values = new Hashtable();
		map = new HashMap();
		initialize();

	}

	public void initialize() {
		parse();
	}

	/**
	* Parsing sbml file
	*/
	public void parse() {
		Document document = null;
		try {
			/** create a SAXBuilder instance */
			SAXBuilder sxb = new SAXBuilder();
			document = sxb.build(_FilePath);
		} catch (IOException e) {
			System.out.println("Ereur lors de la lecture du fichier" + e.getMessage());
			e.printStackTrace();
		} catch (JDOMException e) {
			System.out.println("Ereur lors de la construction du fichier JDOM" + e.getMessage());
			e.printStackTrace();
		}

		try {
			/**
			 * initialization of the root element.
			 */
			Element racine = document.getRootElement();

			Namespace namespace1 = Namespace.getNamespace("sbml",
					"http://www.sbml.org/sbml/level3/version1");
			/** Recovery of the ID model. */
			XPath xpa1 = XPath.newInstance("//sbml:model/@id");
			xpa1.addNamespace(namespace1);
			String modelName = xpa1.valueOf(racine);
			try {
				graph.setGraphName(modelName);
			} catch (GsException e) {
				GsEnv.error(new GsException(GsException.GRAVITY_ERROR, "invalidGraphName"), null);
			}
			vareader = graph.getGraphManager().getVertexAttributesReader();
			ereader = graph.getGraphManager().getEdgeAttributesReader();

			Namespace namespace = Namespace.getNamespace("qual",
					"http://sbml.org/Community/Wiki/SBML_Level_3_Proposals/Qualitative_Models");
			/** Search the list of species. */
			XPath xpa = XPath.newInstance("//qual:listOfQualitativeSpecies");
			xpa.addNamespace(namespace);

			/** Search the transitions list*/
			XPath xpa2 = XPath.newInstance("//qual:listOfTransitions");
			xpa2.addNamespace(namespace);
			/**
			 * retrieves all the nodes corresponding to the path:/model/listOfQualitativeSpecies. 
			 */
			List results = xpa.selectNodes(racine);

			/**retrieves the "id" species 
			* and are supplied to a graph
			*/
			for (int i = 0; i < results.size(); i++) {
				Element obElement = (Element) results.get(i);
				byte maxvalue = 0;
				List elList = obElement.getChildren();
				Iterator it = elList.iterator();
				while (it.hasNext()) {
					try {
						Element elcurrent = (Element) it.next();
						s_nodeOrder += elcurrent.getAttributeValue("id") + " ";
						if (s_nodeOrder == null) {
							throw new JDOMException("missing nodeOrder");
						}
						String id = elcurrent.getAttributeValue("id");
						String name = elcurrent.getAttributeValue("name");
						if (name == null || name.equals("")) {
							name = "noName";
						}
						byte maxval = (byte) Integer.parseInt(elcurrent
								.getAttributeValue("maxLevel"));
						vertex = graph.addNewVertex(id, name, maxval);
						vertex.getV_logicalParameters().setUpdateDup(false);
						byte basevalue = 1;
						vertex.addLogicalParameter(new GsLogicalParameter(basevalue), true);

						String input = elcurrent.getAttributeValue("boundaryCondition");
						if (input != null) {
							vertex.setInput(input.equalsIgnoreCase("true") || input.equals("1"),
									graph);
						}
						values.put(vertex, new Hashtable());
					} catch (NumberFormatException e) {
						throw new JDOMException("mal formed node's parameter");// TODO:
																				// handle
																				// exception
					}
				}
			}
			/** retrieve all transitions list */
			List listOfTransition = xpa2.selectNodes(racine);
			for (int i = 0; i < listOfTransition.size(); i++) {
				Element transition = (Element) listOfTransition.get(i);
				List allTransitionElement = transition.getChildren();
				Element transElem = null;

				for (int k = 0; k < allTransitionElement.size(); k++) {
					/** retrieve a transition element */
					transElem = (Element) allTransitionElement.get(k);
					String trans_Id = transElem.getAttributeValue("id");
					/** retrieve children of transition element */
					List transChildren = transElem.getChildren(); 																																														
					/** retrieve  <listOfInputs> element */
					Element listOfInput = (Element) transChildren.get(0);
					List inputElemList = listOfInput.getChildren();
					/** retrieve children of <listOfInputs> element */
					for (int p = 0; p < inputElemList.size(); p++) {
						try {
							Element input = (Element) inputElemList.get(p);

							String qualitativeSpecies = ((Element) input)
									.getAttributeValue("qualitativeSpecies");
							String sign = ((Element) input).getAttributeValue("sign");
							if (sign.equals("SBO:0000020"))
								sign = "negative";
							else if (sign.equals("SBO:0000459"))
								sign = "positive";
							else
								sign = "unknown"; 
							String transitionEffect = ((Element) input)
									.getAttributeValue("transitionEffect");
							String boundaryCondition = ((Element) input)
									.getAttributeValue("boundaryCondition");
							String to = getNodeId(trans_Id);						
							String minimumvalue = ((Element)input).getAttributeValue("minvalue");
							byte minvalue = (byte)Integer.parseInt(getAttributeValueWithDefault(minimumvalue, "1"));
							String maximumvalue = ((Element)input).getAttributeValue("maxvalue");							
							String smax = getAttributeValueWithDefault(maximumvalue, "-1");
							byte maxvalue = -2;
							edge = graph.addNewEdge(qualitativeSpecies, to, minvalue, sign);							
							if (smax.startsWith("m")) {
                            	maxvalue = -1; 
                            } else 
                            {
                            	maxvalue = (byte)Integer.parseInt(smax);
                            }
							storeMaxValueForCheck(edge, maxvalue);
							m_edges.put(qualitativeSpecies, edge);
							edge.me.rescanSign(graph);
							ereader.setEdge(edge.me);
						} // try
						catch (NumberFormatException e) {
							throw new JDOMException("mal formed interaction's parameters");
						}
					} // for
					Element listOfOutput = (Element) transChildren.get(1);
					List outputElemList = listOfOutput.getChildren();
					Element output = (Element) outputElemList.get(0);
					String qualSpecies = output.getAttributeValue("qualitativeSpecies");
					String transEffect = output.getAttributeValue("transitionEffect");
					Element listOfFunctionTerm = (Element) transChildren.get(2);
					List functTermChildren = listOfFunctionTerm.getChildren();
					/** retrieve <defaultTerm> element */
					Element defaultTerm = (Element) functTermChildren.get(0);
					String fctResultLevel = null;						
					for (int j = 1; j < functTermChildren.size(); j++) 
					{
						v_function = new Vector();
						Element functionTerm = (Element) functTermChildren.get(j);
						fctResultLevel = functionTerm.getAttributeValue("resultLevel");
						StringBuffer sb = deal(functionTerm); 																																					
						v_function.addElement(sb.toString());
						String myVertex = null;
						for (Enumeration enumvertex = values.keys(); enumvertex.hasMoreElements();) 
						{
							vertex = (GsRegulatoryVertex) enumvertex.nextElement();
							String vertexName = vertex.toString();
							if(qualSpecies.equals(vertexName))
							{								
								((Hashtable) values.get(vertex)).put(fctResultLevel, v_function);							
							}
						}						
					} // for					
				} // for 
			} // for 
		} //  try 
		catch (Exception e) {
			// TODO: handle exception
		}
		placeInteractions();
		placeNodeOrder();
		graph.setSaveMode(vslevel);
		if (!values.isEmpty()) {
			parseBooleanFunctions();
		}
		Iterator it = graph.getNodeOrder().iterator();
		while (it.hasNext()) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex) it.next();
			vertex.getV_logicalParameters().cleanupDup();
		}
	} // void parse(File _FilePath)

	public StringBuffer deal(Element root) throws SAXException, IOException {
		List rootConv = root.getChildren();
		Element element = (Element) rootConv.get(0);
		XMLOutputter outputer = new XMLOutputter(Format.getPrettyFormat());
		String xml = outputer.outputString(element);

		InputSource file = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		MathMLParser parser = new MathMLParser();
		parser.parse(file);

		int nLevel = 0;
		Object[] parseList = parser.translateToJSciObjects();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < parseList.length; i++) {
			Object o = parseList[i];
			MathMLExpression expr = (MathMLExpression) o;
			exploreExpression(expr, nLevel, sb);
		}
		return sb;
	}

	public void exploreExpression(Object expression, int level, StringBuffer sb) {
		if (expression instanceof MathMLExpression) {
			MathMLExpression mexpr = (MathMLExpression) expression;
			String op = mexpr.getOperation();
			if (op.equals("lt") || op.equals("geq") || op.equals("leq") || op.equals("gt")) {
				String chaine = null;
				if (op.equals("leq") || op.equals("lt")) {
					op = "!";
					chaine = op + mexpr.getArgument(0);
				} else if (op.equals("gt") || op.equals("geq")) {
					op = "";
					chaine = op + mexpr.getArgument(0);
				}
				sb.append(chaine);
			} else {
				if (mexpr.length() > 1)
					sb.append("(");
				for (int i = 0; i < mexpr.length(); i++) {
					exploreExpression(mexpr.getArgument(i), level + 1, sb);
					if (i < mexpr.length() - 1) {
						if (op.equals("and")) {
							sb.append("&");
						} else if (op.equals("or")) {
							sb.append("|");
						}
					}
				}
				if (mexpr.length() > 1)
					sb.append(")");
			}
		} else {
			sb.append("Erreur");
		}
	}

	public String getNodeId(String transId) {
		char pathSeparator = '_';
		int sep = transId.lastIndexOf(pathSeparator);
		return transId.substring(sep + 1);
	}

	private void storeMaxValueForCheck(GsRegulatoryEdge key, byte maxvalue) {
		if (m_checkMaxValue == null) {
			m_checkMaxValue = new HashMap();
		}
		m_checkMaxValue.put(key, new Integer(maxvalue));
	}

	private void placeInteractions() {
		// check the maxvalues of all interactions first
		if (m_checkMaxValue != null) {
			Map m = null;
			Iterator it = m_checkMaxValue.entrySet().iterator();
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				byte m1 = ((GsRegulatoryEdge) entry.getKey()).getMax();				
				byte m2 = ((Integer) entry.getValue()).byteValue();				
				byte max = ((GsRegulatoryEdge) entry.getKey()).me.getSource().getMaxValue();				
				if (m1 != m2) {
					if (m == null) { 
						m = new HashMap();
					}
					if (m1 == -1 && m2 == max || m2 == -1 && m1 == max) {
						m.put(entry, "");
					} else {
						m.put(entry, null);
					}
				}
			} 
			if (m != null) { 
				graph.addNotificationMessage(new GsGraphNotificationMessage(graph,
						"inconsistency in some interactions", new InteractionInconsistencyAction(),
						m, GsGraphNotificationMessage.NOTIFICATION_WARNING_LONG));
			}			
		}
	} // void placeInteractions()

	private void placeNodeOrder() {
		Vector v_order = new Vector();
		String[] t_order = s_nodeOrder.split(" ");
		for (int i = 0; i < t_order.length; i++) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex) graph.getGraphManager()
					.getVertexByName(t_order[i]);
			if (vertex == null) {
				// ok = false;
				break;
			}
			v_order.add(vertex);
		}
		if (v_order.size() != graph.getGraphManager().getVertexCount()) {
			// error
			Tools.error("incoherent nodeOrder, not restoring it", null);
		} else {
			graph.setNodeOrder(v_order);
		}
	} // void placeNodeOrder()

	private void parseBooleanFunctions() {
		List allowedEdges;
		GsRegulatoryVertex vertex;
		String value, exp;
		try {
			for (Enumeration enu_vertex = values.keys(); enu_vertex.hasMoreElements();) {
				vertex = (GsRegulatoryVertex) enu_vertex.nextElement();
				allowedEdges = graph.getGraphManager().getIncomingEdges(vertex);
				if (allowedEdges.size() > 0) {
					for (Enumeration enu_values = ((Hashtable) values.get(vertex)).keys(); enu_values
							.hasMoreElements();) {
						value = (String) enu_values.nextElement();
						for (Enumeration enu_exp = ((Vector) ((Hashtable) values.get(vertex))
								.get(value)).elements(); enu_exp.hasMoreElements();) {
							exp = (String) enu_exp.nextElement();
							addExpression(Byte.parseByte(value), vertex, exp);
						}
					}
					vertex.getInteractionsModel().parseFunctions(); 
					if (vertex.getMaxValue() + 1 == ((Hashtable) values.get(vertex)).size()) {
						((GsTreeElement) vertex.getInteractionsModel().getRoot()).setProperty(
								"add", new Boolean(false));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addExpression(byte val, GsRegulatoryVertex vertex, String exp) {
		try {
			GsBooleanParser tbp = new GsBooleanParser(graph.getGraphManager().getIncomingEdges(
					vertex));
			GsTreeInteractionsModel interactionList = vertex.getInteractionsModel();
			if (!tbp.compile(exp, graph, vertex)) {
				InvalidFunctionNotificationAction a = new InvalidFunctionNotificationAction();
				Vector o = new Vector();
				o.addElement(new Short(val));
				o.addElement(vertex);
				o.addElement(exp);
				graph.addNotificationMessage(new GsGraphNotificationMessage(graph,
						"Invalid formula : " + exp, a, o,
						GsGraphNotificationMessage.NOTIFICATION_WARNING));				
			} else {
				interactionList.addExpression(val, vertex, tbp);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public GsGraph getGraph() {
		return graph;
	}

    public String getAttributeValueWithDefault(String atValue, String defValue)
    {  	
	    if (atValue != null) 
	    {	
	    	return atValue;
	    }
	    return defValue;
	}
	
	/** Les classes ***/
	class InteractionInconsistencyAction implements GsGraphNotificationAction {
		public String[] getActionName() {
			String t[] = { "view" };
			return t;
		}

		public boolean perform(GsGraph graph, Object data, int index) {
			StackDialog d = new InteractionInconsistencyDialog((Map) data, graph,
					"interactionInconststancy", 200, 150);
			d.setVisible(true);
			return true;
		}

		public boolean timeout(GsGraph graph, Object data) {
			return true;
		}
	} // class InteractionInconsistencyAction

	class InteractionInconsistencyDialog extends StackDialog {
		private static final long serialVersionUID = 4607140440879983498L;

		GsRegulatoryGraph graph;
		Map m;
		JPanel panel = null;

		public InteractionInconsistencyDialog(Map m, GsGraph graph, String msg, int w, int h) {
			super(graph.getGraphManager().getMainFrame(), msg, w, h);
			this.graph = (GsRegulatoryGraph) graph;
			this.m = m;
			setMainPanel(getMainPanel());
		}

		private JPanel getMainPanel() {
			if (panel == null) {
				panel = new JPanel();
				JTextArea txt = new JTextArea();
				String s1 = "";
				String s2 = "";
				Iterator it = m.entrySet().iterator();
				while (it.hasNext()) {
					Entry entry = (Entry) it.next();
					Entry e2 = (Entry) entry.getKey();
					GsRegulatoryEdge edge = (GsRegulatoryEdge) e2.getKey();
					byte oldmax = ((Integer) e2.getValue()).byteValue();
					if (entry.getValue() == null) {
						s1 += edge.getLongDetail(" ") + ": max should be "
								+ (oldmax == -1 ? "max" : "" + oldmax) + "\n";
					} else {
						s2 += edge.getLongDetail(" ") + ": max was explicitely set to " + oldmax
								+ "\n";
					}
				}
				if (s1 != "") {
					s1 = "potential problems:\n" + s1 + "\n\n";
				}
				if (s2 != "") {
					s1 = s1 + "warnings only:\n" + s2;
				}
				txt.setText(s1);
				txt.setEditable(false);
				panel.add(txt);
			}
			return panel;
		}

		public void run() {
			// TODO: propose some automatic corrections
		}
	} // class InteractionInconsistencyDialog
	
	class InvalidFunctionNotificationAction implements GsGraphNotificationAction {

		public InvalidFunctionNotificationAction() {
			super();
		}

		public boolean timeout(GsGraph graph, Object data) {
			return false;
		}

		public boolean perform(GsGraph graph, Object data, int index) {
			Vector v = (Vector) data;
			byte value = ((Short) v.elementAt(0)).byteValue();
			GsRegulatoryVertex vertex = (GsRegulatoryVertex) v.elementAt(1);
			String exp = (String) v.elementAt(2);
			boolean ok = true;
			switch (index) {
			case 0:
				try {
					GsTreeInteractionsModel interactionList = vertex.getInteractionsModel();
					GsTreeExpression texp = interactionList.addEmptyExpression(value, vertex);
					texp.setText(exp);
					texp.setProperty("invalid", new Boolean("true"));
				} catch (Exception ex) {
					ex.printStackTrace();
					ok = false;
				}
				break;
			case 1:
				break;
			}
			return ok;
		}

		public String[] getActionName() {
			String[] t = { "Keep function", "Discard function" };
			return t;
		}
	} // class InvalidFunctionNotificationAction
}