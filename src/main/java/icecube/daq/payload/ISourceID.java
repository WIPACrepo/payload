/*
 * class: ISourceID
 *
 * Version $Id: ISourceID.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * Interface for encoded DAQ process IDs
 *
 * @version $Id: ISourceID.java 4574 2009-08-28 21:32:32Z dglo $
 * @author hellwig, dwharton
 */
public interface ISourceID
    extends ICopyable, Comparable
{
    /**
     * get reference ID of source as integer
     * @return ID value
     */
    int getSourceID();
}
