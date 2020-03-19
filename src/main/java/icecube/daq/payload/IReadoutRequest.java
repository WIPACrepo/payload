package icecube.daq.payload;
/*
 * class: IReadoutRequest
 *
 * @author dwharton,mhellwig
 */

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This Interface defines the message the EventBuilder sends
 * to the Stringprocessor to request data.
 *
 * @version $Id: IReadoutRequest.java,v 1.1 2004/12/06 22:44:48 dwharton Exp $
 * @author hellwig,dwharton
 */
public interface IReadoutRequest
{
    /**
     * Add a readout element.
     *
     * @param type element type
     * @param srcId ID of target string for this request (<tt>-1</tt> for all)
     * @param firstTime start of request
     * @param lastTime end of request
     * @param domId ID of DOM for this request (<tt>-1</tt> for all)
     */
    void addElement(int type, int srcId, long firstTime, long lastTime,
                    long domId);

    /**
     * Get the number of bytes required to store the data (without the
     * payload header) in a ByteBuffer.
     *
     * @return number of bytes
     */
    int getEmbeddedLength();

    /**
     * getReadoutSPRequestElements()
     * returns a list of IReadoutRequestElement's describing the
     * readout request for a single ISourceID (ie String)
     * @return list of IReadoutRequestElement
     */
    List getReadoutRequestElements();

    /**
     * gets the UTC time for this payload as a long value
     * @return time
     */
    long getUTCTime();

    /**
     * getUID()
     * returns the unique Trigger ID
     * by using this UID and the Stringnumber the Eventbuilder can
     * reassemble the events
     * @return int unique Trigger ID given by GlobalTrigger
     */
    int getUID();

    /**
     *  This is the ISourceID which generated this request.
     *  The locations of the individual sources which are to
     *  be requested for data are contained in the request-elements
     *  themselves.
     *  @return the ISourceID of the Trigger which generated
     *                       this request.
     */
    ISourceID getSourceID();

    /**
     * Get the number of bytes required to store this data in a ByteBuffer.
     *
     * @return number of bytes
     */
    int length();

    /**
     * Write the data (without the payload header) to the specified
     * ByteBuffer, starting at the offset given.
     *
     * @param buf ByteBuffer to write to
     * @param offset position to start writing
     *
     * @return number of bytes written
     *
     * @throws PayloadException if there is a problem
     */
    int putBody(ByteBuffer buf, int offset)
        throws PayloadException;

    /**
     * Set the source ID. Needed for backward compatiblility with the old
     * global request handler implementation.
     *
     * @param srcId new source ID
     */
    void setSourceID(ISourceID srcId);

    /**
     * Set the universal ID for global requests which will become events.
     *
     * @param uid new UID
     */
    void setUID(int uid);
}
