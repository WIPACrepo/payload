/*
 * class: IDOMID
 *
 * Version $Id: IDOMID.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;
import icecube.util.ICopyable;

/**
 * Encoding the DOM ID as an interface
 *
 * @version $Id: IDOMID.java 2125 2007-10-12 18:27:05Z ksb $
 * @author hellwig,dwharton
 * dwharton: added descriptions and moved package.
 */
public interface IDOMID extends ICopyable {
    /**
     * Get the DOMID as a long
     */
    long getDomIDAsLong();

    /**
     * Convert the domId to hex string
     * @return 12-character hex string representing the DOM ID.
     */
    String getDomIDAsString();

}
