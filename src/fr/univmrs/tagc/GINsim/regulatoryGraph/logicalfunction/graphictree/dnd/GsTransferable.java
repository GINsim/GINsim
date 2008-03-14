package fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.List;

import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeExpression;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeParam;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeValue;

public class GsTransferable implements Transferable {
  public static final DataFlavor FUNCTION_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Functions");
  public static final DataFlavor VALUE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Values");
  public static final DataFlavor PARAM_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Parameters");
  public static final DataFlavor MIXED_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Mixed elements");
  public static final DataFlavor MANUAL_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Manual");
  public static final DataFlavor PLAIN_TEXT_FLAVOR = DataFlavor.getTextPlainUnicodeFlavor();
  public static final DataFlavor[] dataFlavors = { FUNCTION_FLAVOR, VALUE_FLAVOR, PARAM_FLAVOR, MIXED_FLAVOR, MANUAL_FLAVOR, PLAIN_TEXT_FLAVOR };
  public static final List dataFlavorsList = Arrays.asList(dataFlavors);

  private GsTreeElement[] nodes;
  private DataFlavor currentFlavor;
  private GsTreeElement pParent;

  public GsTransferable(GsTreeElement[] el) {
    super();
    nodes = el;
    pParent = null;
    if (nodes[0] instanceof GsTreeExpression) {
		currentFlavor = FUNCTION_FLAVOR;
	} else if (nodes[0] instanceof GsTreeValue) {
		currentFlavor = VALUE_FLAVOR;
	} else {
		currentFlavor = MIXED_FLAVOR;
	}
    if (currentFlavor != MIXED_FLAVOR) {
		for (int i = 1; i < nodes.length; i++) {
			if (nodes[i] instanceof GsTreeExpression && currentFlavor != FUNCTION_FLAVOR) {
		      currentFlavor = MIXED_FLAVOR;
		      break;
		    }
		    else if (nodes[i] instanceof GsTreeValue && currentFlavor != VALUE_FLAVOR) {
		      currentFlavor = MIXED_FLAVOR;
		      break;
		    }
		    else if (nodes[i] instanceof GsTreeParam && currentFlavor != PARAM_FLAVOR) {
		      currentFlavor = MIXED_FLAVOR;
		      break;
		    }
		}
	}
  }
  public DataFlavor getCurrentFlavor() {
    return currentFlavor;
  }
  public GsTreeElement[] getNodes() {
    return nodes;
  }
  public GsTreeElement getPParent() {
    return pParent;
  }
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (flavor == PLAIN_TEXT_FLAVOR) {
      StringBuffer text = new StringBuffer();
      for (int i = 0; i < nodes.length; i++) {
		text.append(nodes[i].toString() + "\n");
	}
      return text.toString();
    }
    else if (dataFlavorsList.contains(flavor)) {
		return nodes;
	} else {
		throw new UnsupportedFlavorException(flavor);
	}
  }
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return dataFlavorsList.contains(flavor);
  }
  public DataFlavor[] getTransferDataFlavors() {
    return dataFlavors;
  }
}