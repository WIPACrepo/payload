/*
 * class: IDOMID
 *
 * Version $Id: IDOMID.java 2656 2008-02-15 23:20:07Z dglo $
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
 * @version $Id: IDOMID.java 2656 2008-02-15 23:20:07Z dglo $
 * @author hellwig,dwharton
 * dwharton: added descriptions and moved package.
 */
public interface IDOMID extends ICopyable {
    /**
     * Get the DOMID as a long
     */
    long longValue();
}
