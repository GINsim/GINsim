package fr.univmrs.tagc.GINsim.export.regulatoryGraph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPanel;

import fr.univmrs.tagc.GINsim.annotation.Annotation;
import fr.univmrs.tagc.GINsim.annotation.AnnotationLink;
import fr.univmrs.tagc.GINsim.export.GsAbstractExport;
import fr.univmrs.tagc.GINsim.export.GsExportConfig;
import fr.univmrs.tagc.GINsim.global.GsEnv;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.gui.GsPluggableActionDescriptor;
import fr.univmrs.tagc.GINsim.regulatoryGraph.*;
import fr.univmrs.tagc.GINsim.regulatoryGraph.initialState.*;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsModel;
import fr.univmrs.tagc.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeValue;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutantDef;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutants;
import fr.univmrs.tagc.GINsim.stableStates.GsSearchStableStates;
import fr.univmrs.tagc.GINsim.stableStates.StableTableModel;
import fr.univmrs.tagc.common.GsException;
import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.document.*;
import fr.univmrs.tagc.common.widgets.StackDialog;

/**
 * GenericDocumentExport is a plugin to export the documentation of a model into multiples document format.
 * 
 * It export using a documentWriter. You can add support for your own document writer using <u>addSubFormat</u>
 * 
 * @see DocumentWriter
 */
public class GenericDocumentExport extends GsAbstractExport {
	static public Vector v_format = new Vector();
	static {
		v_format.add(new GenericDocumentFormat(XHTMLDocumentWriter.class, "xHTML", new String[] {"html"}, "xHTML files (.html)", ".html"));
		v_format.add(new GenericDocumentFormat(OOoDocumentWriter.class, "OpenOffice.org", new String[] {"odt"}, "OpenOffice.org files (.odt)", ".odt"));
		v_format.add(new GenericDocumentFormat(WikiDocumentWriter.class, "Wiki", new String[] {"txt"}, "Text files (.txt)", ".txt"));
	}

	private GsExportConfig config = null;
	private DocumentExportConfig specConfig;
	protected DocumentWriter doc = null;
	protected Class documentWriterClass;

	private GsRegulatoryGraph graph;
	private List nodeOrder;
	private int len;
	
    public GenericDocumentExport() {
		id = "Documentation";
    }
    /**
     * Allow you to register a new document writer to support a new file type.
     * @see GenericDocumentFormat
     */
    protected static void addSubFormat(Class documentWriterClass, String id, String[] filter, String filterDescr, String extension) {
    	v_format.add(new GenericDocumentFormat(documentWriterClass, id, filter, filterDescr, extension));
    }
   
    /**
     * get a vector of all the GenericDocumentFormat the genericDocument can use.
     */
	public Vector getSubFormat() {
		return v_format;
	}
   
	public GsPluggableActionDescriptor[] getT_action(int actionType, GsGraph graph) {
        if (graph instanceof GsRegulatoryGraph) {
        	return new GsPluggableActionDescriptor[] {
        			new GsPluggableActionDescriptor("STR_Generic", "STR_Generic_descr", null, this, ACTION_EXPORT, 0)
        	};
        }
        return null;
	}

