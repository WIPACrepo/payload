/*
 * class: IDOMID
 *
 * Version $Id: IDOMID.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
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
 * @version $Id: IDOMID.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
 * @author hellwig,dwharton
 * dwharton: added descriptions and moved package.
 */
public interface IDOMID extends ICopyable {
    /**
     * Get's the DOMID as a long
     */
    public long getDomIDAsLong();

    /**
     * Convert the domId to hex string
     * @return 12-character hex string representing the DOM ID.
     */
    public String getDomIDAsString();

}
