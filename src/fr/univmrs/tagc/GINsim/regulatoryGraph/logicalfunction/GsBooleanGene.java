package fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction;

import org.ginsim.exception.GsException;
import org.ginsim.graph.regulatorygraph.RegulatoryEdge;
import org.ginsim.graph.regulatorygraph.RegulatoryMultiEdge;

import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.parser.TBooleanData;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.parser.TBooleanOperand;

public class GsBooleanGene extends TBooleanOperand {
  private GsLogicalFunctionList il;
  private RegulatoryMultiEdge me;
  private RegulatoryEdge edge;

  public GsBooleanGene() {
    super();
    il = null;
  }
  public TBooleanData getValue() {
    return il;
  }
  public void setLogicalFunctionList(GsLogicalFunctionList list) {
    il = list;
  }
  public String toString(boolean par) {
    return getVal();
  }
  public String getSaveVal(){
    return ((GsBooleanParser)parser).getSaveString(value);
  }
  public String getVal() {
	  if (me == null) {
		  return "nil";
	  }
	  if (edge != null) {
		  return edge.getShortInfo();
	  }
	  return me.getSource().getId();
  }
  public void setInteractionName(GsBooleanParser parser, String value) throws GsException {
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
  public boolean hasEdge(GsLogicalFunctionListElement element) {
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
