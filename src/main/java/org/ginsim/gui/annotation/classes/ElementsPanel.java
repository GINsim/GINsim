package org.ginsim.gui.annotation.classes;

import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;
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

	private final List<Annotation> annotations;
	private final AnnotationsComponent<?> owner;
	private final Annotation annotation;
	private final boolean singleListMode;
	private final JPanel content = new JPanel();
	private final JButton header = new JButton();
	private final JTextField editor = new JTextField();
	private final JPanel headerCards = new JPanel(new CardLayout());

	ElementsPanel(AnnotationsComponent parent, Annotation annotation) {
		this(parent, Collections.singletonList(annotation), false);
	}

	ElementsPanel(AnnotationsComponent parent, List<Annotation> annotations, boolean singleListMode) {
		this.owner = parent;
		this.annotations = annotations;
		this.annotation = annotations.isEmpty() ? null : annotations.get(0);
		this.singleListMode = singleListMode;
		if (!singleListMode) {
			this.setBorder(NORMAL_BORDER);
		}

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
		if (!singleListMode) {
			this.add(b_delete, gbc);
			gbc.gridx++;
			gbc.weightx = 1;
			this.add(headerCards, gbc);
		}
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = singleListMode ? 1 : 2;
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


		if (!singleListMode) {
			this.header.setText(isUnspecified() ? UNSPECIFIED : annotation.qualifier.term);
		}

		int uriIndex = 1;
		for (Annotation source: annotations) {
			for (URI uri: source.uris) {
				gbc.gridy = c++;
				content.add(ItemLabel.uri(this, source, uri, singleListMode ? uriIndex : 0), gbc);
				uriIndex++;
			}
			for (Map.Entry<String,String> e: source.keyValues.entrySet()) {
				gbc.gridy = c++;
				content.add(ItemLabel.key_value(this, source, e.getKey(), e.getValue()), gbc);
			}
			for (String tag: source.tags) {
				gbc.gridy = c++;
				content.add(ItemLabel.tag(this, source, tag), gbc);
			}
		}
		this.validate();
	}

	void updateSelection(Annotation source, Object item) {
		for (Component component: content.getComponents()) {
			if (component instanceof ItemLabel) {
				((ItemLabel) component).updateSelection(source, item);
			}
		}
	}

	AnnotationsComponent<?> getOwner() {
		return owner;
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

	protected void setSelection(Annotation select) {
		if (this.annotation == select) {
			this.setBorder(SELECTED_BORDER);
			this.header.setBackground(SELECTED);
		} else {
			this.setBorder(NORMAL_BORDER);
			this.header.setBackground(NORMAL);
		}
	}

	protected void removeItem(Annotation source, String value) {
		source.remove(value);
		this.refresh();
	}
}

class ItemLabel extends JPanel {

	private static final Font FONT = new Font("Sans", Font.PLAIN, 12);
	private static final Color BACKGROUND = new Color(200, 200, 255);
	private static final Color SELECTED_BACKGROUND = new Color(255, 240, 170);
	private final AnnotationsComponent<?> owner;
	private final Annotation source;
	private final Object item;
	private final AnnotationsComponent.ItemKind kind;
	private final String key;
	private final String value;
	private final JComponent valueComponent;
	private final JTextField editField = new JTextField();
	private final JLabel indexLabel;
	private boolean selected;
	private boolean editing;

	protected static ItemLabel tag(ElementsPanel parent, Annotation source, String tag) {
		return new ItemLabel(parent, source, tag, AnnotationsComponent.ItemKind.TAG, null, "#"+tag, null, 0);
	}

	protected static ItemLabel uri(ElementsPanel parent, Annotation source, URI uri, int index) {
		Collection col = uri.getCollection();
		String link = uri.getLink();
		String value = col == null ? uri.getValue() : col.name + ":" + uri.getValue();
		return new ItemLabel(parent, source, uri, AnnotationsComponent.ItemKind.URI, null, value, link, index);
	}

	protected static ItemLabel key_value(ElementsPanel parent, Annotation source, String key, String value) {
		return new ItemLabel(parent, source, key, AnnotationsComponent.ItemKind.KEY_VALUE, key, value, null, 0);
	}

