package org.ginsim.gui.shell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.ginsim.commongui.utils.ImageLoader;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.GSServiceGUIManager;
import org.ginsim.gui.shell.actions.ImportAction;
import org.ginsim.gui.shell.callbacks.FileCallBack;
import org.ginsim.gui.shell.callbacks.HelpCallBack;

public class StartupDialog extends JFrame {
	private static final long serialVersionUID = 2935330158118845149L;

	private final JLabel message;
	
	public StartupDialog(boolean startup) {
		super("GINsim");
		setSize(600, 500);
		setMinimumSize(getSize());
		setResizable(false);
		setIconImage(ImageLoader.getImage("gsicon.png"));
		JPanel content = new JPanel(new GridBagLayout());
		setContentPane(content);
		
		JPanel header = new JPanel();
		Image logo = ImageLoader.getImage("banner.png");
		JLabel logoLabel = new JLabel(new ImageIcon( logo ));
		header.add(logoLabel);
		JLabel title; 
		if (startup) {
			title = new JLabel("Welcome to GINsim");
		}
		else {
			title = new JLabel("");
			 //title = new JLabel("Quit GINsim ?");
		}
		header.setBackground(Color.DARK_GRAY);
		title.setForeground(Color.WHITE);
		title.setFont(new Font("Serif", Font.BOLD, 28));
		header.add(title);

		GridBagConstraints cst = new GridBagConstraints();
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.anchor = GridBagConstraints.NORTH;
		cst.gridwidth = 3;
		cst.weightx = 1;
		content.add(header, cst);
		
		Insets insets = new Insets(5, 10, 5, 10);
		cst = new GridBagConstraints();
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.anchor = GridBagConstraints.NORTH;
		cst.gridwidth = 3;
		cst.weightx = 1;
		cst.gridy = 1;
		message = new JLabel();
		content.add(message, cst);
		
		JPanel panel = new JPanel( new GridBagLayout());
		panel.setBorder( BorderFactory.createTitledBorder("Start with ..."));
		cst = new GridBagConstraints();
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.gridy = 2;
		cst.weightx = 1;
		content.add(panel, cst);
		
		cst = new GridBagConstraints();
		cst.gridx = 0;
		cst.gridy = 0;
		cst.insets = insets;
		panel.add(new JButton( FileCallBack.getActionNew()), cst);
		
		cst = new GridBagConstraints();
		cst.gridx = 1;
		cst.gridy = 0;
		cst.insets = insets;
		panel.add(new JButton( FileCallBack.getActionOpen()), cst);
		
		
		// get import actions
		cst = new GridBagConstraints();
		cst.gridx = 2;
		cst.gridy = 0;
		cst.insets = insets;
		panel.add(new JButton(new ActionImport(this)), cst);
		
		panel = new JPanel( new GridBagLayout());
		panel.setBorder( BorderFactory.createTitledBorder("Recent files"));
		cst = new GridBagConstraints();
		cst.fill = GridBagConstraints.BOTH;
		cst.gridy = 3;
		cst.gridx = 0;
		cst.weightx = 1;
		cst.weighty = 1;
		content.add(panel, cst);
		
		int gridx = 0;
		int gridy = 0;
		Insets smallInset = new Insets(2, 2, 2, 2);
		for (Action action: FileCallBack.getActionsRecent()) {
			cst = new GridBagConstraints();
			cst.gridx = gridx++;
			cst.gridy = gridy;
			cst.insets = smallInset;
			cst.fill = GridBagConstraints.HORIZONTAL;
			if (gridx > 1) {
				gridy++;
				gridx = 0;
			}
			JButton bt = new JButton(action);
			bt.setToolTipText(action.getValue(Action.LONG_DESCRIPTION).toString());
			panel.add(bt, cst);
		}
		
		panel = new JPanel( new GridBagLayout());
		panel.setBorder( BorderFactory.createTitledBorder("Help"));
		cst = new GridBagConstraints();
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.gridy = 4;
		cst.gridx = 0;
		cst.weightx = 1;
		content.add(panel, cst);
		
		List<Action> actions = HelpCallBack.getActions();
		for (Action action: actions) {
			panel.add(new JButton( action));
		}
		
		cst = new GridBagConstraints();
		cst.gridy = 5;
		cst.gridx = 0;
		cst.anchor = GridBagConstraints.SOUTHEAST;
		content.add(new JButton( new ActionQuit(this)), cst);
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				close();
			}
		});
		setVisible(true);
	}

	public void close() {
		GUIManager.getInstance().closeStartupDialog();
		setVisible(false);
		dispose();
	}

	public void setMessage(String string) {
		message.setText(string);		
	}

}

class ActionImport extends AbstractAction {
	
	public ActionImport(StartupDialog dialog) {
		super("Import");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPopupMenu importMenu = new JPopupMenu();
		List<Action> actions = GSServiceGUIManager.getAvailableActions(null);
		for (Action action: actions) {
			if (action instanceof ImportAction) {
				importMenu.add(action);
			}
		}
		
		Object src = e.getSource();
		Component component;
		if (src instanceof Component) {
			component = (Component)src;
		} else {
			component = null;
		}
		importMenu.show(component, 0, component.getHeight());
	}
}


class ActionQuit extends AbstractAction {
	private final StartupDialog dialog;
	
	public ActionQuit(StartupDialog dialog) {
		super("Quit");
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.close();
	}
}
