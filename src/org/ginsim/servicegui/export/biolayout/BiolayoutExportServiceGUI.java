package org.ginsim.servicegui.export.biolayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.exception.GsException;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.common.ExportAction;
import org.ginsim.gui.service.common.GUIFor;
import org.ginsim.gui.service.common.ServiceStatus;
import org.ginsim.service.export.biolayout.BioLayoutExportService;
import org.mangosdk.spi.ProviderFor;


/**
 * Export service to biolayout format
 * 
 */
@ProviderFor( ServiceGUI.class)
@GUIFor( BioLayoutExportService.class)
@ServiceStatus( ServiceStatus.RELEASED)
public class BiolayoutExportServiceGUI implements ServiceGUI {
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		
		List<Action> actions = new ArrayList<Action>();
		actions.add( new ExportBioLayoutAction( graph));
		return actions;
	}

	@Override
	public int getWeight() {
		return W_GENERIC + 2;
	}
}


/**
 * Export to Biolayout file Action
 * 
 * @author spinelli
 *
 */
class ExportBioLayoutAction extends ExportAction {

	static final FileFormatDescription FORMAT = new FileFormatDescription("biolayout", "layout");
	
	public ExportBioLayoutAction( Graph graph) {
		
		super( graph, "STR_biolayout", "STR_biolayout_descr");
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return FORMAT;
	}

	@Override
	protected void doExport(String filename) throws GsException, IOException {
		
		BioLayoutExportService service = ServiceManager.getManager().getService( BioLayoutExportService.class);
		
		if( service != null){
			service.run( graph, null, filename);
		}
		else{
			throw new GsException( GsException.GRAVITY_ERROR, "No BioLayoutExportService service available");
		}
	}
}
