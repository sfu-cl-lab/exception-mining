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
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.session.*;
import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.model.SessionNodeWrapper;
import edu.cmu.tetradapp.model.SessionWrapper;
import edu.cmu.tetradapp.workbench.AbstractWorkbench;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.IDisplayEdge;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.MarshalledObject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Adds the functionality needed to turn an abstract workbench into a workbench
 * usable for editing sessions.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6039 $ $Date: 2006-01-06 15:57:49 -0500 (Fri, 06 Jan
 *          2006) $
 */
public final class SessionEditorWorkbench extends AbstractWorkbench {

    /**
     * The string label of the next node to create.
     */
    private String nextButtonType;

    /**
     * The simulation study. This contains a reference to the session being
     * edited.
     */
    private SimulationStudy simulationStudy;

    //=========================CONSTRUCTORS==============================//

    /**
     * Constructs a new workbench for the given SessionWrapper.
     */
    public SessionEditorWorkbench(SessionWrapper sessionWrapper) {
        super(sessionWrapper);

        sessionWrapper.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent e) {
                String propertyName = e.getPropertyName();

                if ("name".equals(propertyName)) {
                    firePropertyChange("name", e.getOldValue(),
                            e.getNewValue());
                }
            }
        });

        super.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("cloneMe".equals(evt.getPropertyName())) {
                    SessionEditorNode sessionEditorNode =
                            (SessionEditorNode) evt.getNewValue();
                    SessionNodeWrapper wrapper =
                            (SessionNodeWrapper) sessionEditorNode.getModelNode();
                    Object result;
                    try {
                        result = new MarshalledObject(wrapper).get();
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                        throw new IllegalStateException("Could not clone.");
                    }
                    SessionNodeWrapper clone = (SessionNodeWrapper) result;
                    List sessionElements = Collections.singletonList(clone);
                    Point point = EditorUtils.getTopLeftPoint(sessionElements);
                    point.translate(50, 50);

                    SessionWrapper sessionWrapper = (SessionWrapper) getGraph();
                    deselectAll();
                    sessionWrapper.pasteSubsession(sessionElements, point);

                    selectNode(clone);
                }
            }
        });

        List nodes = getGraph().getNodes();

        for (Object node1 : nodes) {
            SessionNodeWrapper node = (SessionNodeWrapper) node1;
            setRepetitionLabel(node.getSessionNode());
        }

        setRightClickPopupAllowed(false);
    }

    //==========================PUBLIC METHODS===========================//

    /**
     * Returns a SessionNodeWrapper for a new SessionNode, the type of which is
     * determined by the next button type, and the name of which is the next
     * button type (a String) appended with the next available positive
     * integer.
     *
     * @see #getNextButtonType
     */
    @Override
	public Node getNewModelNode() {
        if (this.nextButtonType == null) {
            throw new NullPointerException(
                    "Next button type must be a " + "non-null string.");
        }

        String name = nextUniqueName(this.nextButtonType, getGraph());
        Class[] modelClasses = getModelClasses(this.nextButtonType);
        SessionNode newNode =
                new SessionNode(this.nextButtonType, name, modelClasses);

        SessionNodeWrapper nodeWrapper = new SessionNodeWrapper(newNode);
        nodeWrapper.setButtonType(this.nextButtonType);

        return nodeWrapper;
    }

    /**
     * Returns a new SessionEditorNode for the given SessionNodeWrapper (cast as
     * indicated).
     */
    @Override
	public DisplayNode getNewDisplayNode(Node modelNode) {
        SessionNodeWrapper wrapper = (SessionNodeWrapper) modelNode;
        String type = wrapper.getButtonType();

//        SessionConfig config = SessionConfig.getInstance();
//        Map sessionNodeImagesMap = config.getSessionNodeImagesMap();
//        Map sessionNodeTooltipsMap = config.getSessionNodeTooltipsMap();
//        String iconName = (String) sessionNodeImagesMap.get(type);
//        String tooltip = (String) sessionNodeTooltipsMap.get(type);
        SessionEditorNode displayNode = new SessionEditorNode(wrapper, getSimulationStudy());
        displayNode.adjustToModel();

        return displayNode;
    }

    /**
     * Returns a new edge from node1 to node2 (directed).
     */
    @Override
	public Edge getNewModelEdge(Node node1, Node node2) {
        return new Edge(node1, node2, Endpoint.TAIL, Endpoint.ARROW);
    }

    /**
     * Returns a new tracking edge from a node to some mouse location.
     */
    @Override
	public IDisplayEdge getNewTrackingEdge(DisplayNode node, Point mouseLoc) {
        return new SessionEditorEdge((SessionEditorNode) node, mouseLoc);
    }

    /**
     * Returns a new SessionEditorEdge for the given given edge (cast as
     * indicated).
     */
    @Override
	public IDisplayEdge getNewDisplayEdge(Edge modelEdge) {
        Node modelNodeA = modelEdge.getNode1();
        Node modelNodeB = modelEdge.getNode2();
        DisplayNode displayNodeA =
                (DisplayNode) (getModelToDisplay().get(modelNodeA));
        DisplayNode displayNodeB =
                (DisplayNode) (getModelToDisplay().get(modelNodeB));

        if ((displayNodeA == null) || (displayNodeB == null)) {
            return null;
        }

        SessionEditorNode sessionEditorNodeA = (SessionEditorNode) displayNodeA;
        SessionEditorNode sessionEditorNodeB = (SessionEditorNode) displayNodeB;

        return new SessionEditorEdge(sessionEditorNodeA, sessionEditorNodeB,
                SessionEditorEdge.UNRANDOMIZED);
    }


    /**
     * Resets the session wrapper that this editor is editing to the given
     * session wrapper.
     */
    public void setSessionWrapper(SessionWrapper sessionWrapper) {
        super.setGraph(sessionWrapper);

        // Force simulation study to be recreated.
        this.simulationStudy = null;

        sessionWrapper.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent e) {
                String propertyName = e.getPropertyName();

                if ("name".equals(propertyName)) {
                    firePropertyChange("name", e.getOldValue(),
                            e.getNewValue());
                }
            }
        });
    }

    public SimulationStudy getSimulationStudy() {
        if (this.simulationStudy == null) {
            Session session = ((SessionWrapper) getGraph()).getSession();
            this.simulationStudy = new SimulationStudy(session);

            this.simulationStudy.addSessionListener(new SessionAdapter() {
                @Override
				public void repetitionChanged(SessionEvent event) {
                    setRepetitionLabel(event.getNode());
                }
            });
        }

        return this.simulationStudy;
    }

    /**
     * Returns the button type of the next node to be constructed; this will be
     * the base for the next node's name (for instance "base1", "base2", ...),
     * and it will be String key for looking up the possible model classes for
     * the node.
     */
    public String getNextButtonType() {
        if (this.nextButtonType == null) {
            throw new NullPointerException("getNextButtonType() was called " +
                    "when there was no next button type set.");
        }

        return this.nextButtonType;
    }

    /**
     * Sets the String label of the next node to be created by the
     * <code>getNewModelNode</code> method. This label must be in the key set of
     * the <code>modelClassesMap</code> map and must be mapped there to a
     * Class[] array.
     */
    public void setNextButtonType(String nextButtonType) {
        this.nextButtonType = nextButtonType;
    }

    /**
     * Pastes list of session elements into the workbench.
     */
    public void pasteSubsession(List sessionElements, Point point) {
        SessionWrapper sessionWrapper = (SessionWrapper) getGraph();
        sessionWrapper.pasteSubsession(sessionElements, point);
        deselectAll();

        for (Object sessionElement : sessionElements) {
            if (sessionElement instanceof GraphNode) {
                Node modelNode = (Node) sessionElement;
                selectNode(modelNode);
            }
        }

        selectConnectingEdges();
    }

