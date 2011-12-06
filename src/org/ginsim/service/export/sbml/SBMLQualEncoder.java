package org.ginsim.service.export.sbml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialState;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameterList;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDBrowserListener;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNodeBrowser;
import org.ginsim.core.utils.log.LogManager;

public class SBMLQualEncoder implements OMDDBrowserListener{

	public static final String L3_QUALI_URL = "http://sbml.org/Community/Wiki/SBML_Level_3_Proposals/Qualitative_Models";

    List<RegulatoryNode> v_no;
    int len;
    OMDDNode[] t_tree;
    OMDDNodeBrowser browser;
    int curValue;
    XMLWriter out;
	
	/*
	 * This is where the real export is done.
	 * This method will be called by GsAbstractExport once the export configuration panel has been properly filled.
	 */
	protected void doExport( RegulatoryGraph graph, SBMLQualConfig config, String filename) throws IOException {

        v_no = graph.getNodeOrder();
        len = v_no.size();
        t_tree = ((RegulatoryGraph)graph).getAllTrees(true);
        browser = new OMDDNodeBrowser(this, t_tree.length);
        
        byte[][] t_markup = new byte[len][2];
		Iterator itinit = config.getInitialState().keySet().iterator();
		Map m_initstates = null;
		if (itinit.hasNext()) {
			m_initstates = ((InitialState) itinit.next()).getMap();
		}
		itinit = null;
		if (m_initstates == null) {
			m_initstates = new HashMap();
		}
        for (int i=0 ; i<len ; i++) {
            RegulatoryNode vertex = (RegulatoryNode)v_no.get(i);
            // default initial markup = 0
            t_markup[i][0] = 0;
            t_markup[i][1] = vertex.getMaxValue();
            List init = (List)m_initstates.get(vertex);
            if (init != null && init.size() > 0) {
                t_markup[i][0] = ((Integer)init.get(0)).byteValue();
            }
        }

        // FIXME: DTD for SBML ?
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
        out = new XMLWriter(os, null);
        String s_compartment = "c_"+graph.getGraphName();
        out.openTag("sbml");
        out.addAttr("xmlns", "http://www.sbml.org/sbml/level3/version1/core");
        out.addAttr("level", "3");
        out.addAttr("version", "1");
        out.addAttr("xmlns:qual", L3_QUALI_URL);
        out.addAttr("qual:required", "true");
        
        out.openTag("model");
        out.addAttr("id", ""+ getPrevFilename( GraphManager.getInstance().getGraphPath( graph)));
        
        out.openTag("listOfCompartments");
        out.openTag("compartment");
        out.addAttr("id", s_compartment);
        out.closeTag();
        out.closeTag();
        
        // List all components
        out.openTag("listOfQualitativeSpecies");
        out.addAttr("xmlns", L3_QUALI_URL);

        for (int i=0 ; i<t_tree.length ; i++) {
            RegulatoryNode node = (RegulatoryNode)v_no.get(i);
            String s_id = node.getId();
            String s_name = node.getName();
            out.openTag("qualitativeSpecies");
            out.addAttr("id", s_id);
            if ((s_name != null) && (!s_name.equals("noName"))) {
            	out.addAttr("name",s_name);
            } 
            out.addAttr("compartment",s_compartment);
            out.addAttr("maxLevel",""+node.getMaxValue());
            out.addAttr("initialLevel",""+t_markup[i][0]);
            if (node.isInput()) {
                out.addAttr("boundaryCondition", "true");
                out.addAttr("constant", "true");
            }
            else {
            	out.addAttr("boundaryCondition", "false");
                out.addAttr("constant", "false");
            }
            out.closeTag();
        }
        out.closeTag(); // list of species
        
        // Dynamical rules: a transition for each component
        out.openTag("listOfTransitions");
        out.addAttr("xmlns", L3_QUALI_URL);
        for (int i=0 ; i<t_tree.length ; i++) {
            RegulatoryNode regulatoryNode = (RegulatoryNode)v_no.get(i);
            if (regulatoryNode.isInput()) {
            	continue;
            }
            OMDDNode node = t_tree[i];
            String s_node = regulatoryNode.getId();
            out.openTag("transition");
            out.addAttr("id", "tr_"+s_node);
            
            out.openTag("listOfInputs");               
            String edgeSign = null;
            for (RegulatoryMultiEdge me: graph.getIncomingEdges(v_no.get(i))) {
                int sign = me.getSign(); 
                switch (sign) {
				case 0:
					edgeSign = "SBO:0000459";
					break;
				case 1:
					edgeSign = "SBO:0000020";
					break;
				default:
					break;
				}                   
                out.openTag("input");
                out.addAttr("qualitativeSpecies", me.getSource().toString());
                out.addAttr("transitionEffect","none");
                out.addAttr("sign", ""+edgeSign);
                out.closeTag();
            }
            out.closeTag();

            out.openTag("listOfOutputs");
            out.openTag("output");
            out.addAttr("qualitativeSpecies", s_node);
            out.addAttr("transitionEffect","assignmentLevel");
            out.closeTag();
            out.closeTag();

            out.openTag("listOfFunctionTerms");
            out.openTag("defaultTerm");
            
            boolean hasNoBasalValue = true;
            if (graph.getIncomingEdges(v_no.get(i)).size() == 0) {
                LogicalParameterList lpl = regulatoryNode.getV_logicalParameters();
                if (lpl.size() == 1) {
                	LogicalParameter lp = (LogicalParameter) lpl.get(0);
                	int value = lp.getValue();
                	if (lpl.isManual(lp)) {
       			    	out.addAttr("resultLevel", ""+value);
                	    out.closeTag(); 
                	    hasNoBasalValue = false;
                	}
                }
            } 
            if (hasNoBasalValue) {
            out.addAttr("resultLevel", ""+0);
            out.closeTag();
            for (curValue=1 ; curValue<=regulatoryNode.getMaxValue() ; curValue++) {
                out.openTag("functionTerm");
                out.addAttr("resultLevel", ""+curValue);
                out.openTag("math");
                out.addAttr("xmlns", "http://www.w3.org/1998/Math/MathML");
                
                out.openTag("apply");
                out.addTag("or");    // enforced for now, we should try to avoid it when not needed
                browser.browse(node); // will call leafReached()
                out.closeTag();  // apply

                out.closeTag(); // math
                out.closeTag(); // functionTerm
            }
            }
            out.closeTag(); // listOfFunctionTerms
            out.closeTag(); // transition
        }
            
        out.closeTag(); // list of transitions
        
		// Close the file
        out.closeTag(); // model
        out.closeTag(); // sbml
        os.flush();
        os.close();
    }
	
