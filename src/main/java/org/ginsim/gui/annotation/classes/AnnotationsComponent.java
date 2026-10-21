package org.ginsim.gui.annotation.classes;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.biolqm.metadata.annotations.Annotation;
import org.colomoto.biolqm.metadata.annotations.URI;
import org.colomoto.biolqm.metadata.validations.PatternValidator;
import org.ginsim.gui.utils.widgets.StockButton;

import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Produces a GUI to modify the metadata of an object (model, nodes, annotations with nested parts...)
 *
 * @author Martin Boutroux
 */
public class AnnotationsComponent<T> extends JSplitPane implements KeyListener, DocumentListener {
	private static final long serialVersionUID = 1L;
	private static final Color BG_TAG = new Color(170, 170, 255);
	private static final Color BG_QUALIFIER = new Color(200, 255, 150);
	private static final Color BG_INVALID = new Color(255, 150, 150);

	private final Annotator<T> annotator;
	private final boolean singleListMode;
	private boolean refreshing = false;
	private AnnotationItem selectedItem;
	private ElementsPanel singleListPanel;

	JButton deleteItemButton = new StockButton("edit-delete.png", true);
	JButton moveUpButton = new StockButton("go-up.png", true);
	JButton moveDownButton = new StockButton("go-down.png", true);
	JButton annotation_help = new JButton("?");
	JTextField annotation_field = new JTextField();
	JTextArea areaNotes = new JTextArea();
	ScrollablePanel annotation_panel = new ScrollablePanel();

	public AnnotationsComponent(Annotator<T> annotator) {
		this(annotator, false);
	}

	public AnnotationsComponent(Annotator<T> annotator, boolean singleListMode) {
		this.annotator = annotator;
		this.singleListMode = singleListMode;
		this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		this.setLeftComponent(setupAnnotationPanel());
		this.setRightComponent(setupNoteArea());
		refresh();
	}

	private JPanel setupAnnotationPanel() {
		annotation_panel.setLayout(new GridBagLayout());
		deleteItemButton.setToolTipText("Delete selected annotation item");
		moveUpButton.setToolTipText("Move selected link up");
		moveDownButton.setToolTipText("Move selected link down");
		deleteItemButton.setEnabled(false);
		moveUpButton.setEnabled(false);
		moveDownButton.setEnabled(false);
		deleteItemButton.setVisible(singleListMode);
		moveUpButton.setVisible(singleListMode);
		moveDownButton.setVisible(singleListMode);
		deleteItemButton.addActionListener(e -> deleteSelectedItem());
		moveUpButton.addActionListener(e -> moveSelectedLink(-1));
		moveDownButton.addActionListener(e -> moveSelectedLink(1));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		// Add the panel containing the list of annotations
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 6;
		gbc.weightx = gbc.weighty = 1;
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(annotation_panel);
		panel.add(sp, gbc);

		// Add a combo box and a text field to add new annotations
		gbc.insets = new Insets(2, 3, 2, 3);
		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = gbc.weighty = 0;
		panel.add(deleteItemButton, gbc);
		gbc.gridx++;
		panel.add(moveUpButton, gbc);
		gbc.gridx++;
		panel.add(moveDownButton, gbc);
		gbc.gridx++;
		panel.add(annotation_help, gbc);
		annotation_help.addActionListener(e -> show_help());

		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(annotation_field, gbc);

		((AbstractDocument) annotation_field.getDocument()).setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
				replace(fb, offset, 0, text, attrs);
			}

			@Override
			public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
				replace(fb, offset, length, "", null);
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				String current = fb.getDocument().getText(0, fb.getDocument().getLength());
				String inserted = text == null ? "" : text;
				String candidate = current.substring(0, offset) + inserted + current.substring(offset + length);
				String normalized = normalizeIdentifierPrefix(candidate);
				if (candidate.equals(normalized)) {
					fb.replace(offset, length, inserted, attrs);
					return;
				}