	protected void doExport(GsExportConfig config) {
		this.config = config;
		this.specConfig = (DocumentExportConfig)config.getSpecificConfig();
		if (specConfig == null) {
			specConfig = new DocumentExportConfig();
			config.setSpecificConfig(specConfig);
		}
		try {
			System.out.println(config.getFilename());
			this.doc = (DocumentWriter) documentWriterClass.newInstance();
			this.doc.setOutput(new File(config.getFilename()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			long l = System.currentTimeMillis();
			run();
			System.out.println("Generic export: done in "+(System.currentTimeMillis()-l)+"ms");
		} catch (IOException e) {
			e.printStackTrace();
			GsEnv.error(new GsException(GsException.GRAVITY_ERROR, e), null);
		}		
	}

	public boolean needConfig(GsExportConfig config) {
		return true;
	}

	protected JComponent getConfigPanel(GsExportConfig config, StackDialog dialog) {
		return new GDExportConfigPanel(config, dialog);
	}

	
	protected synchronized void run() throws IOException {
		this.graph = (GsRegulatoryGraph) config.getGraph();
		nodeOrder = graph.getNodeOrder();
		len = nodeOrder.size();
		setDocumentProperties();
		setDocumentStyles();
		if (doc.doesDocumentSupportExtra("javascript")) {
			setJavascript();
		}
		writeDocument();
	}

	private void writeDocument() throws IOException {
		doc.startDocument();
		doc.openHeader(1, "Description of the model \"" + graph.getGraphName() + "\"", null);
				
		//Graph annotation
		doc.openHeader(2, "Annotation", null);
		writeAnnotation(graph.getAnnotation());
		doc.openParagraph(null);
		doc.addImage(graph.getGraphManager().getImage(), "model.png");
		doc.closeParagraph();
		
		// all nodes with comment and logical functions
		if (true) {
			doc.openHeader(2, "Nodes", null);
			writeLogicalFunctionsTable(specConfig.putComment);
		}
		
		// initial states
		if (specConfig.exportInitStates) {
			doc.openHeader(2, "Initial States", null);
			writeInitialStates();
		}
		// mutant description
		if (specConfig.exportMutants) {
			doc.openHeader(2, "Mutants and Dynamical Behaviour", null);
			writeMutants();
		}

		doc.close();//close the document		
	}

	private void writeMutants() throws IOException {
		GsRegulatoryMutants mutantList = (GsRegulatoryMutants)graph.getObject(GsMutantListManager.key, true);
		GsSearchStableStates stableSearcher = new GsSearchStableStates(config.getGraph(), null, null);
		OmddNode stable;
		
		String[] cols;
		if (specConfig.searchStableStates && specConfig.putComment) {
			cols = new String[] {"", "", "", "", "", ""};
		} else if (specConfig.searchStableStates || specConfig.putComment){
			cols = new String[] {"", "", "", "", ""};
		} else {
			cols = new String[] {"", "", "", ""};
		}
		int nbcol = cols.length-1;
		doc.openTable("mutants", "table", cols);
		doc.openTableRow(null);
		doc.openTableCell("Mutants", true);
		doc.openTableCell("Gene", true);
		doc.openTableCell("Min", true);
		doc.openTableCell("Max", true);
		if (specConfig.putComment) {
			doc.openTableCell("Comment", true);
		}
		if (specConfig.searchStableStates) {
			doc.openTableCell("Stable States", true);
		}
		
		StableTableModel model = new StableTableModel(nodeOrder);
		for (int i=-1 ; i<mutantList.getNbElements(null) ; i++) {
			GsRegulatoryMutantDef mutant = 
				i<0 ? null : (GsRegulatoryMutantDef)mutantList.getElement(null, i);
			
			if (specConfig.searchStableStates) {
				stableSearcher.setMutant(mutant);
				stable = stableSearcher.getStable();
				model.setResult(stable);
			}
			int nbrow;
			Iterator it_multicellularChanges = null;
			if (i<0) { // wild type
				nbrow = 1;
				doc.openTableRow(null);
				doc.openTableCell(1, (model.getRowCount() > 0?2:1), "Wild Type", true);
				doc.openTableCell("-");
				doc.openTableCell("-");
				doc.openTableCell("-");
				if (specConfig.putComment) {
					doc.openTableCell("");
				}
			} else {
				if (!specConfig.multicellular) {
					nbrow = mutant.getNbChanges();
				} else {
					nbrow = mutant.getNbChanges();
					Map m_multicellularChanges = new HashMap();
					for (int c=0 ; c<nbrow ; c++) {
						String s = mutant.getName(c);
						// TODO: check that the mutant is indeed the same everywhere before doing so ?
						if (s.endsWith("1")) {
							m_multicellularChanges.put(s.substring(0, s.length()-1), 
									new int[] {mutant.getMin(c), mutant.getMax(c)});
						}
					}
					nbrow = m_multicellularChanges.size();
					it_multicellularChanges = m_multicellularChanges.entrySet().iterator();
				}
				if (nbrow < 1) {
					nbrow = 1;
				}
				doc.openTableRow(null);
				doc.openTableCell(1, (nbrow+(model.getRowCount() > 0?1:0)), mutant.getName(), true);
				if (mutant.getNbChanges() == 0) {
					doc.openTableCell("-");
					doc.openTableCell("-");
					doc.openTableCell("-");
				} else if (it_multicellularChanges == null){
					doc.openTableCell(mutant.getName(0));
					doc.openTableCell(""+mutant.getMin(0));
					doc.openTableCell(""+mutant.getMax(0));
				} else {
					Entry e = (Entry)it_multicellularChanges.next();
					doc.openTableCell(e.getKey().toString());
					int[] t_changes = (int[])e.getValue();
					doc.openTableCell(""+t_changes[0]);
					doc.openTableCell(""+t_changes[1]);
				}
				if (specConfig.putComment) {
					doc.openTableCell(1, nbrow, "", false);
					writeAnnotation(mutant.getAnnotation());//BUG?
				}
			}
			
			if (specConfig.searchStableStates) {
				// the common part: stable states
				if (model.getRowCount() > 0) {
					doc.openTableCell(1, nbrow, model.getRowCount()+" Stable states", false);
				} else {
					doc.openTableCell(1, nbrow, "", false);
				}
			}

			// more data on mutants:
			if (mutant != null) {
				for (int j=1 ; j<nbrow ; j++) {
					if (it_multicellularChanges == null) {
						doc.openTableRow(null);
						doc.openTableCell(mutant.getName(j));
						doc.openTableCell(""+mutant.getMin(j));
						doc.openTableCell(""+mutant.getMax(j));
					} else {
						Entry e = (Entry)it_multicellularChanges.next();
						doc.openTableRow(null);
						doc.openTableCell(e.getKey().toString());
						int[] t_changes = (int[])e.getValue();
						doc.openTableCell(""+t_changes[0]);
						doc.openTableCell(""+t_changes[1]);
					}
				}
			}
			
			// more data on stable states:
			if (specConfig.searchStableStates && model.getRowCount() > 0) {
				doc.openTableRow(null);
				doc.openTableCell(nbcol,1, null, false);
				
				doc.openList("L1");
				for (int k=0 ; k<model.getRowCount() ; k++) {
					doc.openListItem(null);
					boolean needPrev=false;
					for (int j=0 ; j<len ; j++) {
						Object val = model.getValueAt(k,j);
						if (!val.toString().equals("0")) {
							String s = needPrev ? " ; " : "";
							needPrev = true;
							if (val.toString().equals("1")) {
								doc.writeText(s+nodeOrder.get(j));
							} else {
								doc.writeText(s+nodeOrder.get(j)+"="+val);
							}
						}
					}
					doc.closeListItem();
				}
				doc.closeList();
			}
		}
		doc.closeTable();
	}
	
	
	private void writeInitialStates() throws IOException {
		GsInitialStateList initStates = (GsInitialStateList) graph.getObject(
				GsInitialStateManager.key, false);
		if (initStates != null && initStates.getNbElements(null) > 0) {
			GsInitStateTableModel model = new GsInitStateTableModel(nodeOrder, null, initStates, false);
			String[] t_cols = new String[len+1];
			for (int i=0 ; i<=len ; i++) {
				t_cols[i] = "";
			}
			doc.openTable("initialStates", "table", t_cols);
			doc.openTableRow(null);
			doc.openTableCell("Name");
			for (int i = 0; i < len; i++) {
				doc.openTableCell(""+nodeOrder.get(i));
			}
			for ( int i=0 ; i< initStates.getNbElements(null) ; i++ ) {
				doc.openTableRow(null);
				doc.openTableCell(""+model.getValueAt(i, 0));
				for (int j = 0; j < len; j++) {
					doc.openTableCell(""+model.getValueAt(i, j+2));
				}
			}
			doc.closeTable();
		}
	}

	private void writeLogicalFunctionsTable(boolean putcomment) throws IOException {
		if (specConfig.putComment) {
			doc.openTable(null, "table", new String[] { "", "", "", "" });
		} else {
			doc.openTable(null, "table", new String[] { "", "", "" });
		}
		doc.openTableRow(null);
		doc.openTableCell("ID", true);
		doc.openTableCell("Val", true);
		doc.openTableCell("Logical function", true);
		if (putcomment) {
			doc.openTableCell("Comment", true);
		}
		
		for (Iterator it=graph.getNodeOrder().iterator() ; it.hasNext() ;) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex)it.next();
			GsTreeInteractionsModel lfunc = vertex.getInteractionsModel();
			int nbval = 0;
			Object funcRoot = null;
			List[] t_val = new List[vertex.getMaxValue()+1];
			int nbrows = 0;
			if (lfunc != null) {
				funcRoot = lfunc.getRoot();
				nbval = lfunc.getChildCount(funcRoot);
				if (nbval == 0) {
					funcRoot = null;
				}
				// put all values from function
				for (int i=0 ; i<nbval ; i++) {
					GsTreeValue val = (GsTreeValue)lfunc.getChild(funcRoot, i);
					int v = val.getValue();
					if (lfunc.getChildCount(val) > 0) {
						t_val[v] = new ArrayList();
						t_val[v].add(val);
						nbrows++;
					}
				}
			}
			// and add logical parameters as well
			Iterator it_param = vertex.getV_logicalParameters().iterator(true);
			while (it_param.hasNext()) {
				GsLogicalParameter param = (GsLogicalParameter)it_param.next();
				if (!param.isDup()) {
					int v = param.getValue();
					if (t_val[v] == null) {
						t_val[v] = new ArrayList();
						nbrows++;
					}
					t_val[v].add(param);
				}
			}
			doc.openTableRow(null);
			doc.openTableCell(1, nbrows, vertex.getId(), true); //ID
			int currentValue = 0;
			if (nbrows > 0) {
				// TODO: put a "0" if nothing is defined
				for ( ; currentValue<t_val.length ; currentValue++) {
					if (t_val[currentValue] != null) {
						doWriteParameters(currentValue, t_val[currentValue], lfunc);
						break;
					}
				}
			} else {
				doc.openTableCell(null);//Values (empty)
				doc.openTableCell("no function");//function
			}
			if (putcomment) {
				doc.openTableCell(1,nbrows, null, false);
				writeAnnotation(vertex.getAnnotation());
			}
			doc.closeTableRow();
			
			// add the other functions
			if (nbrows > 1) {
				for (currentValue++ ; currentValue<t_val.length ; currentValue++) {
					if (t_val[currentValue] != null) {
						doc.openTableRow(null);
						doWriteParameters(currentValue, t_val[currentValue], lfunc);
						doc.closeTableRow();
					}
				}
			} 
		}		
		doc.closeTable();		
	}
	
