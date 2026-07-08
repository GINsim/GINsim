package org.ginsim.servicegui.tool.avatar;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import org.colomoto.biolqm.StatefulLogicalModel;
import org.ginsim.common.application.LogManager;
import org.ginsim.commongui.dialog.GUIMessageUtils;
import org.ginsim.core.graph.GSGraphManager;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.gui.WhatToDoWithGraph;
import org.ginsim.service.tool.avatar.domain.AbstractStateSet;
import org.ginsim.service.tool.avatar.domain.Result;
import org.ginsim.service.tool.avatar.params.AvatarParameters;
import org.ginsim.service.tool.avatar.service.EnumAlgorithm;
import org.ginsim.service.tool.avatar.simulation.MDDUtils;
import org.ginsim.service.tool.avatar.simulation.Simulation;
import org.ginsim.service.tool.avatar.simulation.SimulationUtils;
import org.ginsim.service.tool.avatar.utils.NaturalOrderComparator;
import org.ginsim.servicegui.tool.avatar.others.IndexableActionListener;
import org.ginsim.servicegui.tool.avatar.parameters.AvatarParametersHelper;

/**
 * Facilities to run and kill simulations and display their results
 * 
 * @author Rui Henriques
 * @version 1.0
 */
public class AvatarResults {

	private Simulation sim;
	private JTextArea progress;
	private boolean quiet;
	private final AvatarConfigFrame parent;
	private final StatefulLogicalModel model;
	private File memFile, logFile, resFile, csvFile;
	private JButton brun, stop;

	private SwingWorker<Result, Void> simulationWorker;

	/**
	 * Creates the necessary context to run a simulation and display its results
	 * 
	 * @param _sim      the simulation
	 * @param _progress the component for posting updates during the execution of a
	 *                  simulation
	 * @param _parent   he parent panel
	 * @param _quiet    whether detailed logs are to be printed (default: true)
	 * @param _model    the stateful logical model possibly defining a set of
	 *                  initial
	 *                  states and oracles
	 * @param _memFile  the directory or file to save plots
	 * @param _logFile  the file to print logs
	 * @param _brun     button
	 * @param _csvFile  cvs file
	 * @param _resFile  resultat file
	 * @param _stop     button stop
	 */
	public AvatarResults(Simulation _sim, JTextArea _progress, final AvatarConfigFrame _parent, boolean _quiet,
			final StatefulLogicalModel _model, File _memFile, File _logFile, File _resFile, File _csvFile,
			JButton _brun, JButton _stop) {
		sim = _sim;
		progress = _progress;
		parent = _parent;
		quiet = _quiet;
		model = _model;
		memFile = _memFile;
		logFile = _logFile;
		resFile = _resFile;
		csvFile = _csvFile;
		brun = _brun;
		stop = _stop;
	}

	public void runAvatarResults() {
		try {
			sim.setComponents(progress);

			// Crée le SwingWorker
			simulationWorker = new SwingWorker<Result, Void>() {
				@Override
				protected Result doInBackground() throws Exception {
					return sim.run();
				}

				@Override
				protected void done() {
					try {
						if (isCancelled()) {
							progress.append("Simulation was interrupted!\n");
							progress.append("Re-run from the beginning \n");
						} else {
							Result res = get();
							progress.append("Simulation successfully computed!\n");

							if (parent.getPerturbation() != null)
								res.perturbation = parent.getPerturbation().toString();
							if (parent.getReduction() != null)
								res.reduction = parent.getReduction().toString();

							showOutputFrame(res);
						}
					} catch (Exception e) {
						String msg = (e.getMessage() != null) ? e.getMessage()
								: "Unknown error occurred while running the algorithm.";

						if (!msg.contains("FireFront requests")) {
							msg = "Unfortunately, we were not able to finish your request.<br>Exception while running the algorithm.<br><em>Reason:</em> "
									+ msg;
						}

						GUIMessageUtils.openErrorDialog(msg);
						// e.printStackTrace();
					}

					stop.setEnabled(false);
					brun.setEnabled(true);
				}
			};

			simulationWorker.execute(); // Démarre le thread de fond
			stop.setEnabled(true);
			brun.setEnabled(false);

		} catch (Exception e) {
			errorDisplay(
					"Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while running the algorithm.",
					e);
			stop.setEnabled(false);
			brun.setEnabled(true);
			// e.printStackTrace();
		}
	}

	/**
	 * Kill function
	 */
	public void togglePause() {
		if (simulationWorker != null && !simulationWorker.isDone()) {
			boolean isPaused = sim.isPaused();
			sim.setPaused(!isPaused);
		}
		stop.setText((sim.isPaused()) ? "Continue" : "Pause");
	}

	public void kill(boolean dialog) {
		sim.exit();
		if (simulationWorker != null && !simulationWorker.isDone()) {
			simulationWorker.cancel(true);
		}
	}

