/*
 * class: ISourceID
 *
 * Version $Id: ISourceID.java 2631 2008-02-11 06:27:31Z dglo $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import icecube.util.ICopyable;

/**
 * Interface for encoded DAQ process IDs
 *
 * @version $Id: ISourceID.java 2631 2008-02-11 06:27:31Z dglo $
 * @author hellwig, dwharton
 */
public interface ISourceID extends ICopyable, Comparable {
    /**
     * get reference ID of source as integer
     */
    int getSourceID();

}
