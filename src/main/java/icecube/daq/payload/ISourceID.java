/*
 * class: ISourceID
 *
 * Version $Id: ISourceID.java,v 1.3 2006/08/08 20:18:15 vav111 Exp $
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
 * @version $Id: ISourceID.java,v 1.3 2006/08/08 20:18:15 vav111 Exp $
 * @author hellwig, dwharton
 */
public interface ISourceID extends ICopyable, Comparable {
    /**
     * get reference ID of source as integer
     */
    int getSourceID();

}
