package org.ginsim.gui.utils.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;



public class PropertyActionButton extends JButton 
	implements ObjectPropertyEditorUI, ActionListener {
	private static final long	serialVersionUID	= -2434800129909268912L;
	
	GenericPropertyInfo pinfo;
	
	public PropertyActionButton() {
		addActionListener(this);
		setBorder(BorderFactory.createEtchedBorder());
	}
	
	@Override
	public void apply() {
	}
	@Override
	public void release() {
	}
	
	@Override
	public void refresh(boolean force) {
		setText(pinfo.getStringValue());
	}

	@Override
	public void setEditedProperty(GenericPropertyInfo pinfo,
			GenericPropertyHolder panel) {
		this.pinfo = pinfo;
		int pos = 0;
		if (pinfo.name != null) {
			panel.addField(new JLabel(pinfo.name), pinfo, pos);
			pos++;
		}
		panel.addField(this, pinfo, pos);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pinfo.run();
	}
}
