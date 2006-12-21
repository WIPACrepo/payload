/*
 * class: IHitDataPayload
 *
 *
 */

package icecube.daq.trigger;

import java.util.zip.DataFormatException;
import java.io.IOException;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IDOMID;
import icecube.daq.trigger.ITriggerPayload;

/**
 * Interface of a payload describing a single hit and its associated engineering format
 * data.
 *
 * @author dwharton
 */
public interface IHitDataPayload extends IHitPayload {
    /**
     * Get's access to the underlying data for an engineering hit
     */
    public IHitDataRecord getHitRecord() throws IOException, DataFormatException;
}