	public void leafReached(int value, int depth, int[][] path) {
		if (value != curValue) {
			return;
		}
		
        try {
			out.openTag("apply");
	        out.addTag("and");    // enforced for now, we should try to avoid it when not needed

	        // TODO: list of conditions
	        for (int i=0 ; i<depth ; i++) {
	        	int level = path[i][2];
	        	if (path[i][0] > 0) {
	        		LogManager.trace( "case GEQ");
	        		writeConstraint("geq", level, path[i][0]);
	        	}
	        	if (path[i][1] < path[i][3]) {
	        		LogManager.trace( "case LT");
	        		writeConstraint("lt", level, path[i][1]);
	        	}
	        }
	        
            out.closeTag();  // apply
	        
		} catch (IOException e) {
			e.printStackTrace();
		}

		// this is where we write the mathml corresponding to the selected path
	}
	
	private void writeConstraint(String cst, int idx, int value) throws IOException {
		out.openTag("apply");
		out.addTag(cst);
		
		out.openTag("ci");
		out.addContent(""+v_no.get(idx));
        out.closeTag();  // ci
		
        out.openTag("cn");
		out.addContent(""+value);
        out.closeTag();  // cn

        out.closeTag();  // apply
	}
	
	/**
	 * Return the name of the file in the given path without any extensions
	 * 
	 * @param full_path the path of the file
	 * @return the name of the file in the given path without any extensions
	 */
	public String getPrevFilename(String full_path) {
	
		// Retrieve the name of the file
		File file = new File( full_path);
		String real_name = file.getName();
		
		// Check if it exists an extension an remove it if so
		int index_dot = real_name.lastIndexOf( '.');
		if( index_dot >=0){
			real_name = real_name.substring(0, index_dot);
		}
		
		return real_name;	
	    }
}