//    /**
//     * Sets the classes for the given button type.
//     *
//     * @throws NullPointerException if either 'nextButtonType' or 'classes' is
//     *                              null.
//     */
//    public void setModelClasses(String buttonType, Class[] classes) {
//        if (nextButtonType == null || classes == null) {
//            throw new NullPointerException("Button type and classes array " +
//                    "must both be non-null.");
//        }
//
//        Map modelClassesMap = SessionConfig.getInstance().getModelClassesMap();
//        modelClassesMap.put(buttonType, classes);
//    }

    public SessionWrapper getSessionWrapper() {
        return (SessionWrapper) getGraph();
    }

    @Override
	public void setName(String name) {
        getSimulationStudy().getSession().setName(name);
    }

    //===========================PRIVATE METHODS========================//

    /**
     * Adds a label to the session editor node for the given session node
     * indicating how many times the simulation study will run that node. If the
     * number is one, the label is removed.
     */
    private void setRepetitionLabel(SessionNode sessionNode) {
        if (sessionNode == null) {
            throw new NullPointerException("Node must not be null.");
        }

        SessionNodeWrapper sessionNodeWrapper =
                getSessionNodeWrapper(sessionNode);
        int repetitions = sessionNode.getRepetition();

        if (repetitions > 1) {
            JLabel label = new JLabel("x " + repetitions);
            label.setForeground(Color.red);
            setNodeLabel(sessionNodeWrapper, label, 25, 25);
        }
        else {
            setNodeLabel(sessionNodeWrapper, null, 25, 25);
        }
    }

    /**
     * Returns the SessionNodeWrapper for the given SessionEditorNode.
     */
    private SessionNodeWrapper getSessionNodeWrapper(SessionNode sessionNode) {
        Set keySet = getModelToDisplay().keySet();

        for (Object key : keySet) {
            if (key instanceof SessionNodeWrapper) {
                SessionNodeWrapper wrapper = (SessionNodeWrapper) key;

                if (wrapper.getSessionNode() == sessionNode) {
                    return wrapper;
                }
            }
        }

        throw new NullPointerException("Session node wrapper not in map.");
    }

    /**
     * Returns the model classes associated with the given button type.
     *
     * @throws NullPointerException if no classes are stored for the given
     *                              type.
     */
    private static Class[] getModelClasses(String nextButtonType) {
        //Map modelClassesMap = SessionConfig.getInstance().getModelClassesMap();
        //Class[] classes = (Class[]) modelClassesMap.get(nextButtonType);
        TetradApplicationConfig config = TetradApplicationConfig.getInstance();
        SessionNodeConfig nodeConfig = config.getSessionNodeConfig(nextButtonType);

        if (nodeConfig == null) {
            throw new NullPointerException("There is no configuration for: " + nextButtonType);
        }

        return nodeConfig.getModels();
    }

    /**
     * Returns the next string in the sequence.
     *
     * @param base the string base of the name--for example, "Graph".
     * @return the next string in the sequence--for example, "Graph1".
     */
    private static String nextUniqueName(String base, Graph graph) {

        if (base == null) {
            throw new NullPointerException("Base name must be non-null.");
        }

        int i = 0;    // Sequence 1, 2, 3, ...

        loop:
        while (true) {
            i++;
            String name = base + i;

            for (Object o : graph.getNodes()) {
                Node node = (Node) (o);

                if (node.getName().equals(name)) {
                    continue loop;
                }
            }

            break;
        }

        return base + i;
    }
}


