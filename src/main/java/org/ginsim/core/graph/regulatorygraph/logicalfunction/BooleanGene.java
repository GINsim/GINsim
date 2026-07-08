package org.ginsim.core.graph.regulatorygraph.logicalfunction;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.parser.TBooleanData;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.parser.TBooleanOperand;


/**
 * class BooleanGene
 */
public class BooleanGene extends TBooleanOperand {
  private LogicalFunctionList il;
  private RegulatoryMultiEdge me;
  private RegulatoryEdge edge;

    /**
     * Constructor
     */
    public BooleanGene() {
    super();
    il = null;
  }

    /**
     * Getter of TBooleanData
     * @return TBooleanData value
     */
    public TBooleanData getValue() {
    return il;
  }

    /**
     * LogicalFunctionList  setter
     * @param list the LogicalFunctionList
     */
    public void setLogicalFunctionList(LogicalFunctionList list) {
    il = list;
  }

    /**
     * Reteur the value as string
     * @param par boolean par for partial not used
     * @return the value as string
     */
    public String toString(boolean par) {
    return getVal();
  }

    /**
     * Save value getter
     * @return the save value as string
     */
    public String getSaveVal(){
    return ((BooleanParser)parser).getSaveString(value);
  }

    /**
     * Value getter
     * @return the value as tring
     */
    public String getVal() {
	  if (me == null) {
		  return "nil";
	  }
	  if (edge != null) {
		  return edge.getShortInfo();
	  }
	  return me.getSource().getId();
  }

    /**
     * InteractionName steer
     * @param parser the BooleanParser
     * @param value the string value
     * @throws GsException the Gs exception
     */
    public void setInteractionName(BooleanParser parser, String value) throws GsException {
	  setParser(parser);
	  setValue(value);
	  Object o = parser.getEdge(value);
	  if (o instanceof RegulatoryMultiEdge) {
		  me = (RegulatoryMultiEdge)o;
	  } else {
		  edge = (RegulatoryEdge)o;
		  me = edge.me;
	  }
  }

    /**
     * Test if has edge
     * @param element a LogicalFunctionListElement element
     * @return boolean if has edge
     */
    public boolean hasEdge(LogicalFunctionListElement element) {
	  RegulatoryMultiEdge me = element.getEdge();
	  if (me == null) {
		  return false;
	  }
	  if (edge == null) {
		  return this.me == me;
	  }
	  return edge == me.getEdge(element.getIndex());
  }
}
