package fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel;

import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsEdgeIndex;
import java.util.Vector;
import java.util.Iterator;

public class GsTreeParam extends GsTreeElement {
  private Vector edgeIndexes;
  private boolean error, warning;

  public GsTreeParam(GsTreeElement parent, Vector v) {
    super(parent);
    this.edgeIndexes = v;
    error = warning = false;
  }
  public GsTreeParam(GsTreeElement parent) {
    super(parent);
    edgeIndexes = null;
  }
  public String toString() {
    String s = "";
    GsEdgeIndex ei;
    if (edgeIndexes != null) {
		for (Iterator it = edgeIndexes.iterator(); it.hasNext(); ) {
		    ei = (GsEdgeIndex)it.next();
		    s = s + " " + ei.data.getId(ei.index);
		  }
	}
    return s.trim();
  }
  public Vector getEdgeIndexes() {
    return edgeIndexes;
  }
  public void setEdgeIndexes(Vector v) {
    edgeIndexes = v;
  }
  public void setError(boolean b) {
    error = b;
  }
  public boolean isError() {
    return error;
  }
  public void setWarning(boolean b) {
    warning = b;
  }
  public boolean isWarning() {
    return warning;
  }
  public boolean isBasal() {
    return edgeIndexes == null;
  }
  public int compareTo(Object o) {
    GsTreeElement element = (GsTreeElement)o;
    if (toString().equals(element.toString()) && toString().equals("") && element instanceof GsTreeParam) {
      if (isBasal() && ((GsTreeParam)element).isBasal()) {
        return 0;
      }
      return 1;
    }
    return toString().compareTo(element.toString());
  }
}