	/**
	 * Creates the graphical display of the results from an executed simulation
	 * (called after finishing 'runAvatarResults')
	 * 
	 * @param res the results to be displayed
	 */
	public void showOutputFrame(final Result res) {
		stop.setEnabled(false);
		brun.setEnabled(true);

		/** B: CREATE OUTPUT **/
		final JFrame output = new JFrame();
		output.setLayout(new GridBagLayout());
		output.setMinimumSize(new Dimension(590, 520));
		GridBagConstraints g = new GridBagConstraints();
		// int mheight = (int) Math.round(((double) res.charts.size()) / 2.0) * 19 + 1;
		final String htmlResult;

		try {
			/** C: plot results */
			JTextPane outText = new JTextPane();
			outText.setContentType("text/html");
			htmlResult = "<html>" + res.toHTMLString() + "</html>";
			outText.setText(htmlResult);
			// System.out.println(res.toHTMLString());
			// System.out.println("DONE");
			// outText.setAutoscrolls(true);
			// outText.setEditable(false);
			// outText.setCaretPosition(0);
			JScrollPane scrollPaneTutorial = new JScrollPane(outText);
			scrollPaneTutorial.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPaneTutorial.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			// scrollPaneTutorial.setPreferredSize(new Dimension(20000,300));
			scrollPaneTutorial.setBorder(BorderFactory.createTitledBorder("Output"));

			g.gridx = 0;
			g.gridy = 0;
			g.weightx = 1;
			g.gridheight = 1;
			g.weighty = 1;
			g.fill = GridBagConstraints.BOTH;
			// scrollPaneTutorial.setPreferredSize(new Dimension(560, 300));
			output.add(scrollPaneTutorial, g);
			g.weighty = 0;

			/** D: Draw plots **/
			JPanel panelPlot = new JPanel(new GridLayout(Math.max(1, res.charts.size()), 1));
			for (String title : res.charts.keySet()) {
				JButton button = new JButton(title);
				button.addActionListener(new IndexableActionListener(title, parent) {
					public void actionPerformed(ActionEvent arg0) {
						JLabel picLabel = new JLabel(new ImageIcon(res.charts.get(this.getKey())));
						int dialogResult = JOptionPane.showOptionDialog(output, picLabel, "Plotted chart",
								JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
								new String[] { "Save", "Close" }, "default");
						if (dialogResult == JOptionPane.YES_OPTION) {
							JFileChooser fcnet = new JFileChooser();
							fcnet.setFileSelectionMode(JFileChooser.FILES_ONLY);
							fcnet.setSelectedFile(memFile);
							int returnVal = fcnet.showSaveDialog(parent);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								memFile = fcnet.getSelectedFile();
								if (returnVal == 0)
									try {
										ImageIO.write(res.charts.get(this.getKey()), "png", memFile);
										// JOptionPane.showMessageDialog(output,
										// new JLabel("Your chart was successfully saved"), "Saving chart...",
										// JOptionPane.PLAIN_MESSAGE);
									} catch (IOException e) {
										JOptionPane.showMessageDialog(output, new JLabel(
												"Ops! We were not able to save your chart. Please contact the GINsim team!"),
												"Saving chart...", JOptionPane.ERROR_MESSAGE);
										e.printStackTrace();
									}
								// System.out.println(">>"+memorizedFile.getAbsolutePath());
							}
						}
					}
				});
				panelPlot.add(button);
			}
			// Claudine asked to disable Depth plots for MC
			if (res.charts.size() == 0 && !res.strategy.equals(EnumAlgorithm.MONTE_CARLO)) {
				JLabel label = new JLabel("    Plots were not generated (please select the option)!");
				panelPlot.add(label);
			}
			g.insets = new Insets(3, 3, 3, 3);
			g.gridx = 0;
			g.gridy = 1;
			g.weightx = 1;
			g.gridheight = 1;
			// plots.setPreferredSize(new Dimension(560, 60));
			output.add(panelPlot, g);

		} catch (Exception e) {
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while displaying the plots.";
			errorDisplay(fileErrorMessage, e);
			stop.setEnabled(false);
			brun.setEnabled(true);
			e.printStackTrace();
			return;
		}
		try {

			/** E: Draw complex attractors **/
			Map<String, AbstractStateSet> complexAttractors = res.complexAttractors;
			// mheight = (int) Math.round(((double) complexAttractors.size()) / 2.0) * 19 +
			// 1;

			GridLayout gl = new GridLayout(Math.max(1, complexAttractors.size() / 5),
					complexAttractors.size() < 5 ? 1 : 5, 10, 10);
			JPanel panelAtt = new JPanel(gl);
			panelAtt.setBorder(BorderFactory.createTitledBorder("Complex attractors"));

			if (complexAttractors.size() == 0) {
				JLabel label = new JLabel("    No complex attractors were found!");
				panelAtt.add(label);
			} else {

				// Sort CAs to create Buttons in alphabetical order
				List<String> lsCAs = new ArrayList<String>(complexAttractors.keySet());
				Collections.sort(lsCAs, new NaturalOrderComparator());
				for (String key : lsCAs) {
					JButton button = new JButton(key);
					button.addActionListener(new IndexableActionListener(key, parent) {
						public void actionPerformed(ActionEvent arg0) {
							// HierarchicalTransitionGraph graph =
							// GraphManager.getInstance().getNewGraph(HierarchicalTransitionGraph.class,model.getNodeOrder());
							// RegulatoryGraph graph =
							// GraphManager.getInstance().getNewGraph(RegulatoryGraph.class,model.getNodeOrder());
							// ReducedGraph graph =
							// GraphManager.getInstance().getNewGraph(ReducedGraph.class,model.getNodeOrder());
							DynamicGraph graph = GSGraphManager.getInstance().getNewGraph(DynamicGraph.class,
									model.getComponents());
							graph = SimulationUtils.getGraphFromAttractor(graph,
									res.complexAttractors.get(this.getKey()), model);
							// GUIManager.getInstance().whatToDoWithGraph(graph, true);
							Frame f = new WhatToDoWithGraph(graph);
							f.setVisible(true);
						}
					});
					panelAtt.add(button);
				}

				/** F: Save complex attractors **/
				// for(List<byte[]> att : res.complexAttractorPatterns.values())
				// System.out.println(">>"+AvatarUtils.toString(att));
				Map<String, List<byte[]>> newCAttractors = new HashMap<String, List<byte[]>>();
				for (String ckey : res.complexAttractorPatterns.keySet()) {
					List<byte[]> catt = res.complexAttractorPatterns.get(ckey);
					boolean newAttractor = !MDDUtils.contained(parent.statestore.oracles, catt);
					if (newAttractor)
						newCAttractors.put(ckey, res.complexAttractorPatterns.get(ckey));
				}
				parent.statestore.addOracle(newCAttractors);
				if (newCAttractors.size() > 0) {
					parent.states.updateParam(parent.statestore);
					AvatarParameters p = AvatarParametersHelper.load(parent);
					parent.setCurrent(p);
					parent.refresh(p);
				}
			}

			g.insets = new Insets(3, 3, 3, 3);
			g.gridx = 0;
			g.gridy = 2;
			g.weightx = 1;
			g.gridheight = 1;
			// attractors.setPreferredSize(new Dimension(560, 80));
			output.add(panelAtt, g);

		} catch (Exception e) {
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while displaying the complex attractors.";
			errorDisplay(fileErrorMessage, e);
			stop.setEnabled(false);
			brun.setEnabled(true);
			e.printStackTrace();
			return;
		}

