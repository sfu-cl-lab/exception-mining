package edu.cmu.tetradapp.model;

/**
 * Search params for algorithms that use the <code>MeekRules</code>.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Feb 14, 2007 9:53:47 PM $
 */
public interface MeekSearchParams extends SearchParams{

    public boolean isAggressivelyPreventCycles();

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles);

}
