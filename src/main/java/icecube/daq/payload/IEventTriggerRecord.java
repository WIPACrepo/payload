package icecube.daq.payload;

import icecube.daq.util.IDOMRegistry;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Trigger data recorded in an event.
 */
public interface IEventTriggerRecord
{
    /**
     * Find the trigger record's hits in <tt>hitRecList</tt> and cache a
     * list of those indices.
     * @param domRegistry used to map each hit's DOM ID to the channel ID
     * @param hitRecList list of hits
     * @throws PayloadException if there is a problem
     */
    void computeIndices(IDOMRegistry domRegistry, List<IEventHitRecord> hitRecList)
        throws PayloadException;
    /**
     * Return the starting time for this trigger.
     * @return starting time
     */
    long getFirstTime();
    /**
     * Return the ending time for this trigger.
     * @return ending time
     */
    long getLastTime();
    /**
     * Return the source of this trigger.
     * @return source ID
     */
    int getSourceID();
    /**
     * Return the number of bytes in this record.
     * @return number of bytes
     */
    int length();
    /**
     * Write this trigger record.
     * @param buf byte buffer
     * @param offset index of first trigger record byte in the byte buffer
     * @param baseTime base timestamp from which relative times are computed
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    int writeRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException;
}