				int oldCaret = offset + inserted.length();
				int oldPrefixLength = candidate.regionMatches(true, 0, "pmid:", 0, 5) ? 5
						: candidate.regionMatches(true, 0, "pubmedid:", 0, 9) ? 9
						: candidate.regionMatches(true, 0, "hugo:", 0, 5) ? 5 : 0;
				int newPrefixLength = candidate.regionMatches(true, 0, "pmid:", 0, 5) ? 7
						: candidate.regionMatches(true, 0, "pubmedid:", 0, 9) ? 7
						: candidate.regionMatches(true, 0, "hugo:", 0, 5) ? 5 : 0;
				fb.replace(0, fb.getDocument().getLength(), normalized, attrs);
				int caret = oldCaret < oldPrefixLength ? newPrefixLength : oldCaret + newPrefixLength - oldPrefixLength;
				SwingUtilities.invokeLater(() -> annotation_field.setCaretPosition(Math.min(caret, annotation_field.getText().length())));
			}
		});
		annotation_field.addKeyListener(this);
		annotation_field.getDocument().addDocumentListener(this);

		panel.setMinimumSize(new Dimension(400, 300));
		return panel;
	}

	private JScrollPane setupNoteArea() {
		areaNotes.setLineWrap(true);
		areaNotes.setWrapStyleWord(true);

		areaNotes.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {}

			@Override
			public void focusLost(FocusEvent e) {
				annotator.setNotes(areaNotes.getText());
				areaNotes.setBackground(Color.WHITE);
			}
		});
        areaNotes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePanel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            	updatePanel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            	updatePanel();
            }

            private void updatePanel() {
            	if (refreshing) {
            		return;
            	}
            	annotator.setNotes(areaNotes.getText());
            	areaNotes.setBackground(Color.ORANGE);
            }
        });
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(areaNotes);
		return sp;
	}

	public void refresh() {
		// Fill the notes area
		String notes = annotator.getNotes();
		if (notes == null) {
			notes = "";
		}
		refreshing = true;
		try {
			areaNotes.setText(notes);
		} finally {
			refreshing = false;
		}
		areaNotes.setBackground(Color.WHITE);

		// Rebuild the annotation blocks
		this.annotation_panel.removeAll();
		singleListPanel = null;
		List<Annotation> annots = annotator.annotations();
		if (annots != null) {
			normalizeIdentifierAliases(annots);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.weightx = 1;
			gbc.gridy = 0;

			if (singleListMode) {
				if (!annots.isEmpty()) {
					singleListPanel = new ElementsPanel(this, annots, true);
					this.annotation_panel.add(singleListPanel, gbc);
					if (selectedItem != null) {
						singleListPanel.updateSelection(selectedItem.source, selectedItem.item);
					}
				}
			} else {
				Annotation selected = this.annotator.getSelectedAnnotation();
				for (Annotation annot: annots) {
					ElementsPanel panel = new ElementsPanel(this, annot);
					panel.setSelection(selected);
					gbc.gridy++;
					this.annotation_panel.add(panel, gbc);
				}
			}
		}
		refresh_annotation_field();
		updateItemControls();
		this.validate();
	}

	private void normalizeIdentifierAliases(List<Annotation> annotations) {
		for (Annotation annotation: annotations) {
			for (int i = 0; i < annotation.uris.size(); i++) {
				URI uri = annotation.uris.get(i);
				String prefix = uri.getCollection() == null ? null : uri.getCollection().name;
				String value = uri.getValue();
				if (prefix == null) {
					int separator = value.indexOf(':');
					if (separator > 0) {
						prefix = value.substring(0, separator);
					}
				}
				String canonical = canonicalIdentifierPrefix(prefix);
				if (canonical == null) {
					continue;
				}
				if (uri.getCollection() == null) {
					value = value.substring(value.indexOf(':') + 1);
				}
				annotation.uris.set(i, new URI(annotator.availableCollections().getCollection(canonical), value));
			}
		}
	}

	private static String canonicalIdentifierPrefix(String prefix) {
		if (prefix == null) {
			return null;
		}
		switch (prefix.toLowerCase(java.util.Locale.ROOT)) {
			case "pmid":
		case "pubmedid":
			return "pubmed";
		case "hugo":
			return "hgnc";
		default:
			return null;
		}
	}

	void selectItem(Annotation source, Object item, ItemKind kind, String key, String value) {
		if (selectedItem != null && selectedItem.source == source && selectedItem.item == item) {
			selectedItem = null;
		} else {
		selectedItem = new AnnotationItem(source, item, kind, key, value);
		}
		if (singleListPanel != null) {
			singleListPanel.updateSelection(selectedItem == null ? null : source, selectedItem == null ? null : item);
		}
		updateItemControls();
	}

	boolean isSelected(Annotation source, Object item) {
		return selectedItem != null && selectedItem.source == source && selectedItem.item == item;
	}

	boolean isSingleListMode() {
		return singleListMode;
	}

	private void deleteSelectedItem() {
		if (selectedItem == null) {
			return;
		}
		switch (selectedItem.kind) {
			case URI:
				removeUriByIdentity(selectedItem.source, selectedItem.item);
				break;
			case TAG:
				selectedItem.source.tags.remove(selectedItem.item);
				break;
			case KEY_VALUE:
				selectedItem.source.keyValues.remove(selectedItem.key, selectedItem.value);
				break;
		}
		selectedItem = null;
		refresh();
	}

	private void moveSelectedLink(int offset) {
		if (selectedItem == null || selectedItem.kind != ItemKind.URI) {
			return;
		}
		int index = findUriByIdentity(selectedItem.source, selectedItem.item);
		int target = index + offset;
		if (index < 0 || target < 0 || target >= selectedItem.source.uris.size()) {
			return;
		}
		java.util.Collections.swap(selectedItem.source.uris, index, target);
		refresh();
	}

	private void removeUriByIdentity(Annotation source, Object uri) {
		int index = findUriByIdentity(source, uri);
		if (index >= 0) {
			source.uris.remove(index);
		}
	}

	private int findUriByIdentity(Annotation source, Object uri) {
		for (int i = 0; i < source.uris.size(); i++) {
			if (source.uris.get(i) == uri) {
				return i;
			}
		}
		return -1;
	}

	private void updateItemControls() {
		boolean hasSelection = selectedItem != null;
		deleteItemButton.setEnabled(hasSelection);
		int linkIndex = -1;
		int linkCount = 0;
		if (hasSelection && selectedItem.kind == ItemKind.URI) {
			linkIndex = findUriByIdentity(selectedItem.source, selectedItem.item);
			linkCount = selectedItem.source.uris.size();
		}
		moveUpButton.setEnabled(linkIndex > 0);
		moveDownButton.setEnabled(linkIndex >= 0 && linkIndex < linkCount - 1);
	}

	enum ItemKind { URI, TAG, KEY_VALUE }

	private static final class AnnotationItem {
		final Annotation source;
		final Object item;
		final ItemKind kind;
		final String key;
		final String value;

		AnnotationItem(Annotation source, Object item, ItemKind kind, String key, String value) {
			this.source = source;
			this.item = item;
			this.kind = kind;
			this.key = key;
			this.value = value;
		}
	}

	public void onModel() {
		selectedItem = null;
		this.annotator.onModel();
		refresh();
	}

	public void onNode(T node) {
		selectedItem = null;
		this.annotator.node(node);
		refresh();
	}

	public void onEdge(T node1, T node2) {
		selectedItem = null;
		this.annotator.edge(node1, node2);
		refresh();
	}

	public void show_help() {
		// TODO: better menu, filter based on the text field
		JPopupMenu menu = new JPopupMenu();
		if (!singleListMode) {
			menu.add(populateAction("@"));
			menu.add(populateAction("@is"));
			menu.addSeparator();
		}
		Set<String> tags = annotator.availableTags();
		if (tags != null && tags.size() > 0) {
			tags.stream().limit(4).forEach(t -> menu.add(populateAction("#"+t)));
			menu.addSeparator();
		}
		Set<String> keys = annotator.availableKeys();
		if (keys != null && keys.size() > 0) {
			menu.addSeparator();
			keys.stream().limit(4).forEach(k -> menu.add(fillAction(k+"=")));
			menu.addSeparator();
		}
		menu.add(fillAction("uniprot:"));
		menu.add(fillAction("hgnc:"));
		menu.add(fillAction("pdb:"));
		menu.add(fillAction("pubmed:"));
		menu.add(fillAction("doi:"));

		menu.show(annotation_field, 0, 0);
	}

	public void selectBlock(Annotation annot) {
		// Skip the rebuild when already selected, it would destroy the header being double-clicked
		if (annot == this.annotator.getSelectedAnnotation()) {
			return;
		}
		if (this.annotator.selectBlock(annot)) {
			refresh();
		}
	}

	public boolean renameBlock(Annotation annot, String name) {
		if (!this.annotator.renameBlock(annot, name)) {
			return false;
		}
		refresh();
		return true;
	}

	public void removeBlock(Annotation annot) {
		if (this.annotator.removeBlock(annot)) {
			refresh();
		}
	}

	private Action populateAction(String txt) {
		return new AbstractAction(txt) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				validateAnnotation(txt);
			}
		};
	}

	private Action fillAction(String txt) {
		return new AbstractAction(txt) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				annotation_field.setText(txt);
				annotation_field.grabFocus();
			}
		};
	}


	@Override
	public void keyTyped(KeyEvent keyEvent) {
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		switch (keyEvent.getKeyCode()) {
			case KeyEvent.VK_UP:
				this.annotator.selectPrevious();
				refresh();
				break;
			case KeyEvent.VK_DOWN:
				this.annotator.selectNext();
				refresh();
				break;
			case KeyEvent.VK_ENTER:
				if (validateAnnotation(this.annotation_field.getText())) {
					this.annotation_field.setText("");
				}
				break;
		}
	}

	private boolean validateAnnotation(String txt) {
		txt = normalizeIdentifierPrefix(txt.trim());
		Optional<String> qf = PatternValidator.asQualifier(txt);
		if (qf.isPresent()) {
			if (singleListMode) {
				return false;
			}
			this.annotator.openBlock(qf.get());
			refresh();
			return true;
		}

		if (singleListMode) {
			this.annotator.openBlock("");
		}
		if (this.annotator.annotate(txt)) {
			this.annotation_field.setText("");
			refresh();
			return true;
		}
		return false;
	}

	private static String normalizeIdentifierPrefix(String text) {
		if (text.regionMatches(true, 0, "pmid:", 0, 5)) {
			return "pubmed:" + text.substring(5);
		}
		if (text.regionMatches(true, 0, "pubmedid:", 0, 9)) {
			return "pubmed:" + text.substring(9);
		}
		if (text.regionMatches(true, 0, "hugo:", 0, 5)) {
			return "hgnc:" + text.substring(5);
		}
		return text;
	}

	protected void refresh_annotation_field() {
		switch (PatternValidator.validate(this.annotation_field.getText())) {
			case EMPTY:
				annotation_field.setBackground(Color.WHITE);
				break;
			case TAG:
			case COLLECTION:
			case KEY_VALUE:
				annotation_field.setBackground(BG_TAG);
				break;
			case QUALIFIER:
				annotation_field.setBackground(BG_QUALIFIER);
				break;
			case INVALID:
				annotation_field.setBackground(BG_INVALID);
				break;
		}
		annotation_field.validate();
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {

	}

	@Override
	public void insertUpdate(DocumentEvent documentEvent) {
		refresh_annotation_field();
	}

	@Override
	public void removeUpdate(DocumentEvent documentEvent) {
		refresh_annotation_field();
	}

	@Override
	public void changedUpdate(DocumentEvent documentEvent) {
		refresh_annotation_field();
	}
}

class ScrollablePanel extends JPanel implements Scrollable {

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
		return 20;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle rectangle, int i, int i1) {
		return 50;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}