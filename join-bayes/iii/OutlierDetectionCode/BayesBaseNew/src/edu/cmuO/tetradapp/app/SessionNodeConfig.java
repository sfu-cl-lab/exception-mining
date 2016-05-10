package edu.cmu.tetradapp.app;


/**
 * Represents the configuration details for a session node.
 *
 * @author Tyler Gibson
 */
public interface SessionNodeConfig {


    /**
     * Returns the model confif for the model with the given class or null if there isn't
     * one.
     *
     * @param model
     * @return  The model config for the given class or null.
     */
    SessionNodeModelConfig getModelConfig(Class model);


    /**
     * Returns all the models for this node.
     *
     * @return - all the models for this node.
     */
    Class[] getModels();


    /**
     * Returns text to use as a tooltip for the node.
     *
     * @return - tooltip text
     */
    String getTooltipText();


    /**
     * Returns a newly created <code>ModelChooser</code> that should be utilized to select a model. If no
     * chooser was specified then the default chooser will be returned.
     *
     * @param sessionNodeName - The current name of the node in the session. 
     * @return - model chooser.
     */
    ModelChooser getModelChooserInstance(String sessionNodeName);


    /**
     * Returns a newly created <code>SessionDisplayComp</code> that is used to display the node
     * on the session workbench. If no display component class was specified then a default instance
     * will be used.
     *
     * @return - session comp.
     */
    SessionDisplayComp getSessionDisplayCompInstance();


}
