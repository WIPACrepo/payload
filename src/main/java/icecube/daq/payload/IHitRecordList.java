package icecube.daq.payload;

/**
 * List of hit records
 */
public interface IHitRecordList
    extends Iterable<IEventHitRecord>
{
    /**
     * Get the unique ID of the trigger request associated with this list
     * @return unique ID
     */
    int getUID();
}