	private void doWriteParameters(int value, List data, GsTreeInteractionsModel lfunc) throws IOException {
		doc.openTableCell(""+value); //Values
		doc.openTableCell(null); //logical function
		doc.openList("L1");
		for (Iterator it_all=data.iterator() ; it_all.hasNext() ; ) {
			Object o = it_all.next();
			if (o instanceof GsTreeValue) {
				int nbfunc = lfunc.getChildCount(o);
				for (int j=0 ; j<nbfunc ; j++) {
					Object func = lfunc.getChild(o, j);
					doc.openListItem(func.toString());
				}
			} else {
				doc.openListItem(o.toString());
			}
		}
		doc.closeList();
		doc.closeTableCell();
	}
	
	public void writeAnnotation(Annotation annotation) throws IOException {
		boolean hasLink = false;
		Iterator it = annotation.getLinkList().iterator();
		if (it.hasNext()) {
			hasLink = true;
			doc.openList("L1");
		}
		while (it.hasNext()) {
			AnnotationLink lnk = (AnnotationLink)it.next();
			String s_link;
			if (lnk.getHelper() != null) {
				s_link = lnk.getHelper().getLink(lnk.getProto(), lnk.getValue());
			} else {
				s_link = Tools.getLink(lnk.getProto(), lnk.getValue());
			}
			if (s_link == null) {
				doc.openListItem(null);
				doc.writeText(lnk.toString());
				doc.closeListItem();
			} else {
				if (s_link == lnk.toString() && s_link.length() >= 50) {
					doc.openListItem(null);
					doc.addLink(s_link, s_link.substring(0, 45) + "...");
					doc.closeListItem();
				} else {
					doc.openListItem(null);
					doc.addLink(s_link, lnk.toString());
					doc.closeListItem();
				}
			}
		}
		if (hasLink) {
			doc.closeList();
		}
		doc.openParagraph(null);
		String[] t = annotation.getComment().split("\n");
		for (int i = 0; i < t.length-1; i++) {
			doc.writeTextln(t[i]);
		}
		doc.writeText(t[t.length-1]);
		doc.closeParagraph();
	}

