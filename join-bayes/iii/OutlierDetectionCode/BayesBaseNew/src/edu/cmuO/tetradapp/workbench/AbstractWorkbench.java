///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////                   (

package edu.cmu.tetradapp.workbench;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.PersistentRandomUtil;
import edu.cmu.tetradapp.util.LayoutEditable;
import edu.cmu.tetradapp.util.LayoutMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * The functionality of the workbench which is shared between the workbench
 * workbench and the session workbench (and any other workbenches which want to
 * use this functionality).
 *
 * @author Aaron Powell
 * @author Joseph Ramsey
 * @author Willie Wheeler
 * @version $Revision: 6530 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 * @see DisplayNode
 * @see DisplayEdge
 */
public abstract class AbstractWorkbench extends JComponent
        implements WorkbenchModel, LayoutEditable {

    //===================PUBLIC STATIC FINAL FIELDS=====================//

    /**
     * The mode in which the user is permitted to select workbench items or move
     * nodes.
     */
    public static final int SELECT_MOVE = 0;

    /**
     * The mode in which the user is permitted to select workbench items or move
     * nodes.
     */
    public static final int ADD_NODE = 1;

    /**
     * The mode in which the user is permitted to select workbench items or move
     * nodes.
     */
    public static final int ADD_EDGE = 2;

    //=========================PRIVATE FIELDS=============================//

    /**
     * The workbench which this workbench displays.
     */
    private Graph graph;

    /**
     * The map from model elements to display elements.
     */
    private Map modelToDisplay;

    /**
     * The map from display elements to model elements.
     */
    private Map displayToModel;

    /**
     * The map from edges to edge labels.
     */
    private Map displayToLabels;

    /**
     * The current mode of the workbench.
     */
    private int workbenchMode = SELECT_MOVE;

    /**
     * When edges are being constructed, one edge is anchored to a node and the
     * other edge tracks mouse dragged events; this is the edge that does this.
     * This edge should be null unless an edge is actually being tracked.
     */
    private IDisplayEdge trackedEdge;

    /**
     * For dragging nodes, a click point is needed; this is that click point.
     */
    private Point clickPoint;

    /**
     * For dragging nodes, the set of selected nodes at the start of dragging is
     * needed, since the selected nodes need to be moved in sync during the
     * drag.
     */
    private List<DisplayNode> dragNodes;

    /**
     * For selecting multiple nodes using a rubberband, a rubberband is needed;
     * this is it.
     */
    private Rubberband rubberband;

    /**
     * Indicates whether user editing is permitted.
     */
    private boolean allowDoubleClickActions = true;

    /**
     * Indicates whether nodes may be moved by the user.
     */
    private boolean allowNodeDragging = true;

    /**
     * Indicates whether user editing is permitted.
     */
    private boolean allowNodeEdgeSelection = true;

    /**
     * Indicates whether edge reorientations are permitted.
     */
    private boolean allowEdgeReorientations = true;

    /**
     * Indicates whether multiple node selection is allowed.
     */
    private boolean allowMultipleSelection = true;

    /**
     * True iff the user is allows to add measured variables.
     */
    private boolean addMeasuredVarsAllowed = true;

    /**
     * True iff the user is allowed to edit existing measured variables.
     */
    private boolean editExistingMeasuredVarsAllowed = true;

    /**
     * True iff the user is allowed to delete variables.
     */
    private boolean deleteVariablesAllowed = true;

    /**
     * ` Handler for ComponentEvents.
     */
    private final ComponentHandler compHandler = new ComponentHandler(this);

    /**
     * Handler for MouseEvents.
     */
    private final MouseHandler mouseHandler = new MouseHandler(this);

    /**
     * Handler MouseMotionEvents.
     */
    private final MouseMotionHandler mouseMotionHandler =
            new MouseMotionHandler(this);

    /**
     * Handler for PropertyChangeEvents.
     */
    private final PropertyChangeHandler propChangeHandler =
            new PropertyChangeHandler(this);

    /**
     * Maximum x value (for dragging).
     */
    private int maxX = 2000;

    /**
     * Maximum y value (for dragging).
     */
    private int maxY = 1500;

    /**
     * True iff node/edge adding/removing errors should be reported to the
     * user.
     */
    private boolean nodeEdgeErrorsReported = false;

    /**
     * True iff layout is permitted using a right click popup.
     */
    private boolean rightClickPopupAllowed = false;

    /**
     * A key dispatcher to allow pressing the control key to control whether
     * edges will be drawn in the workbench.
     */
    private KeyEventDispatcher controlDispatcher;

    /**
     * Returns the current mouse location. Needed for pasting.
     */
    private Point currentMouseLocation;

    /**
     * TEMPORARY bug fix added 4/15/2005. The bug is that in JDK 1.5.0_02
     * (without this bug fix) groups of nodes cannot be selected, because if you
     * click and drag, an extra mouseClicked event is fired when you release the
     * mouse. This is a known bug, #5039416 in Sun's bug database. To get around
     * the problem, we set this flag to true when a mouseDragged event is fired
     * and ignore the first click (and reset this flag to false) on the first
     * mouseClicked event after any mouseDragged event. When this bug is fixed
     * in JDK 1.5, this temporary bug fix shold be removed. jdramsey 4/15/2005
     */
    private boolean mouseDragging = false;

    /**
     * Returns the current displayed mouseover equation label. Returns null if
     * none is displayed. Used for removing the label.
     */
    private JComponent currentMouseoverLbl = null;

    //==============================CONSTRUCTOR============================//

    /**
     * Constructs a new workbench workbench.
     *
     * @param graph The graph that this workbench will display.
     */
    public AbstractWorkbench(Graph graph) {
        setGraph(graph);
        addMouseListener(this.mouseHandler);
        addMouseMotionListener(this.mouseMotionHandler);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setBackground(new Color(254, 254, 255));
        setFocusable(true);
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Deletes all selected nodes in the workbench plus any edges that have had
     * one of their nodes deleted in the process.
     */
    public final void deleteSelectedObjects() {
        Component[] components = getComponents();
        List<DisplayNode> graphNodes = new ArrayList<DisplayNode>();
        List<IDisplayEdge> graphEdges = new ArrayList<IDisplayEdge>();

        for (Component comp : components) {
            if (comp instanceof DisplayNode) {
                if (!isDeleteVariablesAllowed()) {
                    continue;
                }

                DisplayNode node = (DisplayNode) comp;

                if (node.isSelected()) {
                    graphNodes.add(node);
                }
            } else if (comp instanceof IDisplayEdge) {
                IDisplayEdge edge = (IDisplayEdge) comp;

                if (edge.isSelected()) {
                    graphEdges.add(edge);
                }
            }
        }

        for (DisplayNode graphNode : graphNodes) {
            removeNode(graphNode);
        }

        for (IDisplayEdge displayEdge : graphEdges) {
            try {
                removeEdge(displayEdge);
                resetEdgeOffsets(displayEdge);
            }
            catch (Exception e) {
                if (isNodeEdgeErrorsReported()) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            e.getMessage());
                }
            }
        }
    }


    /**
     * Deselects all edges and nodes in the workbench.
     */
    public final void deselectAll() {
        Component[] components = getComponents();

        for (Component comp : components) {
            if (comp instanceof IDisplayEdge) {
                ((IDisplayEdge) comp).setSelected(false);
            } else if (comp instanceof DisplayNode) {
                ((DisplayNode) comp).setSelected(false);
            }
        }

        repaint();
        firePropertyChange("BackgroundClicked", null, null);
    }

    /**
     * Returns the workbench mode. One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     *
     * @return the workbench mode. One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     */
    public final int getWorkbenchMode() {
        return this.workbenchMode;
    }

    /**
     * Returns the Graph this workbench displays.
     */
    @Override
	public final Graph getGraph() {
        return this.graph;
    }

    /**
     * Returns the currently selected nodes as a list.
     *
     * @return the currently selected nodes as a list.
     */
    public final List<DisplayNode> getSelectedNodes() {
        List<DisplayNode> selectedNodes = new ArrayList<DisplayNode>();
        Component[] components = getComponents();

        for (Component comp : components) {
            if ((comp instanceof DisplayNode) &&
                    ((DisplayNode) comp).isSelected()) {
                selectedNodes.add((DisplayNode) comp);
            }
        }

        return selectedNodes;
    }


    /**
     * Returns the current selected node, if exactly one is selected; otherwise,
     * return null.
     *
     * @return the current selected node, if exactly one is selected; otherwise,
     *         return null.
     */
    public final DisplayNode getSelectedNode() {
        List<DisplayNode> selectedNodes = getSelectedNodes();

        if (selectedNodes.size() == 1) {
            return selectedNodes.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the currently selected nodes as a vector.
     */
    public final List<Component> getSelectedComponents() {
        List<Component> selectedComponents = new ArrayList<Component>();
        Component[] components = getComponents();

        for (Component comp : components) {
            if (comp instanceof DisplayNode &&
                    ((DisplayNode) comp).isSelected()) {
                selectedComponents.add(comp);
            } else if (comp instanceof IDisplayEdge &&
                    ((IDisplayEdge) comp).isSelected()) {
                selectedComponents.add(comp);
            }
        }

        return selectedComponents;
    }

    /**
     * Returns the model edge for the given display edge.
     */
    public final Edge getModelEdge(IDisplayEdge displayEdge) {
        return (Edge) getDisplayToModel().get(displayEdge);
    }

    public boolean isAllowMultipleNodeSelection() {
        return allowMultipleSelection;
    }

    /**
     * Returns true iff nodes and edges may be added/removed by the user or
     * node/edge properties edited.
     */
    public boolean isAllowDoubleClickActions() {
        return allowDoubleClickActions;
    }

    /**
     * Returns true iff nodes may be dragged to new locations by the user.
     */
    public boolean isAllowNodeDragging() {
        return allowNodeDragging;
    }

    /**
     * Returns true iff nodes and edges may be selected by the user.
     */
    public boolean isAllowNodeEdgeSelection() {
        return allowNodeEdgeSelection;
    }

    /**
     * Returns true iff edge reorientations are permitted.
     */
    public boolean isAllowEdgeReorientation() {
        return allowEdgeReorientations;
    }

    /**
     * Returns true iff multiple nodes may be selected by the user using a
     * rubberband.
     */
    public boolean isAllowMultipleSelection() {
        return allowMultipleSelection;
    }

    /**
     * Sets whether adding or removing of nodes or edges will be allowed, or
     * node/edge properties edited.
     */
    public final void setAllowDoubleClickActions(boolean allowDoubleClickActions) {
        if (isAllowDoubleClickActions() && !allowDoubleClickActions) {
//            unregisterKeys();
            this.allowDoubleClickActions = false;
        } else if (!isAllowDoubleClickActions() && allowDoubleClickActions) {
//            registerKeys();
            this.allowDoubleClickActions = true;
        }
    }

    /**
     * Sets whether edge reorientations are permitted.
     */
    public final void setAllowEdgeReorientations(
            boolean allowEdgeReorientations) {
        this.allowEdgeReorientations = allowEdgeReorientations;
    }

    /**
     * Sets whether nodes may be dragged by the user to new locations.
     */
    public void setAllowNodeDragging(boolean allowNodeDragging) {
        this.allowNodeDragging = allowNodeDragging;
    }

    /**
     * Sets whether nodes and edges may be selected by the user.
     */
    public void setAllowNodeEdgeSelection(boolean allowNodeEdgeSelection) {
        this.allowNodeEdgeSelection = allowNodeEdgeSelection;
    }

    /**
     * Sets whether multiple mode selection is allowed.  If the value is set to
     * true, users may select multiple nodes in the workbench by holding down
     * the shift key.  If it is set to false, single node selection is
     * enforced.
     */
    public final void setAllowMultipleSelection(
            boolean allowMultipleSelection) {
        this.allowMultipleSelection = allowMultipleSelection;
    }

    /**
     * Sets the display workbench model to the indicated model and then notifies
     * listeners of the change.  (Called when the workbench is first constructed
     * as well as whenever the workbench model is changed.)
     */
    public final void setGraph(Graph graph) {
        setGraphWithoutNotify(graph);

        // if this workbench is sitting inside of a scrollpane,
        // let the scrollpane know how big it is.
        scrollRectToVisible(getVisibleRect());
        registerKeys();
        firePropertyChange("graph", null, graph);
        firePropertyChange("modelChanged", null, null);
    }

    /**
     * Sets the label for an edge to a particular JComponent.  The label will be
     * displayed halfway along the edge slightly off to the side.
     *
     * @param modelEdge the edge for the label.
     * @param label     the label for the component.
     */
    public final void setEdgeLabel(Edge modelEdge, JComponent label) {
        if (modelEdge == null) {
            throw new NullPointerException(
                    "Attempt to set a label on a " + "null model edge.");
        } else if (!getModelToDisplay().containsKey(modelEdge)) {
            throw new IllegalArgumentException("Attempt to set a label on " +
                    "a model edge that's not " + "in the editor.");
        }

        // retrieve display edge from map, or create one if not there...
        IDisplayEdge displayEdge =
                (IDisplayEdge) getModelToDisplay().get(modelEdge);
        GraphEdgeLabel oldLabel = getEdgeLabel(displayEdge, 0);

        if (oldLabel != null) {
            removeEdgeLabel(modelEdge);
        }

        if (label == null) {
            return;
        }

        GraphEdgeLabel edgeLabel = new GraphEdgeLabel(displayEdge, label);
        edgeLabel.setSize(edgeLabel.getPreferredSize());
        add(edgeLabel, 0);
        edgeLabel.validate();
        setEdgeLabel(displayEdge, edgeLabel);
    }

    /**
     * Sets the label for a node to a particular JComponent.  The label will be
     * displayed slightly off to the right of the node.
     */
    public final void setNodeLabel(Node modelNode, JComponent label, int x,
                                   int y) {
        if (modelNode == null) {
            throw new NullPointerException(
                    "Attempt to set a label on a " + "null model node.");
        } else if (!getModelToDisplay().containsKey(modelNode)) {
            throw new IllegalArgumentException("Attempt to set a label on " +
                    "a model node that's not " + "in the editor.");
        }

        // retrieve display node from map, or create one if not
        // there...
        DisplayNode displayNode =
                (DisplayNode) getModelToDisplay().get(modelNode);
        GraphNodeLabel oldLabel = getNodeLabel(displayNode, 0);

        if (oldLabel != null) {
            remove(oldLabel);
        }

        if (label == null) {
            return;
        }

        GraphNodeLabel nodeLabel = new GraphNodeLabel(displayNode, label, x, y);
        nodeLabel.setSize(nodeLabel.getPreferredSize());
        add(nodeLabel, 0);
        nodeLabel.validate();
        setNodeLabel(displayNode, nodeLabel);
    }

    /**
     * Sets the equation label for the measured graph node.
     */
    public final void setNodeEquationLabel(Node modelNode, JComponent label) {
        add(label, 0);
        currentMouseoverLbl = label;

        label.validate();
    }

    /**
     * Removes currently active equation label for the measured graph node.
     */
    public final void removeNodeEquationLabel() {
        if (currentMouseoverLbl != null)
            remove(currentMouseoverLbl);

        currentMouseoverLbl = null;

        repaint();
        validate();
    }

    private void setEdgeLabel(IDisplayEdge displayEdge,
                              GraphEdgeLabel edgeLabel) {
        getDisplayToLabels().put(displayEdge,
                Collections.singletonList(edgeLabel));
    }

    private GraphEdgeLabel getEdgeLabel(IDisplayEdge displayEdge, int index) {
        List list = (List) getDisplayToLabels().get(displayEdge);

        if (list == null) {
            return null;
        } else {
            return (GraphEdgeLabel) list.get(index);
        }
    }

    private void setNodeLabel(DisplayNode displayNode,
                              GraphNodeLabel nodeLabel) {
        getDisplayToLabels().put(displayNode,
                Collections.singletonList(nodeLabel));
    }

    private GraphNodeLabel getNodeLabel(DisplayNode displayNode, int index) {
        List list = (List) getDisplayToLabels().get(displayNode);

        if (list == null) {
            return null;
        } else {
            return (GraphNodeLabel) list.get(index);
        }
    }

    /**
     * Returns the label for the given object.
     */
    public final Object getLabel(Object graphElement) {
        Object displayObject = getModelToDisplay().get(graphElement);
        Object labelWrapper = getDisplayToLabels().get(displayObject);
        return ((GraphNodeLabel) labelWrapper).getLabel();
    }

    /**
     * Removes the label from an edge.
     *
     * @param edge the edge from which a label is to be removed.
     */
    private void removeEdgeLabel(Edge edge) {
        IDisplayEdge displayEdge = (IDisplayEdge) getModelToDisplay().get(edge);
        GraphEdgeLabel edgeLabel = getEdgeLabel(displayEdge, 0);

        if (edgeLabel == null) {
            return;
        }

        remove(edgeLabel);
        getDisplayToLabels().remove(displayEdge);
    }

    /**
     * Sets the mode of the workbench to the indicated new mode. (Ignores
     * unrecognized modes.)
     *
     * @param workbenchMode One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     */
    public final void setWorkbenchMode(int workbenchMode) {
        if (workbenchMode == SELECT_MOVE) {
            if (this.workbenchMode != SELECT_MOVE) {
                this.workbenchMode = workbenchMode;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                deselectAll();
            }
        } else if (workbenchMode == ADD_NODE) {
            if (this.workbenchMode != ADD_NODE) {
                this.workbenchMode = workbenchMode;
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                deselectAll();
            }
        } else if (workbenchMode == ADD_EDGE) {
            if (this.workbenchMode != ADD_EDGE) {
                this.workbenchMode = workbenchMode;
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                deselectAll();
            }
        } else {
            throw new IllegalArgumentException(
                    "Must be SELECT_MOVE, " + "ADD_NODE, or ADD_EDGE.");
        }
    }

    public final Map getModelToDisplay() {
        return modelToDisplay;
    }

    private Map getDisplayToModel() {
        return displayToModel;
    }

    /**
     * Sets the maximum x value (for dragging).
     *
     * @param maxX the maximum x value (must be >= 100).
     */
    public final void setMaxX(int maxX) {
        if (maxX < 100) {
            throw new IllegalArgumentException();
        }

        this.maxX = maxX;
    }

    /**
     * Selects the editor node corresponding to the given model node.
     */
    public final void selectNode(Node modelNode) {
        if (!isAllowNodeEdgeSelection()) {
            return;
        }

        DisplayNode graphNode =
                (DisplayNode) getModelToDisplay().get(modelNode);
        graphNode.setSelected(true);
    }

    /**
     * Selects the editor edge corresponding to the given model edge.
     */
    public final void selectEdge(Edge modelEdge) {
        IDisplayEdge graphEdge =
                (IDisplayEdge) getModelToDisplay().get(modelEdge);
        graphEdge.setSelected(true);
    }

    /**
     * Selects all and only those edges that are connecting selected nodes.
     * Should be called after every time the node selection is changed.
     */
    public final void selectConnectingEdges() {
        if (!isAllowNodeEdgeSelection()) {
            return;
        }

        Component[] components = getComponents();

        for (Component comp : components) {
            if (comp instanceof IDisplayEdge) {
                IDisplayEdge graphEdge = (IDisplayEdge) comp;
                DisplayNode node1 = graphEdge.getComp1();
                DisplayNode node2 = graphEdge.getComp2();

                if (node2 != null) {
                    boolean selected = node1.isSelected() && node2.isSelected();
                    graphEdge.setSelected(selected);
                }
            }
        }
    }

    /**
     * Selects all and only those edges that are connecting selected nodes.
     * Should be called after every time the node selection is changed.
     */
    public final void selectConnectingEdges(List<DisplayNode> displayNodes) {
        if (!isAllowNodeEdgeSelection()) {
            return;
        }

        Component[] components = getComponents();

        for (Component comp : components) {
            if (comp instanceof IDisplayEdge) {
                IDisplayEdge graphEdge = (IDisplayEdge) comp;
                DisplayNode node1 = graphEdge.getComp1();
                DisplayNode node2 = graphEdge.getComp2();

                if (node1 instanceof GraphNodeError) {
                    continue;
                }

                if (node2 instanceof GraphNodeError) {
                    continue;
                }

                if (node2 != null) {
                    boolean selected = displayNodes.contains(node1) &&
                            displayNodes.contains(node2);
                    graphEdge.setSelected(selected);
                }
            }
        }
    }

    /**
     * Paints the background of the workbench.
     */
    @Override
	public final void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

    /**
     * Scrolls the workbench image so that the given node is in view, then
     * selects that node.
     *
     * @param modelNode the model node to show.
     */
    public final void scrollWorkbenchToNode(Node modelNode) {
        Object o = getModelToDisplay().get(modelNode);
        DisplayNode displayNode = (DisplayNode) o;

        if (displayNode != null) {
            Rectangle bounds = displayNode.getBounds();
            scrollRectToVisible(bounds);
            deselectAll();

            if (isAllowNodeEdgeSelection()) {
                displayNode.setSelected(true);
            }
        }
    }

    /**
     * Sets the maximum x value (for dragging).
     *
     * @param maxY the maximum Y value (must be >= 100).
     */
    public final void setMaxY(int maxY) {
        if (maxY < 100) {
            throw new IllegalArgumentException();
        }

        this.maxY = maxY;
    }

    @Override
	public void setBackground(Color color) {
        super.setBackground(color);
        repaint();
    }

    @Override
	public Color getBackground() {
        return super.getBackground();
    }

    public Point getCurrentMouseLocation() {
        return currentMouseLocation;
    }

    @Override
	public void layoutByGraph(Graph layoutGraph) {
        GraphUtils.arrangeBySourceGraph(graph, layoutGraph);

        for (Node modelNode : graph.getNodes()) {
            DisplayNode displayNode = (DisplayNode) getModelToDisplay().get(modelNode);

            if (displayNode == null) {
                continue;
            }

            Dimension dim = displayNode.getPreferredSize();

            int centerX = modelNode.getCenterX();
            int centerY = modelNode.getCenterY();

            displayNode.setSize(dim);
            displayNode.setLocation(centerX - dim.width / 2,
                    centerY - dim.height / 2);
        }

//        setGraphWithoutNotify(graph);
    }

    @Override
	public Knowledge getKnowledge() {
        return null;
    }

    @Override
	public Graph getSourceGraph() {
        return getGraph();
    }

    /**
     * Not implemented for the workbench.
     */
    @Override
	public void layoutByKnowledge() {
        // Do nothing.
    }

    @Override
	public Rectangle getVisibleRect() {
        return super.getVisibleRect();
    }

    public void scrollNodesToVisible(List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        DisplayNode displayNode = (DisplayNode) getModelToDisplay().get(nodes.get(0));
        Rectangle rect = displayNode.getBounds();

        for (int i = 1; i < nodes.size(); i++) {
            displayNode = (DisplayNode) getModelToDisplay().get(nodes.get(i));
            rect = rect.union(displayNode.getBounds());
        }

        adjustPreferredSize();
        scrollRectToVisible(rect);
    }

    @Override
	abstract public Node getNewModelNode();

    @Override
	abstract public DisplayNode getNewDisplayNode(Node modelNode);

    @Override
	abstract public IDisplayEdge getNewDisplayEdge(Edge modelEdge);

    @Override
	abstract public Edge getNewModelEdge(Node node1, Node node2);

    @Override
	abstract public IDisplayEdge getNewTrackingEdge(DisplayNode displayNode,
                                                    Point mouseLoc);

    //============================PRIVATE METHODS=========================//

    /**
     * Sets the display workbench model to the indicated model.  (Called when
     * the workbench is first constructed as well as whenever the workbench
     * model is changed.)
     */
    private void setGraphWithoutNotify(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph model cannot be null.");
        }

        this.graph = graph;
        this.modelToDisplay = new HashMap();
        this.displayToModel = new HashMap();
        this.displayToLabels = new HashMap();

        removeAll();
        graph.addPropertyChangeListener(this.propChangeHandler);

        // extract the current contents from the model...
        List<Node> nodes = graph.getNodes();
        for (Node node : nodes) {
            if (!getModelToDisplay().containsKey(node)) {
                addNode(node);
            }
        }

        List<Edge> edges = graph.getEdges();
        for (Edge edge : edges) {
            if (!getModelToDisplay().containsKey(edge)) {
                addEdge(edge);
            }
        }

        adjustPreferredSize();

        if (getPreferredSize().getWidth() > getMaxX()) {
            setMaxX((int) getPreferredSize().getWidth());
        }

        if (getPreferredSize().getHeight() > getMaxY()) {
            setMaxY((int) getPreferredSize().getHeight());
        }

        revalidate();
        repaint();
    }

    /**
     * Returns the maximum x value (for dragging).
     */
    private int getMaxX() {
        return maxX;
    }

    /**
     * Adjusts the bounds of the workbench to included the point (0, 0) and the
     * union of the bounds rectangles of all of the components in the workbench.
     * This allows for scrollbars to automatically reflect the position of a
     * component which is being dragged.
     */
    private void adjustPreferredSize() {
        Component[] components = getComponents();
        Rectangle r = new Rectangle(0, 0, 400, 400);

        for (Component component1 : components) {
            r = r.union(component1.getBounds());
        }

        setPreferredSize(new Dimension(r.width, r.height));
    }

    /**
     * Adds a session node to the workbench centered at the specified location;
     * the type of node added is determined by the mode of the workbench.
     *
     * @param loc the location of the center of the session node.
     * @return the added node.
     */
    private Node addNode(Point loc) throws IllegalArgumentException {
        Node modelNode = getNewModelNode();

        if (modelNode.getNodeType() == NodeType.MEASURED && !isAddMeasuredVarsAllowed()) {
            throw new IllegalArgumentException("Attempt to add measured variable " +
                    "when this has been disallowed.");
        }

        // Add the model node to the display workbench model.
        modelNode.setCenterX(loc.x);
        modelNode.setCenterY(loc.y);
        getGraph().addNode(modelNode);
        firePropertyChange("modelChanged", null, null);

        return modelNode;
    }

    /**
     * Adds the given model node to the model and adds a corresponding display
     * node to the display.
     *
     * @param modelNode the model node.
     */
    private void addNode(Node modelNode) {
        if (getModelToDisplay().containsKey(modelNode)) {
            return;
        }

        if (modelNode.getNodeType() == NodeType.MEASURED && !isAddMeasuredVarsAllowed()) {
            throw new IllegalArgumentException("Attempt to add measured variable " +
                    "when this has been disallowed.");
        }

        // Pick a location for the node:
        // (1) If the node has a location in the workbench info object, use that.
        // (2) If not, then if the node is an error term, pick a location
        // that's down and to the right of its associated term.
        // (3) If it's an error term that doesn't have an associated node,
        // don't add it.
        // (4) Otherwise, pick a random location.
        int centerX = modelNode.getCenterX();
        int centerY = modelNode.getCenterY();

        if (centerX == -1 || centerY == -1) {
            if (modelNode.getNodeType() == NodeType.ERROR) {
                Node assocNode =
                        GraphUtils.getAssociatedNode(modelNode, getGraph());

                if (assocNode == null) {
                    return;
                }

                centerX += assocNode.getCenterX() + 50;
                centerY += assocNode.getCenterY() + 50;
            } else {
                centerX = PersistentRandomUtil.getInstance().nextInt(300);
                centerY = PersistentRandomUtil.getInstance().nextInt(300);
            }

            modelNode.setCenterX(centerX);
            modelNode.setCenterY(centerY);
        }

        // Construct a display node for the model node.
        DisplayNode displayNode = getNewDisplayNode(modelNode);

        // Link the display node to the model node.
        getModelToDisplay().put(modelNode, displayNode);
        getDisplayToModel().put(displayNode, modelNode);

        // Set the bounds of the display node.
        Dimension dim = displayNode.getPreferredSize();

        displayNode.setSize(dim);
        displayNode.setLocation(centerX - dim.width / 2,
                centerY - dim.height / 2);

        // add the display node
        add(displayNode, 0);

        // Add listeners.
        displayNode.addComponentListener(this.compHandler);
        displayNode.addMouseListener(this.mouseHandler);
        displayNode.addMouseMotionListener(this.mouseMotionHandler);
        displayNode.addPropertyChangeListener(this.propChangeHandler);
        repaint();
        validate();

//        snapNodeToGrid(displayNode);

        // Fire notification event. jdramsey 12/11/01
        firePropertyChange("nodeAdded", null, displayNode);
    }

    /**
     * Adds the specified edge to the model and updates the display to match.
     *
     * @param modelEdge the mode edge.
     */
    private void addEdge(Edge modelEdge) {
        if (modelEdge == null) {
            return;
        }

        if (modelEdge.getNode1() == modelEdge.getNode2()) {
            throw new IllegalArgumentException(
                    "Edges to self are not supported.");
        }

        if (getModelToDisplay().containsKey(modelEdge)) {
            return;
        }

        if (!getGraph().containsEdge(modelEdge)) {
            throw new IllegalArgumentException(
                    "Attempt to add edge not in model.");
        }

        // construct a display edge for the model edge
        Node modelNodeA = modelEdge.getNode1();
        Node modelNodeB = modelEdge.getNode2();

        DisplayNode displayNodeA = displayNode(modelNodeA);
        DisplayNode displayNodeB = displayNode(modelNodeB);

        if ((displayNodeA == null) || (displayNodeB == null)) {
            return;
        }

        IDisplayEdge displayEdge = getNewDisplayEdge(modelEdge);

        if (displayEdge == null) {
            return;
        }

        // Link the display edge to the model edge.

        getModelToDisplay().put(modelEdge, displayEdge);
        getDisplayToModel().put(displayEdge, modelEdge);

        // Add the display edge to the workbench. (Add it to the "back".)
        add((Component) displayEdge, -1);

        // Add listeners.
        ((Component) displayEdge).addComponentListener(this.compHandler);
        ((Component) displayEdge).addMouseListener(this.mouseHandler);
        ((Component) displayEdge).addMouseMotionListener(
                this.mouseMotionHandler);
        ((Component) displayEdge).addPropertyChangeListener(
                this.propChangeHandler);

        // Reset offsets (for multiple edges between node pairs).
        resetEdgeOffsets(displayEdge);

        // Fire notification. jdramsey 12/11/01
        firePropertyChange("edgeAdded", null, displayEdge);
    }

    /**
     * Scans through all edges between two nodes, resets those edge's offset
     * values. Note that these offsets are stored in the edges themselves so
     * this does not have to be recomputed all the time
     */
    private void resetEdgeOffsets(IDisplayEdge graphEdge) {
        try {
            DisplayNode displayNode1 = graphEdge.getNode1();
            DisplayNode displayNode2 = graphEdge.getNode2();

            Node node1 = displayNode1.getModelNode();
            Node node2 = displayNode2.getModelNode();

            Graph graph = getGraph();

            List<Edge> edges = graph.getEdges(node1, node2);

            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                Node _node1 = edge.getNode1();
                boolean awayFrom = (_node1 == node1);

                IDisplayEdge displayEdge = (IDisplayEdge) getModelToDisplay()
                        .get(edge);

                if (displayEdge != null) {
                    displayEdge.setOffset(
                            calcEdgeOffset(i, edges.size(), 35, awayFrom));
                }
            }
        }
        catch (UnsupportedOperationException e) {
            // This happens for the session workbench. The getEdges() method
            // is not implemented for it. Not sure if we'll ever need it to
            // be implemented.  jdramsey 4/14/2004
        }
    }

    /**
     * Calculates the offset in pixels of a given edge - this could use a little
     * tweaking still.
     *
     * @param i the index of the given edge
     * @param n the number of edges
     * @param w the total width to spread edges over
     */
    private static double calcEdgeOffset(int i, int n, int w,
                                         boolean away_from) {
        double i_d = i;
        double n_d = n;
        double w_d = w;

        double offset = w_d * (2.0 * i_d + 1.0 - n_d) / 2.0 / n_d;

        double direction = away_from ? 1.0 : -1.0;
        return direction * offset;
    }

    private DisplayNode displayNode(Node modelNodeA) {
        Object o = getModelToDisplay().get(modelNodeA);

        if (o == null) {
            reconstiteMaps();
            o = getModelToDisplay().get(modelNodeA);
        }

        return (DisplayNode) o;
    }

    /**
     * Calculates the distance between two points.
     *
     * @param p1 the 'from' point.
     * @param p2 the 'to' point.
     * @return the distance between p1 and p2.
     */
    private static double distance(Point p1, Point p2) {
        double d = (p1.x - p2.x) * (p1.x - p2.x);
        d += (p1.y - p2.y) * (p1.y - p2.y);
        d = Math.sqrt(d);
        return d;
    }

    /**
     * Finds the nearest node to a given point.  More specifically, finds the
     * node whose center point is nearest to the given point.  (If more than one
     * such node exists, the one with lowest z-order is returned.)
     *
     * @param p the point for which the nearest node is requested.
     * @return the nearest node to point p.
     */
    private DisplayNode findNearestNode(Point p) {
        Component[] components = getComponents();
        double distance, leastDistance = Double.POSITIVE_INFINITY;
        int index = -1;

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof DisplayNode) {
                DisplayNode node = (DisplayNode) components[i];

                distance = distance(p, node.getCenterPoint());

                if (distance < leastDistance) {
                    leastDistance = distance;
                    index = i;
                }
            }
        }

        if (index != -1) {
            return (DisplayNode) (components[index]);
        } else {
            return null;
        }
    }

    /**
     * Finishes drawing a rubberband.
     *
     * @see #startRubberband
     */
    private void finishRubberband() {
        if (rubberband != null) {
            remove(rubberband);
            this.rubberband = null;
            repaint();
        }
    }

    /**
     * Finishes the drawing of a new edge.
     *
     * @see #startEdge
     */
    private void finishEdge() {
        if (getTrackedEdge() == null) {
            return;
        }

        // Retrieve the two display components this edge should connect.
        DisplayNode comp1 = getTrackedEdge().getComp1();
        Point p = getTrackedEdge().getTrackPoint();
        DisplayNode comp2 = findNearestNode(p);

        // Edges to self are not supported.
        if (comp1 != comp2) {

            // Construct the model edge
            try {
                Node node1 = (Node) (getDisplayToModel().get(comp1));
                Node node2 = (Node) (getDisplayToModel().get(comp2));
                Edge modelEdge = getNewModelEdge(node1, node2);

                // Add model edge to model; this will result in an event fired
                // back from the model to add a display edge, so we can remove
                // the tracked edge and forget about it.
                getGraph().addEdge(modelEdge);
                firePropertyChange("modelChanged", null, null);
            }
            catch (Exception e) {
                e.printStackTrace();

                if (isNodeEdgeErrorsReported()) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            e.getMessage());
                }
            }
        }

        remove((Component) getTrackedEdge());
        repaint();

        // reset the tracked edge to null to wait for the next attempt
        // at adding an edge.
        this.trackedEdge = null;
    }

    /**
     * Fires a property change event, property name = "selectedNodes", with the
     * new node selection as its new value (a List).
     */
    private void fireNodeSelection() {
        Component[] components = getComponents();
        List<Node> selection = new LinkedList<Node>();

        for (Component component : components) {
            if (component instanceof DisplayNode) {
                DisplayNode displayNode = (DisplayNode) component;

                if (displayNode.isSelected()) {
                    Node modelNode =
                            (Node) (getDisplayToModel().get(displayNode));

                    selection.add(modelNode);
                }
            }
        }

        if (isAllowMultipleNodeSelection()) {
            firePropertyChange("selectedNodes", null, selection);
        } else {
            if (selection.size() == 1) {
                firePropertyChange("selectedNode", null, selection.get(0));
            } else {
                throw new IllegalStateException(
                        "Multiple or null selection detected " +
                                "when single selection mode is set.");
            }
        }
    }

    /**
     * Registers the remove and backspace keys to remove selected objects.
     */
    private void registerKeys() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");

        Action deleteAction = new AbstractAction() {
            @Override
			public void actionPerformed(ActionEvent e) {
                AbstractWorkbench workbench = (AbstractWorkbench) e.getSource();

                List<Component> components = workbench.getSelectedComponents();
                int numNodes = 0, numEdges = 0;

                for (Component c : components) {
                    if (c instanceof DisplayNode) {
                        numNodes++;
                    } else if (c instanceof DisplayEdge) {
                        numEdges++;
                    }
                }

                StringBuffer buf = new StringBuffer();

                if (isDeleteVariablesAllowed()) {
                    buf.append("Number of nodes selected = ");
                    buf.append(numNodes);
                }
                
                buf.append("\nNumber of edges selected = ");
                buf.append(numEdges);
                buf.append("\n\nDelete selected items?");

                int ret = JOptionPane.showConfirmDialog(workbench,
                        buf.toString());

                if (ret != JOptionPane.YES_OPTION) {
                    return;
                }

                deleteSelectedObjects();
            }
        };

        getActionMap().put("DELETE", deleteAction);

        if (controlDispatcher == null) {
            controlDispatcher = new KeyEventDispatcher() {
                @Override
				public boolean dispatchKeyEvent(KeyEvent e) {
                    return respondToControlKey(e);
                }
            };
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(controlDispatcher);
    }

    private boolean respondToControlKey(KeyEvent e) {
        if (hasFocus()) {
            int keyCode = e.getKeyCode();
            int id = e.getID();

            if (keyCode == KeyEvent.VK_CONTROL) {
                if (id == KeyEvent.KEY_PRESSED) {
                    this.workbenchMode = ADD_EDGE;
                    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                } else if (id == KeyEvent.KEY_RELEASED) {
                    finishEdge();
                    this.workbenchMode = SELECT_MOVE;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        return false;
    }

    AbstractWorkbench getWorkbench() {
        return this;
    }

    /**
     * In response to a request from the model, removes the display node
     * corresponding to the given model node from the display. Assumes that the
     * model node was removed from the model and that this request is coming
     * from the propertyChange() method.  DO NOT CALL THIS METHOD DIRECTLY;
     * RATHER, REMOVE THE MODEL NODE DIRECTLY FROM THE MODEL AND LET THE ENSUING
     * EVENTS FROM MODEL TO DISPLAY REMOVE THE NODE FROM THE DISPLAY. OTHERWISE,
     * THE DISPLAY AND MODEL WILL GET OUT OF SYNC!
     *
     * @param modelNode the model node to be removed.
     */
    private void removeNode(Node modelNode) {
        if (modelNode == null) {
            throw new NullPointerException(
                    "Attempt to remove a null model node.");
        }

        DisplayNode displayNode =
                (DisplayNode) (getModelToDisplay().get(modelNode));

        if (displayNode == null) {
            getModelToDisplay().remove(modelNode);
        } else {
            setNodeLabel(modelNode, null, 0, 0);
            remove(displayNode);
            getDisplayToModel().remove(displayNode);
            getModelToDisplay().remove(modelNode);
            displayNode.removePropertyChangeListener(this.propChangeHandler);
            repaint();

            // Fire notification.
            firePropertyChange("nodeRemoved", displayNode, null);
        }
    }

    /**
     * Removes the given display node from the workbench by requesting that the
     * model remove the corresponding model node.
     *
     * @param displayNode the display node.
     */
    private void removeNode(DisplayNode displayNode) {
//        if (!isAllowDoubleClickActions()) {
//            return;
//        }

        if (displayNode == null) {
            return;
        }

        Node modelNode = (Node) (getDisplayToModel().get(displayNode));

        // Error nodes cannot be removed explicitly; they must be removed by
        // removing the nodes they are attached to.
        if (!(modelNode.getNodeType() == NodeType.ERROR)) {
            getGraph().removeNode(modelNode);
        }

        firePropertyChange("modelChanged", null, null);
    }

    /**
     * In response to a request from the model, removes the display edge
     * corresponding to the given model edge from the display. Assumes that the
     * model edge was removed from the model and that this request is coming
     * from the propertyChange() method.  DO NOT CALL THIS METHOD DIRECTLY;
     * RATHER, REMOVE THE MODEL EDGE FROM THE MODEL AND LET THE ENSUING EVENTS
     * (FROM MODEL TO DISPLAY) REMOVE THE EDGE FROM THE DISPLAY.  OTHERWISE, THE
     * DISPLAY AND MODEL WILL GET OUT OF SYNC.
     */
    private void removeEdge(Edge modelEdge) {
        if (modelEdge == null) {
            return;
        }

        IDisplayEdge displayEdge =
                (IDisplayEdge) (getModelToDisplay().get(modelEdge));

        if (displayEdge == null) {
            getModelToDisplay().remove(modelEdge);
        } else {
            removeEdgeLabel(modelEdge);
            remove((Component) displayEdge);
            getDisplayToModel().remove(displayEdge);
            getModelToDisplay().remove(modelEdge);

            ((Component) displayEdge).removePropertyChangeListener(
                    this.propChangeHandler);
            repaint();
            firePropertyChange("edgeRemoved", displayEdge, null);
        }
    }

    /**
     * Removes the given display edge from the workbench by requesting that the
     * model remove the corresponding model edge.
     */
    private void removeEdge(IDisplayEdge displayEdge) {
        if (displayEdge == null) {
            return;
        }

        Edge modelEdge = (Edge) (getDisplayToModel().get(displayEdge));

        try {
            getGraph().removeEdge(modelEdge);
            firePropertyChange("modelChanged", null, null);
        }
        catch (Exception e) {
            if (isNodeEdgeErrorsReported()) {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        e.getMessage());
            }
        }
    }

    /**
     * Selects all of the nodes inside the rubberband and all edges connecting
     * selected nodes.
     *
     * @param rubberband The rubberband shape appearing in the GUI.
     * @param shiftDown  Whether the shift key is down.
     */
    private void selectAllInRubberband(Rubberband rubberband,
                                       boolean shiftDown, boolean altDown) {
        if (!isAllowNodeEdgeSelection()) {
            return;
        }

        if (!shiftDown) {
            deselectAll();
        }

        Shape rubberShape = rubberband.getShape();
        Point rubberLoc = rubberband.getLocation();
        Component[] components = getComponents();
        List<DisplayNode> selectedNodes = new ArrayList<DisplayNode>();

        for (Component comp : components) {
            if (comp instanceof DisplayNode) {
                Rectangle bounds = comp.getBounds();
                bounds.translate(-rubberLoc.x, -rubberLoc.y);
                DisplayNode graphNode = (DisplayNode) comp;

                if (rubberShape.intersects(bounds)) {
                    selectedNodes.add(graphNode);
                }
            }
        }

        if (altDown) {
            selectConnectingEdges(selectedNodes);
        } else {
            for (DisplayNode graphNode : selectedNodes) {
                graphNode.setSelected(true);
            }

            selectConnectingEdges();
        }
    }

    /**
     * Returns the maximum y value (for dragging).
     */
    private int getMaxY() {
        return maxY;
    }

    /**
     * Starts a tracked edge by anchoring it to one node and specifying the
     * initial mouse track point.
     *
     * @param node     the initial anchored node.
     * @param mouseLoc the initial tracking mouse location.
     */
    private void startEdge(DisplayNode node, Point mouseLoc) {
        if (getTrackedEdge() != null) {
            remove((Component) getTrackedEdge());
            this.trackedEdge = null;
            repaint();
        }

        this.trackedEdge = getNewTrackingEdge(node, mouseLoc);
        add((Component) getTrackedEdge(), -1);
        deselectAll();
    }

    /**
     * Starts dragging a node.
     *
     * @param p the click point for the drag.
     */
    private void startNodeDrag(Point p) {
        if (!allowNodeDragging) {
            return;
        }

        this.clickPoint = p;
        this.dragNodes = getSelectedNodes();
    }

    /**
     * Starts drawing a rubberband to allow selection of multiple nodes.
     *
     * @param p the point where the rubberband begins.
     * @see #finishRubberband
     */
    private void startRubberband(Point p) {
        if (rubberband != null) {
            remove(rubberband);
            this.rubberband = null;
            repaint();
        }

        if (isAllowNodeEdgeSelection() && isAllowMultipleNodeSelection()) {
            this.rubberband = new Rubberband(p);
            add(rubberband, 0);
            rubberband.repaint();
        }
    }

    /**
     * Unregistered the keyboard actions which are normally registered when the
     * user is allowed to edit the workbench directly.
     *
     * @see #registerKeys
     */
    private void unregisterKeys() {
        unregisterKeyboardAction(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(controlDispatcher);
    }

    private void snapNodeToGrid(DisplayNode node) {
        int gridSize = 5;

        int x = node.getCenterPoint().x;
        int y = node.getCenterPoint().y;

        x = gridSize * ((x + gridSize / 2) / gridSize);
        y = gridSize * ((y + gridSize / 2) / gridSize);

        node.setLocation(x - node.getSize().width / 2,
                y - node.getSize().height / 2);
    }

    private void snapNodeToGrid(Node node) {
        if (!(modelToDisplay.containsKey(node))) {
            return;
        }

        DisplayNode display = (DisplayNode) modelToDisplay.get(node);

        int gridSize = 20;

        int x = display.getCenterPoint().x;
        int y = display.getCenterPoint().y;

        x = gridSize * ((x + gridSize / 2) / gridSize);
        y = gridSize * ((y + gridSize / 2) / gridSize);

        display.setLocation(x - display.getSize().width / 2,
                y - display.getSize().height / 2);
    }

    private void handleMouseClicked(MouseEvent e) {
        Object source = e.getSource();

        if (!isAllowNodeEdgeSelection()) {
            return;
        }

        if (source instanceof DisplayNode) {
            nodeClicked(source, e);
        } else if (source instanceof IDisplayEdge) {
            edgeClicked(source, e);
        } else {
            deselectAll();
        }
    }

    private void edgeClicked(Object source, MouseEvent e) {
        IDisplayEdge graphEdge = (IDisplayEdge) (source);

        if (e.getClickCount() == 2) {
            deselectAll();
            graphEdge.launchAssociatedEditor();
            firePropertyChange("edgeLaunch", graphEdge, graphEdge);
        } else {
            if (isAllowEdgeReorientation()) {
                reorientEdge(source, e);
            }

            if (graphEdge.isSelected()) {
                graphEdge.setSelected(false);
            } else if (e.isShiftDown()) {
                graphEdge.setSelected(true);
            } else {
                deselectAll();
                graphEdge.setSelected(true);
            }
        }
    }

    private void nodeClicked(Object source, MouseEvent e) {
        DisplayNode node = (DisplayNode) source;

        if (e.getClickCount() == 2) {
            if (isAllowDoubleClickActions()) {
                doDoubleClickAction(node);
            }
        } else {
            if (node.isSelected()) {
                node.setSelected(false);
                selectConnectingEdges();
                fireNodeSelection();
            } else {
                if (!e.isShiftDown()) {
                    deselectAll();
                }

                node.setSelected(true);
                selectConnectingEdges();
                fireNodeSelection();
            }
        }
    }

    private void reorientEdge(Object source, MouseEvent e) {
        IDisplayEdge graphEdge = (IDisplayEdge) (source);
        Point point = e.getPoint();
        PointPair connectedPoints = graphEdge.getConnectedPoints();
        Point pointA = connectedPoints.getFrom();
        Point pointB = connectedPoints.getTo();
        double length = distance(pointA, pointB);
        double endpointRadius = Math.min(20.0, length / 3.0);

        if (e.isShiftDown()) {
            if (distance(point, pointA) < endpointRadius) {
                toggleEndpoint(graphEdge, 1);
                firePropertyChange("modelChanged", null, null);
            } else if (distance(point, pointB) < endpointRadius) {
                toggleEndpoint(graphEdge, 2);
                firePropertyChange("modelChanged", null, null);
            }
        } else {
            if (distance(point, pointA) < endpointRadius) {
                directEdge(graphEdge, 1);
                firePropertyChange("modelChanged", null, null);
            } else if (distance(point, pointB) < endpointRadius) {
                directEdge(graphEdge, 2);
                firePropertyChange("modelChanged", null, null);
            }
        }
    }

    private void handleMousePressed(MouseEvent e) {
        grabFocus();

        Object source = e.getSource();
        Point loc = e.getPoint();

        if (isRightClickPopupAllowed() && source == this &&
                SwingUtilities.isRightMouseButton(e)) {
            launchPopup(e);
            return;
        }

        switch (workbenchMode) {
            case SELECT_MOVE:
                if (source == this) {
                    startRubberband(loc);
                } else if (source instanceof DisplayNode) {
                    startNodeDrag(loc);
                }
                break;

            case ADD_NODE:
                if (!isAllowDoubleClickActions()) {
                    return;
                }

                if (source == this) {
                    Node node = addNode(loc);
                    snapNodeToGrid(node);
                }
                break;

            case ADD_EDGE:
                if (!isAllowDoubleClickActions()) {
                    return;
                }

                if (source instanceof IDisplayEdge) {
                    return;
                }

                if (source instanceof DisplayNode) {
                    Point o = ((Component) (source)).getLocation();
                    loc.translate(o.x, o.y);
                }

                DisplayNode nearestNode = findNearestNode(loc);

                if (nearestNode != null) {
                    startEdge(nearestNode, loc);
                }

                break;
        }
    }

    private void launchPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new LayoutMenu(this));
        popup.show(this, e.getX(), e.getY());
    }

    private void handleMouseReleased(MouseEvent e) {
        Object source = e.getSource();

        switch (workbenchMode) {
            case SELECT_MOVE:
                if (source == this) {
                    finishRubberband();
                } else if (source instanceof DisplayNode) {
                    List<DisplayNode> dragNodes = this.dragNodes;

                    if (dragNodes != null && dragNodes.isEmpty()) {
                        snapSingleNodeFromNegative(source);
                        snapNodeToGrid((DisplayNode) source);
                        scrollRectToVisible(((DisplayNode) source).getBounds());
                    } else if (dragNodes != null && !dragNodes.isEmpty()) {
                        snapDragGroupFromNegative();

                        Rectangle rect = dragNodes.get(0).getBounds();

                        for (int i = 1; i < dragNodes.size(); i++) {
                            rect = rect.union(dragNodes.get(i).getBounds());
                        }

//                        scrollRectToVisible(rect);
                    }
                }
                break;

            case ADD_EDGE:
                if (!isAllowDoubleClickActions()) {
                    return;
                }

                finishEdge();
                break;
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        setMouseDragging(true);

        Object source = e.getSource();
        Point newPoint = e.getPoint();

        switch (workbenchMode) {
            case SELECT_MOVE:
                dragNodes(source, newPoint, e.isShiftDown(), e.isAltDown());
                break;

            case ADD_NODE:
                if (source instanceof DisplayNode && getSelectedComponents().isEmpty()) {
                    dragNodes(source, newPoint, e.isShiftDown(), e.isAltDown());
                }

                break;

            case ADD_EDGE:
                if (!isAllowDoubleClickActions()) {
                    return;
                }

                dragNewEdge(source, newPoint);
                break;
        }
    }

    private void snapSingleNodeFromNegative(Object source) {
        DisplayNode node = (DisplayNode) source;

        int x = node.getLocation().x;
        int y = node.getLocation().y;

        x = Math.max(x, 0);
        y = Math.max(y, 0);

        node.setLocation(x, y);
    }

    private void snapDragGroupFromNegative() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        List<DisplayNode> dragNodes = this.dragNodes;

        if (dragNodes == null) {
            return;
        }

        for (DisplayNode _node : dragNodes) {
            int x = _node.getLocation().x;
            int y = _node.getLocation().y;

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
        }

        minX = Math.min(minX, 0);
        minY = Math.min(minY, 0);

        for (DisplayNode _node : dragNodes) {
            int x = _node.getLocation().x;
            int y = _node.getLocation().y;

            _node.setLocation(x - minX, y - minY);
        }
    }

    private void dragNewEdge(Object source, Point newPoint) {
        if (source instanceof DisplayNode) {
            Point point = ((Component) source).getLocation();
            newPoint.translate(point.x, point.y);
        }

        if (getTrackedEdge() != null) {
            getTrackedEdge().updateTrackPoint(newPoint);
        }
    }

    private void dragNodes(Object source, Point newPoint, boolean shiftDown,
                           boolean altDown) {
        if (!isAllowNodeDragging()) {
            return;
        }

        if (source instanceof DisplayNode) {
            List<DisplayNode> dragNodes = this.dragNodes;

            if (!dragNodes.contains(source)) {
                moveSingleNode(source, newPoint);
            } else {
                moveSelectedNodes(source, newPoint);
            }
        } else if (this.rubberband != null) {
            this.rubberband.updateTrackPoint(newPoint);
            selectAllInRubberband(this.rubberband, shiftDown, altDown);
        }
    }

    /**
     * Move a single, unselected node.
     */
    private void moveSingleNode(Object source, Point newPoint) {
        DisplayNode node = (DisplayNode) source;
        int deltaX = newPoint.x - clickPoint.x;
        int deltaY = newPoint.y - clickPoint.y;

        int newX = node.getLocation().x + deltaX;
        int newY = node.getLocation().y + deltaY;

        if (newX > getMaxX()) {
            newX = getMaxX();
        }

        if (newY > getMaxY()) {
            newY = getMaxY();
        }

        node.setLocation(newX, newY);
    }

    /**
     * Move a group of selected nodes together.
     */
    private void moveSelectedNodes(Object source, Point newPoint) {
        if (!this.dragNodes.contains(source)) {
            return;
        }

        int deltaX = newPoint.x - clickPoint.x;
        int deltaY = newPoint.y - clickPoint.y;

        for (DisplayNode _node : this.dragNodes) {
            int newX = _node.getLocation().x + deltaX;
            int newY = _node.getLocation().y + deltaY;

            if (newX > getMaxX()) {
                newX = getMaxX();
            }

            if (newY > getMaxY()) {
                newY = getMaxY();
            }

            _node.setLocation(newX, newY);
        }
    }

    /**
     * Toggles endpoint A in the following sense: if it's a "o" make it a "-";
     * if it's a "-", make it a ">"; if it's a ">", make it a "-".
     *
     * @param endpoint 1 for endpoint 1, 2 for endpoint 2.
     */
    private void directEdge(IDisplayEdge graphEdge, int endpoint) {
        Edge edge = graphEdge.getModelEdge();

        if (edge == null) {
            throw new IllegalStateException(
                    "Graph edge without model edge: " + graphEdge);
        }

        Edge newEdge;

        if (endpoint == 1) {
            newEdge = Edges.directedEdge(edge.getNode2(), edge.getNode1());
        } else if (endpoint == 2) {
            newEdge = Edges.directedEdge(edge.getNode1(), edge.getNode2());
        } else {
            throw new IllegalArgumentException(
                    "Endpoint number should be 1 or 2.");
        }

        getGraph().removeEdge(edge);

        try {
            boolean added = getGraph().addEdge(newEdge);
            if (!added) {
                getGraph().addEdge(edge);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Reorienting that edge would violate graph constraints.");
            }
        }
        catch (IllegalArgumentException e) {
            getGraph().addEdge(edge);
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Reorienting that edge would violate graph constraints.");
        }

        repaint();
    }

    private void toggleEndpoint(IDisplayEdge graphEdge,
                                int endpointNumber) {
        Edge edge = graphEdge.getModelEdge();
        Edge newEdge;

        if (endpointNumber == 1) {
            Endpoint endpoint = edge.getEndpoint1();
            Endpoint nextEndpoint;

            if (endpoint == Endpoint.TAIL) {
                nextEndpoint = Endpoint.ARROW;
            } else if (endpoint == Endpoint.ARROW) {
                nextEndpoint = Endpoint.CIRCLE;
            } else {
                nextEndpoint = Endpoint.TAIL;
            }

            newEdge = new Edge(edge.getNode1(), edge.getNode2(), nextEndpoint,
                    edge.getEndpoint2());
        } else if (endpointNumber == 2) {
            Endpoint endpoint = edge.getEndpoint2();
            Endpoint nextEndpoint;

            if (endpoint == Endpoint.TAIL) {
                nextEndpoint = Endpoint.ARROW;
            } else if (endpoint == Endpoint.ARROW) {
                nextEndpoint = Endpoint.CIRCLE;
            } else {
                nextEndpoint = Endpoint.TAIL;
            }

            newEdge = new Edge(edge.getNode1(), edge.getNode2(),
                    edge.getEndpoint1(), nextEndpoint);
        } else {
            throw new IllegalArgumentException(
                    "Endpoint number should be 1 or 2.");
        }

        getGraph().removeEdge(edge);

        try {
            boolean added = getGraph().addEdge(newEdge);
            if (!added) {
                getGraph().addEdge(edge);
                return;
            }
        }
        catch (IllegalArgumentException e) {
            getGraph().addEdge(edge);
            return;
        }

        repaint();
    }

    public boolean isMouseDragging() {
        return mouseDragging;
    }

    public void setMouseDragging(boolean mouseDragging) {
        this.mouseDragging = mouseDragging;
    }

    /**
     * True if the user is allowed to add measured variables.
     */
    public boolean isAddMeasuredVarsAllowed() {
        return addMeasuredVarsAllowed;
    }

    /**
     * True if the user is allowed to add measured variables.
     */
    public void setAddMeasuredVarsAllowed(boolean addMeasuredVarsAllowed) {
        this.addMeasuredVarsAllowed = addMeasuredVarsAllowed;
    }

    /**
     * True if the user is allowed to edit existing meausred variables.
     */
    public boolean isEditExistingMeasuredVarsAllowed() {
        return editExistingMeasuredVarsAllowed;
    }

    /**
     * True if the user is allowed to edit existing measured variables.
     */
    public void setEditExistingMeasuredVarsAllowed(boolean editExistingMeasuredVarsAllowed) {
        this.editExistingMeasuredVarsAllowed = editExistingMeasuredVarsAllowed;
    }

    /**
     * True if the user is allowed to delete variables.
     */
    public boolean isDeleteVariablesAllowed() {
        return deleteVariablesAllowed;
    }

    /**
     * True if the user is allowed to delete variables.
     */
    public void setDeleteVariablesAllowed(boolean deleteVariablesAllowed) {
        this.deleteVariablesAllowed = deleteVariablesAllowed;
    }

    /**
     * This inner class is a simple wrapper for JComponents which are to serve
     * as edge labels in the workbench.  Its sole function is to make sure the
     * wrapped JComponents stay in the right place in the workbench--that is,
     * halfway along their respective edge, slightly off to the side.
     *
     * @author Joseph Ramsey
     */
    private static final class GraphEdgeLabel extends JComponent
            implements PropertyChangeListener {

        /**
         * The edge that this label should attach to.
         */
        private final IDisplayEdge edge;

        /**
         * The JComponent that serves as the label.
         */
        private final JComponent label;

        /**
         * The distance from the midpoint of the edge to the center of the
         * component along the normal to the edge.
         */
        private double normalDistance = 0.0;

        /**
         * Constructs a new label wrapper for the given JComponent and edge.
         *
         * @param edge  the edge with which the label is associated.
         * @param label the JComponent which serves as the label.
         */
        public GraphEdgeLabel(IDisplayEdge edge, JComponent label) {
            this.label = label;
            this.edge = edge;
            setLayout(new BorderLayout());
            add(label, BorderLayout.CENTER);
            updateLocation(edge.getPointPair());
            ((Component) edge).addPropertyChangeListener(this);
        }

        /**
         * Listens to "newPointPair" property changes which are emitted by the
         * display edge whenever a new point pair is calculated.
         *
         * @param e the property change event.
         */
        @Override
		public void propertyChange(PropertyChangeEvent e) {
            if ((e.getSource() == edge) &&
                    e.getPropertyName().equals("newPointPair")) {
                updateLocation((PointPair) e.getNewValue());
            }
        }

        /**
         * Updates the location of the label so that it lies halfway between the
         * two points of the point pair, slightly off to the right (if you go
         * from the "from" point to the "to" point).
         *
         * @param pp the point pair to track.
         */
        void updateLocation(PointPair pp) {
            if (pp != null) {
                moveCenterOutAlongNormal(pp);
                //            attachCornerToMidpoint(pp, label);
            }
        }

        private void moveCenterOutAlongNormal(PointPair pp) {
            // calculate the cross outerProduct of a unit vector from
            // pp.from in the direction of pp.to with a unit vector
            // perpendicular down into the workbench.  this will be a
            // unit vector normal to the vector from pp.from to pp.to.
            double dx = pp.getFrom().x - pp.getTo().x;
            double dy = pp.getFrom().y - pp.getTo().y;
            double dist = distance(pp.getFrom(), pp.getTo());
            double normalx = -dy / dist;
            double normaly = dx / dist;

            // Move the center of the label out from the midpoint in
            // the direction of this normal a distance of half the
            // diagonal of the label. (Note--a better "distance"
            // algorithm needs to be invented for this; a very wide
            // but short label gets put way too far from the edge at
            // some angles. jdramsey 10/25/01.)
            Point midPt = new Point((pp.getFrom().x + pp.getTo().x) / 2,
                    (pp.getFrom().y + pp.getTo().y) / 2);
            Point edgeLoc = ((Component) edge).getLocation();
            int setx = edgeLoc.x + midPt.x;

            // Putting the labels directly on the line. (If this works,
            // need to modifiy the API to give the user some choice
            // in the matter.)
            setx += (int) (getNormalDistance() * normalx) / 2;
            setx -= getLabel().getWidth() / 2;

            int sety = edgeLoc.y + midPt.y;

            sety += (int) (getNormalDistance() * normaly) / 2;
            sety -= getLabel().getHeight() / 2;

            setLocation(setx, sety);
        }

        public double getNormalDistance() {
            return this.normalDistance;
        }

        public void setNormalDistance(double normalDistance) {
            this.normalDistance = normalDistance;
        }

        public JComponent getLabel() {
            return label;
        }
    }

    /**
     * This inner class is a simple wrapper for JComponents which are to serve
     * as node labels in the workbench.  Its sole function is to make sure the
     * wrapped JComponents stay in the right place in the workbench--that is,
     * slightly off to the right of the display node.
     *
     * @author Joseph Ramsey
     */
    private static final class GraphNodeLabel extends JComponent {

        /**
         * The JComponent that serves as the label.
         */
        private final JComponent label;

        /**
         * The x location of the label relative to the center of the node.
         */
        private int xOffset = 0;

        /**
         * The y location of the label relative to the center of the node.
         */
        private int yOffset = 0;

        /**
         * Constructs a new label wrapper for the given JComponent and node.
         *
         * @param node  the node with which the label is associated.
         * @param label the JComponent which serves as the label.
         */
        public GraphNodeLabel(DisplayNode node, JComponent label, int xOffset,
                              int yOffset) {

            this.label = label;
            this.xOffset = xOffset;
            this.yOffset = yOffset;

            setLayout(new BorderLayout());
            add(label, BorderLayout.CENTER);
            updateLocation(node);
            node.addComponentListener(new ComponentAdapter() {
                @Override
				public void componentMoved(ComponentEvent e) {
                    updateLocation(e.getComponent());
                }
            });
        }

        void updateLocation(Component component) {
            int x = component.getX() + component.getWidth() / 2 + xOffset;
            int y = component.getY() + component.getHeight() / 2 + yOffset;
            setLocation(x, y);
        }

        public JComponent getLabel() {
            return label;
        }
    }

    //
    // Event handler classes
    //

    /**
     * Handles <code>ComponentEvent</code>s.  We use an inner class instead of
     * the workbench itself since we don't want to expose the handler methods on
     * the workbench's public API.
     */
    private static final class ComponentHandler extends ComponentAdapter {
        private final AbstractWorkbench workbench;

        public ComponentHandler(AbstractWorkbench workbench) {
            this.workbench = workbench;
        }

        /**
         * Adjusts scrollbars to automatically reflect the position of a
         * component which is being dragged.
         */
        @Override
		public final void componentMoved(ComponentEvent e) {
            Component source = (Component) e.getSource();
            Rectangle bounds = source.getBounds();

            if (source instanceof DisplayNode) {
                Node modelNode =
                        (Node) (workbench.getDisplayToModel().get(source));
                int centerX = bounds.x + bounds.width / 2;
                int centerY = bounds.y + bounds.height / 2;

                modelNode.setCenterX(centerX);
                modelNode.setCenterY(centerY);

                workbench.adjustPreferredSize();

                // This causes wierdness when nodes are dragged off to the
                // right. Replacing with a scroll to rect on mouseup.
                // jdramsey 4/29/2005
//                workbench.scrollRectToVisible(bounds);
            }
        }
    }

    /**
     * Handles mouse events and mouse motion events.
     */
    private static final class MouseHandler extends MouseAdapter {
        private final AbstractWorkbench workbench;

        public MouseHandler(AbstractWorkbench workbench) {
            this.workbench = workbench;
        }

        @Override
		public final void mouseClicked(MouseEvent e) {
            workbench.handleMouseClicked(e);
        }

        @Override
		public final void mousePressed(MouseEvent e) {
            workbench.handleMousePressed(e);
        }

        @Override
		public final void mouseReleased(MouseEvent e) {
            workbench.handleMouseReleased(e);
        }
    }

    private static final class MouseMotionHandler extends MouseMotionAdapter {
        private final AbstractWorkbench workbench;

        public MouseMotionHandler(AbstractWorkbench workbench) {
            this.workbench = workbench;
        }

        @Override
		public final void mouseMoved(MouseEvent e) {
            workbench.currentMouseLocation = e.getPoint();
        }

        @Override
		public final void mouseDragged(MouseEvent e) {
            workbench.handleMouseDragged(e);
        }
    }

    private void doDoubleClickAction(DisplayNode node) {
        deselectAll();
        node.doDoubleClickAction(getGraph());
    }

    private Map getDisplayToLabels() {
        return displayToLabels;
    }

    /**
     * This is necessary sometimes when the hashcodes of certain objects change
     * (e.g. a node changes its name.)
     */
    private void reconstiteMaps() {
        modelToDisplay = new HashMap(getModelToDisplay());
        displayToModel = new HashMap(displayToModel);
        displayToLabels = new HashMap(displayToLabels);
    }

    private IDisplayEdge getTrackedEdge() {
        return trackedEdge;
    }

    public boolean isNodeEdgeErrorsReported() {
        return nodeEdgeErrorsReported;
    }

    public void setNodeEdgeErrorsReported(boolean nodeEdgeErrorsReported) {
        this.nodeEdgeErrorsReported = nodeEdgeErrorsReported;
    }

    public boolean isRightClickPopupAllowed() {
        return rightClickPopupAllowed;
    }

    public void setRightClickPopupAllowed(boolean rightClickPopupAllowed) {
        this.rightClickPopupAllowed = rightClickPopupAllowed;
    }

    /**
     * Handles <code>PropertyChangeEvent</code>s.
     */
    private final class PropertyChangeHandler
            implements PropertyChangeListener {
        private final AbstractWorkbench workbench;

        public PropertyChangeHandler(AbstractWorkbench workbench) {
            this.workbench = workbench;
        }

        @Override
		public final void propertyChange(PropertyChangeEvent e) {
            String propName = e.getPropertyName();
            Object oldValue = e.getOldValue();
            Object newValue = e.getNewValue();

            if ("nodeAdded".equals(propName)) {
                workbench.addNode((Node) newValue);
            } else if ("nodeRemoved".equals(propName)) {
                workbench.removeNode((Node) oldValue);
            } else if ("edgeAdded".equals(propName)) {
                workbench.addEdge((Edge) newValue);
            } else if ("edgeRemoved".equals(propName)) {
                workbench.removeEdge((Edge) oldValue);
            } else if ("edgeLaunch".equals(propName)) {
                System.out.println("Attempt to launch edge.");
            } else if ("deleteNode".equals(propName)) {
                Object node = e.getSource();

                if (node instanceof DisplayNode) {
                    node = workbench.displayToModel.get(node);
                }

                if (node instanceof GraphNode) {
                    workbench.deselectAll();
                    workbench.selectNode((GraphNode) node);
                    workbench.deleteSelectedObjects();
                }
            } else if ("cloneMe".equals(propName)) {
                AbstractWorkbench.this.firePropertyChange("cloneMe",
                        e.getOldValue(), e.getNewValue());
            }
        }
    }
}

