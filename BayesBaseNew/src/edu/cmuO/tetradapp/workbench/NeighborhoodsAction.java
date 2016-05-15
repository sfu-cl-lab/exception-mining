package edu.cmu.tetradapp.workbench;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.editor.EditorWindow;
import edu.cmu.tetradapp.util.DesktopController;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Puts up a panel showing some graph properties, e.g., number of nodes and
 * edges in the graph, etc.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class NeighborhoodsAction extends AbstractAction implements ClipboardOwner {
    private GraphWorkbench workbench;

    /**
     * Creates a new copy subsession action for the given LayoutEditable and
     * clipboard.
     */
    public NeighborhoodsAction(GraphWorkbench workbench) {
        super("Neighborhoods");
        this.workbench = workbench;
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Box b = Box.createVerticalBox();
        Graph graph = workbench.getGraph();

        JTextArea textArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 600));

        textArea.append("Neighborhoods:");

        for (int i = 0; i < graph.getNodes().size(); i++) {
            Node node = graph.getNodes().get(i);

            List<Node> parents = graph.getParents(node);
            List<Node> children = graph.getChildren(node);

            List<Node> ambiguous = graph.getAdjacentNodes(node);
            ambiguous.removeAll(parents);
            ambiguous.removeAll(children);

            textArea.append("\n\nNeighborhood for " + node + ":");
            textArea.append("\n\tParents: " + niceList(parents));
            textArea.append("\n\tChildren: " + niceList(children));
            textArea.append("\n\tAmbiguous: " + niceList(ambiguous));
        }


        Box b2 = Box.createHorizontalBox();
        b2.add(scroll);
        textArea.setCaretPosition(0);
        b.add(b2);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(b);

        EditorWindow window = new EditorWindow(panel,
                "Neighborhoods", "Close", false);
        DesktopController.getInstance().addEditorWindow(window);
        window.setVisible(true);
        
//        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), b,
//                "Graph Properties", JOptionPane.PLAIN_MESSAGE);
    }

    private String niceList(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return "--NONE--";
        }

        Collections.sort(nodes, new Comparator<Node>() {
            @Override
			public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < nodes.size(); i++) {
            buf.append(nodes.get(i));

            if (i < nodes.size() - 1) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    /**
     * Required by the AbstractAction interface; does nothing.
     */
    @Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }


}