	private ItemLabel(ElementsPanel parent, Annotation source, Object item, AnnotationsComponent.ItemKind kind, String key, String value, String link, int index) {
		this.owner = parent.getOwner();
		this.source = source;
		this.item = item;
		this.kind = kind;
		this.key = key;
		this.value = value;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		indexLabel = new JLabel(index > 0 ? "[" + index + "]" : "");
		indexLabel.setPreferredSize(new Dimension(42, 20));
		indexLabel.setHorizontalAlignment(SwingConstants.LEFT);
		indexLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		indexLabel.setForeground(Color.DARK_GRAY);
		if (index > 0) {
			this.add(indexLabel, BorderLayout.WEST);
	}
		if (!owner.isSingleListMode()) {
			CircleButton b_remove = new CircleButton("x", CircleButton.DELETE);
			b_remove.addActionListener(e -> parent.removeItem(source, removalText()));
			this.add(b_remove, BorderLayout.WEST);
		}
		if (link == null) {
			JLabel label = new JLabel(value);
			label.setFont(FONT);
			label.setBackground(BACKGROUND);
			valueComponent = label;
			this.add(label, BorderLayout.CENTER);
		} else {
			JLabel label = new JLabel(value);
			label.setFont(FONT);
			label.setBackground(BACKGROUND);
			label.setForeground(Color.BLUE);
			label.setCursor(Cursor.getDefaultCursor());
			valueComponent = label;
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (isOnLinkText(label, e.getX())) {
						OpenUtils.openURI(link);
					} else if (e.getClickCount() == 2) {
						startEdit();
					} else {
						select();
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					label.setCursor(Cursor.getDefaultCursor());
				}
			});
			label.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					label.setCursor(Cursor.getPredefinedCursor(isOnLinkText(label, e.getX()) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
				}
			});
			this.add(label, BorderLayout.CENTER);
		}
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				backgroundClick(e);
			}
		});
		editField.setFont(FONT);
		editField.setText(value);
		editField.addActionListener(e -> commitEdit());
		editField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancelEdit();
				}
			}
		});
		indexLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				backgroundClick(e);
			}
		});
		if (link == null) {
			valueComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					backgroundClick(e);
				}
			});
		}
		updateSelection(null, null);
	}

	private boolean isOnLinkText(JLabel label, int x) {
		return x >= label.getInsets().left && x <= label.getInsets().left + label.getFontMetrics(label.getFont()).stringWidth(value);
	}

	private void select() {
		if (owner.isSingleListMode()) {
			owner.selectItem(source, item, kind, key, value);
		}
	}

	private void backgroundClick(MouseEvent event) {
		if (event.getClickCount() == 2) {
			startEdit();
		} else {
			select();
		}
	}

	private void startEdit() {
		if (!owner.isSingleListMode() || editing) {
			return;
		}
		editing = true;
		editField.setText(value);
		editField.setBackground(Color.WHITE);
		remove(valueComponent);
		add(editField, BorderLayout.CENTER);
		revalidate();
		repaint();
		editField.requestFocusInWindow();
		editField.selectAll();
	}

	private void commitEdit() {
		if (owner.editItem(source, item, kind, key, value, editField.getText())) {
			return;
		}
		editField.setBackground(new Color(255, 150, 150));
		editField.requestFocusInWindow();
	}

	private void cancelEdit() {
		if (!editing) {
			return;
		}
		editing = false;
		remove(editField);
		add(valueComponent, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private String removalText() {
		if (kind == AnnotationsComponent.ItemKind.KEY_VALUE) {
			return key + "=" + value;
		}
		return value;
	}

	void updateSelection(Annotation source, Object item) {
		selected = this.source == source && this.item == item;
		Color background = selected ? SELECTED_BACKGROUND : getParent() instanceof JPanel ? getParent().getBackground() : Color.WHITE;
		setBackground(background);
		indexLabel.setBackground(background);
		indexLabel.setOpaque(selected);
		valueComponent.setBackground(selected ? SELECTED_BACKGROUND : BACKGROUND);
		valueComponent.setOpaque(true);
	}

}
