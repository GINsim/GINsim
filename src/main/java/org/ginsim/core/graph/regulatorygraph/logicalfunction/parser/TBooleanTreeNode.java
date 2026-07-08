package org.ginsim.core.graph.regulatorygraph.logicalfunction.parser;

/**
 * interface TBooleanTreeNode
 */
public interface TBooleanTreeNode {
  /**
   * TBooleanData getter
   * @return the TBooleanData data
   * @throws Exception the exception
   */
  public TBooleanData getValue() throws Exception;

  /**
   * ReturnClass return as string
   * @param cl ReturnClass as string
   */
  public void setReturnClass(String cl);

  /**
   * Parser setter
   * @param parser the TBooleanParser
   */
  public void setParser(TBooleanParser parser);

  /**
   * To string function
   * @param par boolean partial
   * @return representation as string
   */
  public String toString(boolean par);

  /**
   * Test if is leaf
   * @return boolean is is Leaf
   */
  public boolean isLeaf();
}
