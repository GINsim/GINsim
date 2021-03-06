package org.ginsim.core.graph.regulatorygraph.logicalfunction.parser;

public abstract class TUnaryOperator extends TBooleanOperator {
  protected TBooleanTreeNode arg;

  public TUnaryOperator() {
    super();
  }
  public void setArg(TBooleanTreeNode node) {
    arg = node;
  }
  public String toString(boolean par) {
  	String s = getSymbol() + arg.toString(true);
    return s;
  }
  public int getNbArgs() {
    return 1;
  }
  public TBooleanTreeNode[] getArgs() {
    TBooleanTreeNode[] r = new TBooleanTreeNode[1];
    r[0] = arg;
    return r;
  }
}
