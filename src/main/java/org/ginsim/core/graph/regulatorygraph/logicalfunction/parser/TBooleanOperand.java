package org.ginsim.core.graph.regulatorygraph.logicalfunction.parser;

/**
 * abstract class TBooleanOperand
 */
public abstract class TBooleanOperand implements TBooleanTreeNode {
  protected String value;
  protected String returnClass;
  protected TBooleanParser parser;

  /**
   * Constructor of TBooleanOperand
   */
  public TBooleanOperand() {
    super();
  }

  /**
   * Getter of Reteurn class as string
   * @param cl the class as string
   */
  public void setReturnClass(String cl) {
    returnClass = cl;
  }

  /**
   * Setter of Value
   * @param val the string value
   */
  public void setValue(String val) {
    value = val;
  }

  /**
   * Setter of TBooleanParser
   * @param parser the TBooleanParser parser
   */
  public void setParser(TBooleanParser parser) {
    this.parser = parser;
  }

  /**
   * Test if is Leaf
   * @return boolean if is Leaf
   */
  public boolean isLeaf() {
    return true;
  }

  /**
   * Value getter
   * @return the value as string
   */
  public String getVal() {
    return value;
  }
  public String toString() {
	  return toString(false);
  }
}
