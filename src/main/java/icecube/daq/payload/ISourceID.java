/*
 * class: ISourceID
 *
 * Version $Id: ISourceID.java 2125 2007-10-12 18:27:05Z ksb $
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
 * @version $Id: ISourceID.java 2125 2007-10-12 18:27:05Z ksb $
 * @author hellwig, dwharton
 */
public interface ISourceID extends ICopyable, Comparable {
    /**
     * get reference ID of source as integer
     */
    int getSourceID();

}