	/**
	 * import the javascript (DocumentExtra) from js file. 
	 * The javascript is use to allow the user to collapse/expand the stables states in the table.
	 * @throws IOException 
	 * 
	 */
	private void setJavascript() throws IOException {
		StringBuffer javascript = doc.getDocumentExtra("javascript");
		InputStream stream = Tools.getStreamForPath("/fr/univmrs/tagc/GINsim/ressources/makeStableStatesClickable.js");
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String s;
		while ((s = in.readLine()) != null) {
			javascript.append(s);
			javascript.append("\n");
		}
	}
	/**
	 * Set the style for the document.
	 */
	private void setDocumentStyles() {
		DocumentStyle styles = doc.getStyles();
		styles.addStyle("L1");
		styles.addProperty(DocumentStyle.LIST_TYPE, "U");	
		styles.addStyle("table");
		styles.addProperty(DocumentStyle.TABLE_BORDER, new Integer(1));	
	}

	/**
	 * Set the properties (meta-information) for the document.
	 */
	private void setDocumentProperties() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//for dc:date
		doc.setDocumentProperties(new String[] {
				//DocumentWriter.META_AUTHOR,
				DocumentWriter.META_DATE, simpleDateFormat.format(new Date()).toString(),
				//DocumentWriter.META_DESCRIPTION, 
				DocumentWriter.META_GENERATOR, "GINsim",
				//DocumentWriter.META_KEYWORDS,
				DocumentWriter.META_TITLE, graph.getGraphName()
		});
	}
  		

}

