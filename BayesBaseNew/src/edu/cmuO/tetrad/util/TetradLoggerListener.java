package edu.cmu.tetrad.util;

/**
 * A listener for tetrad's logger.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Mar 27, 2007 3:31:17 AM $
 */
public interface TetradLoggerListener {


    /**
     * Invoked whenever a logger configuration is set on the <code>TetradLogger</code> and
     * the logger is active (i.e., logging isn't turned off etc).
     *
     * @param evt
     */
    public void configurationActived(TetradLoggerEvent evt);


    /**
     * Invoked whenever a previously set logger config is resert or set to null and
     * the logger is active (i.e., logging isn't turned off etc).
     *
     * @param evt
     */
    public void configurationDeactived(TetradLoggerEvent evt);



    

}
