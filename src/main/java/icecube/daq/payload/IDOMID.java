/*
 * class: IDOMID
 *
 * Version $Id: IDOMID.java 2631 2008-02-11 06:27:31Z dglo $
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
 * @version $Id: IDOMID.java 2631 2008-02-11 06:27:31Z dglo $
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