class DocumentExportConfig implements GsInitialStateStore {


	Map m_init = new HashMap();

	boolean exportInitStates = true;
	boolean exportMutants = true;
	boolean searchStableStates = true;
	boolean putComment = true;
	// set to true to avoid generating redondant things for multicellular models
	boolean multicellular = false;
	
	public Map getInitialState() {
		return m_init;
	}
	
}

class GDExportConfigPanel extends JPanel {
    private static final long serialVersionUID = 9043565812912568136L;
   
    
	protected GDExportConfigPanel (GsExportConfig config, StackDialog dialog) {
		DocumentExportConfig cfg = (DocumentExportConfig)config.getSpecificConfig();
		if (cfg == null) {
			cfg = new DocumentExportConfig();
			config.setSpecificConfig(cfg);
		}
    	GsInitialStatePanel initPanel = new GsInitialStatePanel(dialog, config.getGraph(), false);
    	initPanel.setParam(cfg);

    	setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = c.gridy = 0;
    	c.weightx = c.weighty = 1;
    	c.fill = GridBagConstraints.BOTH;
    	add(initPanel, c);
    }
}

/**
 * This class contain the informations 
 */
class GenericDocumentFormat extends GenericDocumentExport {
	/**
	 * Define a new generic document format.
	 * @param documentWriterClass : The DocumentWriter sub-class for the format
	 * @param id : The name of the format (for the dropdown menu)
	 * @param filter : an array of filter for the file extention the format can overwrite
	 * @param fillterDescr : a description
	 * @param extention : the extetion to add to the exported file
	 */
	public GenericDocumentFormat(Class documentWriterClass, String id, String[] filter, String filterDescr, String extension) {
		this.documentWriterClass = documentWriterClass;
		this.id = id;
		this.filter = filter;
		this.filterDescr = filterDescr;
		this.extension = extension;		
	}
	
	public GsPluggableActionDescriptor[] getT_action(int actionType, GsGraph graph) {
		return null;
	}
	
}