package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.MbFanSearch;
import edu.cmu.tetrad.search.MbUtils;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Assumes that the search method of the fan search has been run and
 * shows the various options for Markov blanket DAGs consistent with
 * correlation information over the variables.
 *
 * @author Joseph Ramsey
 */
public class MbPatternDisplay extends JPanel {

    public MbPatternDisplay(final MbFanSearch search) {
        final List dags = MbUtils.generateMbDags(search.resultGraph(), false,
                search.getTest(), search.getDepth(), search.getTarget());

        if (dags.size() == 0) {
            JOptionPane.showMessageDialog(
                    JOptionUtils.centeringComp(),
                    "There are no consistent DAG's.");
            return;
        }

        Graph dag = (Graph) dags.get(0);
        final GraphWorkbench graphWorkbench =
                new GraphWorkbench(dag);

        final SpinnerNumberModel model =
                new SpinnerNumberModel(1, 1, dags.size(), 1);
        model.addChangeListener(new ChangeListener() {
            @Override
			public void stateChanged(ChangeEvent e) {
                int index = model.getNumber().intValue();
                graphWorkbench.setGraph(
                        (Graph) dags.get(index - 1));
            }
        });

        final JSpinner spinner = new JSpinner();
        JComboBox orient = new JComboBox(
                new String[]{"Orient --- only", "Orient ---, <->"});
        spinner.setModel(model);
        final JLabel totalLabel = new JLabel(" of " + dags.size());

        orient.setMaximumSize(orient.getPreferredSize());
        orient.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                String option = (String) box.getSelectedItem();

                if ("Orient --- only".equals(option)) {
                    List _dags = MbUtils.generateMbDags(search.resultGraph(), false,
                            search.getTest(), search.getDepth(), search.getTarget());
                    dags.clear();
                    dags.addAll(_dags);
                    final SpinnerNumberModel model =
                            new SpinnerNumberModel(1, 1,
                                    dags.size(), 1);
                    model.addChangeListener(new ChangeListener() {
                        @Override
						public void stateChanged(ChangeEvent e) {
                            int index =
                                    model.getNumber().intValue();
                            graphWorkbench.setGraph(
                                    (Graph) dags.get(index - 1));
                        }
                    });
                    spinner.setModel(model);
                    totalLabel.setText(" of " + dags.size());
                    graphWorkbench.setGraph((Graph) dags.get(0));
                }
                else if ("Orient ---, <->".equals(option)) {
                    List _dags = MbUtils.generateMbDags(search.resultGraph(), true,
                            search.getTest(), search.getDepth(), search.getTarget());
                    dags.clear();
                    dags.addAll(_dags);
                    final SpinnerNumberModel model =
                            new SpinnerNumberModel(1, 1,
                                    dags.size(), 1);
                    model.addChangeListener(new ChangeListener() {
                        @Override
						public void stateChanged(ChangeEvent e) {
                            int index =
                                    model.getNumber().intValue();
                            graphWorkbench.setGraph(
                                    (Graph) dags.get(index - 1));
                        }
                    });
                    spinner.setModel(model);
                    totalLabel.setText(" of " + dags.size());
                    graphWorkbench.setGraph((Graph) dags.get(0));
                }
            }
        });

        spinner.setPreferredSize(new Dimension(50, 20));
        spinner.setMaximumSize(spinner.getPreferredSize());
        Box b = Box.createVerticalBox();
        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Target = " + search.getTarget()));
        b1.add(Box.createHorizontalStrut(10));
        b1.add(orient);
        b1.add(Box.createHorizontalStrut(10));
        b1.add(Box.createHorizontalGlue());
        b1.add(new JLabel("DAG "));
        b1.add(spinner);
        b1.add(totalLabel);
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        JScrollPane jScrollPane = new JScrollPane(graphWorkbench);
        jScrollPane.setPreferredSize(new Dimension(400, 400));
        graphPanel.add(jScrollPane);
        graphPanel.setBorder(new TitledBorder("Markov Blank DAG"));
        b2.add(graphPanel);
        b.add(b2);

        setLayout(new BorderLayout());
        add(b, BorderLayout.CENTER);
    }
}
