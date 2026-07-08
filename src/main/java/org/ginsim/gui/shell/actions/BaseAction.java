package org.ginsim.gui.shell.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.ginsim.common.application.Txt;
import org.ginsim.commongui.utils.ImageLoader;
import org.ginsim.gui.service.ServiceGUI;


/**
 * this class is used for all actions, it will lookup for an internationalized string
 * for the name and the tooltip
 */
public abstract class BaseAction extends AbstractAction {
	private static final long	serialVersionUID	= 6937495427962796865L;

	/**
	 * Image Icon getter
	 * @param name the name
	 * @return the image icon
	 */
	public static ImageIcon getIcon(String name) {
		if (name != null && !"".equals(name)) {
			return ImageLoader.getImageIcon(name);
		}
		return null;
	}

	private int weight = -1;

	/**
     * Constructor
     * @param name name of the action (menu entry)
     * @param icon for menu and toolbar
     * @param tooltip string for tool tips
     * @param accelerator (ie keyboard bytecut)
	 * @param serviceGUI  the service gui object
     */
	public BaseAction(String name,
			   ImageIcon icon,
			   String tooltip,
			   KeyStroke accelerator,
			   ServiceGUI serviceGUI) {
		this(name, icon, tooltip, accelerator, null, serviceGUI);
	}

	/**
	 * Constructor
	 * @param name the name string
	 * @param icon for menu and toolbar
	 * @param tooltip string for tool tips
	 * @param accelerator  (ie keyboard bytecut)
	 * @param mnemonic menomic integer
	 * @param serviceGUI  the service gui object
	 */
	public BaseAction(String name,
			   String icon,
			   String tooltip,
			   KeyStroke accelerator,
			   Integer mnemonic,
			   ServiceGUI serviceGUI) {
		this(name, getIcon(icon), tooltip, accelerator, mnemonic, serviceGUI);
	}

	/**
	 * Constructor
	 * @param name the name string
	 * @param icon for menu and toolbar
	 * @param tooltip string for tool tips
	 * @param accelerator  (ie keyboard bytecut)
	 * @param mnemonic menomic integer
	 * @param serviceGUI  the service gui object
	 */
	public BaseAction(String name,
			   ImageIcon icon,
			   String tooltip,
			   KeyStroke accelerator,
			   Integer mnemonic,
			   ServiceGUI serviceGUI) {

		String title = Txt.t(name);
		int idx = title.indexOf('/');
		if (idx > 0 && idx < title.length()-1) {
			String category = title.substring(0, idx);
			this.putValue("category", category);
			title = title.substring(idx+1);
		}
		this.putValue( Action.NAME, title);
		
		if (mnemonic != null) {
			this.putValue(Action.MNEMONIC_KEY, mnemonic);
		}
		if( icon != null ) {
			this.putValue( Action.SMALL_ICON, icon );
		}
		if (tooltip != null) {
			this.putValue( Action.SHORT_DESCRIPTION, Txt.t(tooltip) );
		}	   		 
		if (accelerator != null) {
			this.putValue( Action.ACCELERATOR_KEY, accelerator );
		}	 
		
		if (serviceGUI != null) {
			weight = serviceGUI.getWeight();
		}
	 }

	/**
	 * Weight getter
	 * @return the weight int
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Weight setter
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
