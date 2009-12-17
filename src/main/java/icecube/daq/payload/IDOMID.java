/*
 * class: IDOMID
 *
 * Version $Id: IDOMID.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * Encoding the DOM ID as an interface
 *
 * @version $Id: IDOMID.java 4574 2009-08-28 21:32:32Z dglo $
 * @author hellwig,dwharton
 * dwharton: added descriptions and moved package.
 */
public interface IDOMID
    extends ICopyable
{
    /**
     * Get the DOMID as a long
     * @return DOM ID as a long
     */
    long longValue();
}
