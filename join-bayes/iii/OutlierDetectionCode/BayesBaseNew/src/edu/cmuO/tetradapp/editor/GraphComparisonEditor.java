package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetradapp.model.GraphComparison;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Provides a little display/editor for notes in the session workbench. This
 * may be elaborated in the future to allow marked up text.
 *
 * @author Joseph Ramsey
 * @version $Revision$ $Date$
 */
public class GraphComparisonEditor extends JPanel {

    /**
     * The model for the note.
     */
    private GraphComparison comparison;


    /**
     * Constructs the editor given the model
     *
     * @param comparison
     */
    public GraphComparisonEditor(GraphComparison comparison) {
        this.comparison = comparison;
        setup();
    }

    //============================ Private Methods =========================//


    private boolean isLegal(String text) {
//        if (!NamingProtocol.isLegalName(text)) {
//            JOptionPane.showMessageDialog(this, NamingProtocol.getProtocolDescription() + ": " + text);
//            return false;
//        }
        return true;
    }


    private void setup() {
        StringBuffer buf = new StringBuffer();

        buf.append("\nEdges added:");

        if (comparison.getEdgesAdded().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            for (Edge edge : comparison.getEdgesAdded()) {
                buf.append("\n<> ====> ").append(edge);
            }
        }

        buf.append("\n\nEdges reoriented:");

        Graph referenceGraph = comparison.getReferenceGraph();

        if (comparison.getEdgesReorientedFrom().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            for (int i = 0; i < comparison.getEdgesReorientedFrom().size(); i++) {
                Edge from = comparison.getEdgesReorientedFrom().get(i);
                Edge to = comparison.getEdgesReorientedTo().get(i);
                buf.append("\n").append(from).append(" ====> ").append(to);
            }
        }

        buf.append("\n\nEdge removed:");

        if (comparison.getEdgesRemoved().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            for (Edge edge : comparison.getEdgesRemoved()) {
                buf.append("\n").append(edge).append(" ====> <>");
            }
        }

        Font font = new Font("Monospaced", Font.PLAIN, 14);
        final JTextPane textPane = new JTextPane();
        textPane.setText(buf.toString());
//        final JTextField field = new StringTextField(graphComparison.getName(), 20);

//        field.setFont(font);
        textPane.setFont(font);
        textPane.setCaretPosition(textPane.getStyledDocument().getLength());

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(400, 400));

//        field.addFocusListener(new FieldListener(field));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(10));

        Box box = Box.createHorizontalBox();
//        box.add(new JLabel(" Name: "));
//        box.add(field);
//        box.add(Box.createHorizontalGlue());

        this.add(box);
        this.add(Box.createVerticalStrut(10));

        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel("Graph Comparison: "));
        box1.add(Box.createHorizontalGlue());

        add(box1);
        setLayout(new BorderLayout());
        add(scroll);
    }

    //============================= Inner Class ============================//

    private class FieldListener extends FocusAdapter  {

        private String current;
        private JTextField field;

        public FieldListener(JTextField field) {
            this.field = field;
            this.current = field.getText();
        }



        @Override
		public void focusLost(FocusEvent evt) {
            doAction();
        }


        private void doAction() {
            String text = field.getText();
            if(current.equals(text)){
                return;
            }
            if (isLegal(text)) {
                current = text;
                comparison.setName(text);
                GraphComparisonEditor.this.firePropertyChange("changeNodeLabel", null, text);
            } else {
               field.setText(current);
            }
        }
    }


}