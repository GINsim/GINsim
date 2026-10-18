package org.ginsim.gui.annotation.classes;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.colomoto.biolqm.metadata.annotations.Annotation;
import org.colomoto.biolqm.metadata.annotations.URI;
import org.colomoto.biolqm.metadata.constants.Collection;
import org.ginsim.common.utils.OpenUtils;

/**
 * Display the content of a single (qualified) annotation block,
 * composed of collections of URIs, tags and key:value pairs.
 *
 * @author Martin Boutroux
 * @author Aurelien Naldi
 */
class ElementsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Color SELECTED = new Color(255, 240, 170);
	private static final Color NORMAL = Color.GRAY.brighter();
	private static final Color INVALID = new Color(255, 150, 150);
	private static final String UNSPECIFIED = "(unspecified)";
	private static final String HEADER_TIP = "<html>Annotation group: annotations sharing the same relation to this element<br>"
			+ "(e.g. is, isDescribedBy).<br>"
			+ "Click to select it as the target of new annotations, double-click to rename it.</html>";

	private static final Border SELECTED_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED, SELECTED, SELECTED);
	private static final Border NORMAL_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED, NORMAL, NORMAL);

	private final Annotation annotation;
	private final JPanel content = new JPanel();
	private final JButton header = new JButton();
	private final JTextField editor = new JTextField();
	private final JPanel headerCards = new JPanel(new CardLayout());

	ElementsPanel(AnnotationsComponent parent, Annotation annotation) {
		this.annotation = annotation;
		this.setBorder(NORMAL_BORDER);

		// prepare the header
		header.setBackground(NORMAL);
		header.setLayout(new GridBagLayout());
		header.setToolTipText(HEADER_TIP);
		header.addActionListener(e -> parent.selectBlock(annotation));
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					startEditing();
				}
			}
		});

		// inline editor for the qualifier name
		editor.setHorizontalAlignment(JTextField.CENTER);
		editor.setToolTipText("Enter to validate, Escape to cancel; leave empty for an unspecified qualifier");
		editor.addActionListener(e -> {
			String name = editor.getText().trim();
			if (name.startsWith("@")) {
				name = name.substring(1);
			}
			if (parent.renameBlock(annotation, name)) {
				return;
			}
			editor.setBackground(INVALID);
		});
		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					stopEditing();
				}
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				stopEditing();
			}
		});
		headerCards.add(header, "label");
		headerCards.add(editor, "edit");

		CircleButton b_delete = new CircleButton("x", CircleButton.DELETE);
		b_delete.setToolTipText("Delete this block");
		b_delete.addActionListener(e -> parent.removeBlock(annotation));

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		content.setLayout(new GridBagLayout());
		this.add(b_delete, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		this.add(headerCards, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(content, gbc);

		refresh();
	}

	private void refresh() {
		this.content.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2,2,2,2);
		gbc.anchor = GridBagConstraints.WEST;
		int c = 0;


		this.header.setText(isUnspecified() ? UNSPECIFIED : annotation.qualifier.term);

		for (URI uri: annotation.uris) {
			gbc.gridy = c++;
			content.add(ItemLabel.uri(this, uri), gbc);
		}
		for (Map.Entry<String,String> e: annotation.keyValues.entrySet()) {
			gbc.gridy = c++;
			content.add(ItemLabel.key_value(this, e.getKey(), e.getValue()), gbc);
		}
		for (String tag: annotation.tags) {
			gbc.gridy = c++;
			content.add(ItemLabel.tag(this, tag), gbc);
		}
		this.validate();
	}

	private boolean isUnspecified() {
		return annotation.qualifier == null || annotation.qualifier.term == null || annotation.qualifier.term.isBlank();
	}

	private void startEditing() {
		editor.setText(isUnspecified() ? "" : annotation.qualifier.term);
		editor.setBackground(Color.WHITE);
		((CardLayout) headerCards.getLayout()).show(headerCards, "edit");
		editor.requestFocusInWindow();
		editor.selectAll();
	}

	private void stopEditing() {
		((CardLayout) headerCards.getLayout()).show(headerCards, "label");
	}

	protected void removeItem(String value) {
		this.annotation.remove(value);
		this.refresh();
	}

	protected void setSelection(Annotation select) {
		if (this.annotation == select) {
			this.setBorder(SELECTED_BORDER);
			this.header.setBackground(SELECTED);
		} else {
			this.setBorder(NORMAL_BORDER);
			this.header.setBackground(NORMAL);
		}
	}
}

class ItemLabel extends JPanel {

	private static final Font FONT = new Font("Sans", Font.PLAIN, 12);
	private static final Color BACKGROUND = new Color(200, 200, 255);
	protected static ItemLabel tag(ElementsPanel parent, String tag) {
		return new ItemLabel(parent, null, "#"+tag);
	}

	protected static ItemLabel uri(ElementsPanel parent, URI uri) {
		Collection col = uri.getCollection();
		String link = uri.getLink();
		if (col == null) {
			return new ItemLabel(parent, link, uri.getValue());
		} else {
			return new ItemLabel(parent, link, col.name + ":" + uri.getValue());
		}
	}

	protected static ItemLabel key_value(ElementsPanel parent, String key, String value) {
		return new ItemLabel(parent, null, key + "=" + value);
	}

	private ItemLabel(ElementsPanel parent, String link, String text) {
		this.setLayout(new BorderLayout());
		this.setBorder(null);
		CircleButton b_remove = new CircleButton("x", CircleButton.DELETE);
		b_remove.addActionListener(actionEvent -> parent.removeItem(text));
		this.add(b_remove, BorderLayout.WEST);
		if (link == null) {
			JLabel label = new JLabel(text);
			label.setFont(FONT);
			label.setBackground(BACKGROUND);
			this.add(label);
		} else {
			JButton label = new JButton(text);
			label.setFont(FONT);
			label.setBackground(BACKGROUND);
			label.setForeground(Color.BLUE);
			label.setBorder(null);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.addActionListener(e -> {
				System.err.println("opening link: "+link);
				OpenUtils.openURI(link);
			});
			this.add(label);
		}
	}

}