		JPanel panelOthers = null;
		panelOthers = new JPanel(new GridLayout(1, (!quiet || res.strategy.equals(EnumAlgorithm.AVATAR)) ? 3 : 2));
		g.gridx = 0;
		g.gridy = 3;
		g.weightx = 1;
		g.gridheight = 1;
		output.add(panelOthers, g);

		try {
			/** F: View Log **/
			JButton logButton = new JButton("View Log");
			if (!quiet || res.strategy.equals(EnumAlgorithm.AVATAR)) {
				logButton.addActionListener(new IndexableActionListener("", parent) {
					public void actionPerformed(ActionEvent arg0) {
						JTextPane logText = new JTextPane();
						logText.setContentType("text/html");
						logText.setText("<html>" + res.logToHTMLString() + "</html>");
						logText.setEditable(false);
						logText.setCaretPosition(0);
						JScrollPane scrollLog = new JScrollPane(logText);
						scrollLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						scrollLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						scrollLog.setPreferredSize(new Dimension(700, 500));
						// scrollLog.setBorder(new TitledBorder(new LineBorder(purple,2), "Output",
						// TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
						int dialogResult = JOptionPane.showOptionDialog(output, scrollLog, "Log Messages",
								JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
								new String[] { "SAVE", "CLOSE" }, "default");
						if (dialogResult == JOptionPane.YES_OPTION) {
							JFileChooser fcnet = new JFileChooser();
							fcnet.setFileSelectionMode(JFileChooser.FILES_ONLY);
							fcnet.setSelectedFile(logFile);
							int returnVal = fcnet.showSaveDialog(parent);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								logFile = fcnet.getSelectedFile();
								if (returnVal == 0)
									try {
										BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
										writer.write(res.log);
										writer.close();
										// JOptionPane.showMessageDialog(output,
										// new JLabel("Your log was successfully saved"), "Saving log...",
										// JOptionPane.PLAIN_MESSAGE);
									} catch (IOException e) {
										JOptionPane.showMessageDialog(output, new JLabel(
												"Ops! We were not able to save your log. Please contact the GINsim team!"),
												"Saving log...", JOptionPane.ERROR_MESSAGE);
										e.printStackTrace();
									}
								// System.out.println(">>"+memorizedFile.getAbsolutePath());
							}
						}
					}
				});
			}
			if (false) { // !quiet || res.strategy.equals("Avatar")) {
				panelOthers.add(logButton);
			}
		} catch (Exception e) {
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while gathering or displaying the log.";
			errorDisplay(fileErrorMessage, e);
			stop.setEnabled(false);
			brun.setEnabled(true);
			e.printStackTrace();
			return;
		}

		try {
			/** SAVING **/
			JButton saveButton1 = new JButton("Save Results as .HTML");
			JButton saveButton2 = new JButton("Save Results as .CSV");

			panelOthers.add(saveButton1);
			panelOthers.add(saveButton2);

			saveButton1.addActionListener(new IndexableActionListener("", parent) {
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser fcnet = new JFileChooser();
					fcnet.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fcnet.setSelectedFile(resFile);
					int returnVal = fcnet.showSaveDialog(parent);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						resFile = fcnet.getSelectedFile();
						if (returnVal == 0)
							try {
								BufferedWriter writer = new BufferedWriter(new FileWriter(resFile));
								writer.write(htmlResult);
								writer.close();
								// JOptionPane.showMessageDialog(output, new JLabel("Your results was
								// successfully saved"),
								// "Saving results...", JOptionPane.PLAIN_MESSAGE);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(output, new JLabel(
										"Ops! We were not able to save your results. Please contact the GINsim team!"),
										"Saving results...", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
					}
				}
			});
			final String csvResult = res.toCSVString();
			saveButton2.addActionListener(new IndexableActionListener("", parent) {
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser fcnet = new JFileChooser();
					fcnet.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fcnet.setSelectedFile(csvFile);
					int returnVal = fcnet.showSaveDialog(parent);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						csvFile = fcnet.getSelectedFile();
						if (returnVal == 0)
							try {
								BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
								writer.write(csvResult);
								writer.close();
								// JOptionPane.showMessageDialog(output, new JLabel("Your results was
								// successfully saved"),
								// "Saving results...", JOptionPane.PLAIN_MESSAGE);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(output, new JLabel(
										"Ops! We were not able to save your results. Please contact the GINsim team!"),
										"Saving results...", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
					}
				}
			});
		} catch (Exception e) {
			String fileErrorMessage = "Unfortunately we were not able to finish your request.<br><em>Reason:</em> Exception while gathering or displaying the log.";
			errorDisplay(fileErrorMessage, e);
			stop.setEnabled(false);
			brun.setEnabled(true);
			e.printStackTrace();
			return;
		}

		/** D: Sizes and so forth **/
		// JOptionPane.showMessageDialog(this, output);
		JButton jButton1 = new JButton("Close");
		jButton1.addActionListener(new IndexableActionListener(output) {
			public void actionPerformed(ActionEvent e) {
				this.getFrame().dispose();
			}
		});

		g.gridy = 7;
		g.weightx = 0;
		g.gridheight = 0;
		g.ipadx = 10;
		g.anchor = GridBagConstraints.EAST;
		g.fill = GridBagConstraints.NONE;
		output.add(jButton1, g);

		output.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		output.setAlwaysOnTop(false);
		output.setVisible(true);
		// for(Frame f : Frame.getFrames()) System.out.println("#"+f.toString());
	}

	/**
	 * Displays a message of error whenever a simulation is not successfully
	 * executed
	 * 
	 * @param errorMessage the message to be display
	 * @param e            the exception whose stack trace is to be printed in the
	 *                     log of
	 *                     ginsim
	 */
	public static void errorDisplay(String errorMessage, Exception e) {
		JTextPane output = new JTextPane();
		output.setContentType("text/html");
		String errorFull = errorMessage + "\nException:\n" + e.getMessage() + "\n\n";
		for (StackTraceElement el : e.getStackTrace())
			errorFull += el.toString() + "\n";
		output.setText("<html>" + errorMessage + "</html>");// "<html>"+text+"</html>");
		if (!errorMessage.startsWith("FireFront")) {
			LogManager.debug(errorFull);
			LogManager.error(errorFull);
			LogManager.info(errorFull);
			LogManager.trace(errorFull);
		}
		output.setEditable(false);
		output.setCaretPosition(0);
		JScrollPane scrollPaneoutput = new JScrollPane(output);
		scrollPaneoutput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneoutput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneoutput.setPreferredSize(new Dimension(300, 100));
		JOptionPane.showMessageDialog(null, scrollPaneoutput, "Error", JOptionPane.INFORMATION_MESSAGE);
	}
}
