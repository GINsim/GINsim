package org.ginsim.gui.annotation.classes;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colomoto.biolqm.metadata.Annotator;
import org.colomoto.biolqm.metadata.annotations.Annotation;
import org.colomoto.biolqm.metadata.validations.PatternValidator;

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
	private boolean refreshing = false;

	JButton annotation_help = new JButton("?");
	JTextField annotation_field = new JTextField();
	JTextArea areaNotes = new JTextArea();
	ScrollablePanel annotation_panel = new ScrollablePanel();

	public AnnotationsComponent(Annotator<T> annotator) {
		this.annotator = annotator;
		this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		this.setLeftComponent(setupAnnotationPanel());
		this.setRightComponent(setupNoteArea());
		refresh();
	}

	private JPanel setupAnnotationPanel() {
		annotation_panel.setLayout(new GridBagLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		// Add the panel containing the list of annotations
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 3;
		gbc.weightx = gbc.weighty = 1;
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(annotation_panel);
		panel.add(sp, gbc);

		// Add a combo box and a text field to add new annotations
		gbc.insets = new Insets(2, 3, 2, 3);
		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = gbc.weighty = 0;
		panel.add(annotation_help, gbc);
		annotation_help.addActionListener(e -> show_help());

		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(annotation_field, gbc);

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
		List<Annotation> annots = annotator.annotations();
		if (annots != null) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.weightx = 1;
			gbc.gridy = 0;

			Annotation selected = this.annotator.getSelectedAnnotation();
			for (Annotation annot: annots) {
				ElementsPanel panel = new ElementsPanel(this, annot);
				panel.setSelection(selected);
				gbc.gridy++;
				this.annotation_panel.add(panel, gbc);
			}
		}
		refresh_annotation_field();
		this.validate();
	}

	public void onModel() {
		this.annotator.onModel();
		refresh();
	}

	public void onNode(T node) {
		this.annotator.node(node);
		refresh();
	}

	public void onEdge(T node1, T node2) {
		this.annotator.edge(node1, node2);
		refresh();
	}

	public void show_help() {
		// TODO: better menu, filter based on the text field
		JPopupMenu menu = new JPopupMenu();
		menu.add(populateAction("@"));
		menu.add(populateAction("@is"));
		Set<String> tags = annotator.availableTags();
		if (tags != null && tags.size() > 0) {
			menu.addSeparator();
			tags.stream().limit(4).forEach(t -> menu.add(populateAction("#"+t)));
		}
		Set<String> keys = annotator.availableKeys();
		if (keys != null && keys.size() > 0) {
			menu.addSeparator();
			keys.stream().limit(4).forEach(k -> menu.add(fillAction(k+"=")));
		}
		menu.addSeparator();
		menu.add(fillAction("pubmed:"));
		menu.add(fillAction("uniprot:"));

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
		Optional<String> qf = PatternValidator.asQualifier(txt);
		if (qf.isPresent()) {
			this.annotator.openBlock(qf.get());
			refresh();
			return true;
		}

		if (this.annotator.annotate(txt)) {
			this.annotation_field.setText("");
			refresh();
			return true;
		}
		return false;
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